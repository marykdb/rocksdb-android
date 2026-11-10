package org.rocksdb

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.EnumSet

@RunWith(AndroidJUnit4::class)
class TaggedNativeHandleTest {
    @Test(timeout = 60_000)
    fun bloomFilterIsWrittenToSst() = withTestDirectory("bloom") { directory ->
        BloomFilter(10.0).use { filter ->
            requireTaggedHandlesWhenRequested(filter)
            Options().setCreateIfMissing(true)
                .setTableFormatConfig(BlockBasedTableConfig().setFilterPolicy(filter)).use { options ->
                    RocksDB.open(options, directory.path).use { db ->
                        writeAndFlush(db)
                        assertArrayEquals(value, db.get(key))
                        val properties = db.propertiesOfAllTables.values.single()
                        assertTrue("Configured Bloom filter must be stored in the SST", properties.filterSize > 0)
                    }
                }
        }
    }

    @Test(timeout = 60_000)
    fun suppliedBlockCacheReceivesBlocks() = withTestDirectory("block-cache") { directory ->
        LRUCache(4L * 1024 * 1024).use { cache ->
            requireTaggedHandlesWhenRequested(cache)
            Options().setCreateIfMissing(true).setTableFormatConfig(
                BlockBasedTableConfig().setBlockCache(cache).setCacheIndexAndFilterBlocks(true),
            ).use { options ->
                RocksDB.open(options, directory.path).use { db ->
                    writeAndFlush(db)
                    assertArrayEquals(value, db.get(key))
                    assertTrue("Reads must use the caller's block cache", cache.usage > 0)
                }
            }
        }
    }

    @Test(timeout = 60_000)
    fun persistentCacheReceivesSstReads() = withTestDirectory("persistent") { directory ->
        object : Logger(InfoLogLevel.ERROR_LEVEL) {
            override fun log(logLevel: InfoLogLevel, logMsg: String) = Unit
        }.use { logger ->
            PersistentCache(Env.getDefault(), File(directory, "cache").path,
                128L * 1024 * 1024, logger, false).use { cache ->
                requireTaggedHandlesWhenRequested(cache)
                Statistics().use { statistics ->
                    Options().setCreateIfMissing(true).setStatistics(statistics).setTableFormatConfig(
                        BlockBasedTableConfig().setPersistentCache(cache).setNoBlockCache(true),
                    ).use { options ->
                        val dbPath = File(directory, "db").path
                        RocksDB.open(options, dbPath).use { writeAndFlush(it) }
                        RocksDB.open(options, dbPath).use { db ->
                            assertArrayEquals(value, db.get(key))
                            assertTrue("SST reads must consult the persistent cache",
                                statistics.getTickerCount(TickerType.PERSISTENT_CACHE_MISS) > 0)
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 60_000)
    fun statisticsForwardThroughBothConstructorsAndSurviveWrapperClose() {
        for (ignoreHistograms in listOf(false, true)) {
            withTestDirectory("statistics-$ignoreHistograms") { directory ->
                Statistics().use { delegate ->
                    requireTaggedHandlesWhenRequested(delegate)
                    val forwarding = if (ignoreHistograms) {
                        Statistics(EnumSet.noneOf(HistogramType::class.java), delegate)
                    } else {
                        Statistics(delegate)
                    }
                    forwarding.use {
                        requireTaggedHandlesWhenRequested(forwarding)
                        Options().setCreateIfMissing(true).setStatistics(forwarding).use { options ->
                            RocksDB.open(options, directory.path).use { db ->
                                db.put(key, value)
                                val firstCount = delegate.getTickerCount(TickerType.BYTES_WRITTEN)
                                assertTrue("Writes must reach the supplied statistics delegate", firstCount > 0)
                                assertEquals(forwarding.getTickerCount(TickerType.BYTES_WRITTEN), firstCount)
                                forwarding.close()
                                forwarding.close()
                                db.put("second".encodeToByteArray(), value)
                                assertTrue("The database retains statistics after its Java wrapper closes",
                                    delegate.getTickerCount(TickerType.BYTES_WRITTEN) > firstCount)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requireTaggedHandlesWhenRequested(vararg objects: RocksObject) {
        if (InstrumentationRegistry.getArguments().getString("requireTaggedHandles") == "true") {
            objects.forEach { resource ->
                assertTrue("This regression run requires a tagged pointer with its sign bit set",
                    resource.nativeHandle < 0)
            }
        }
    }

    private fun writeAndFlush(db: RocksDB) {
        db.put(key, value)
        FlushOptions().setWaitForFlush(true).use { db.flush(it) }
    }

    private fun withTestDirectory(name: String, block: (File) -> Unit) {
        RocksDB.loadLibrary()
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val directory = File(cacheDir, "rocksdb-tagged-$name-${System.nanoTime()}")
        check(directory.mkdirs()) { "Cannot create test directory $directory" }
        try {
            block(directory)
        } finally {
            directory.deleteRecursively()
        }
    }

    private val key = "key".encodeToByteArray()
    private val value = ByteArray(32 * 1024) { it.toByte() }
}
