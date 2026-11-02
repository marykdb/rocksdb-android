package org.rocksdb

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.file.Files

@ExperimentalStdlibApi
@RunWith(AndroidJUnit4::class)
class RocksDBBasicTest {
    @Test
    fun backgroundFlushCallsJavaListener() {
        RocksDB.loadLibrary()
        val completed = java.util.concurrent.CountDownLatch(1)
        val listener = object : AbstractEventListener() {
            override fun onFlushCompleted(db: RocksDB, flushJobInfo: FlushJobInfo) {
                completed.countDown()
            }
        }
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val dir = java.io.File(cacheDir, "rocksdb-listener-${System.nanoTime()}")
        try {
            listener.use {
                Options().setCreateIfMissing(true).setListeners(listOf(listener)).use { options ->
                    RocksDB.open(options, dir.path).use { db ->
                        db.put("key".encodeToByteArray(), "value".encodeToByteArray())
                        FlushOptions().setWaitForFlush(true).use { db.flush(it) }
                        org.junit.Assert.assertTrue(
                            "Native flush thread must invoke the Java listener",
                            completed.await(10, java.util.concurrent.TimeUnit.SECONDS),
                        )
                    }
                }
            }
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun versionAndCompressedTablesSurviveReopen() {
        RocksDB.loadLibrary()
        assertEquals("11.8.1", RocksDB.rocksdbVersion().toString())
        val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val value = "compressible-value-".repeat(1024).encodeToByteArray()
        for (compression in listOf(
            CompressionType.SNAPPY_COMPRESSION,
            CompressionType.LZ4_COMPRESSION,
            CompressionType.LZ4HC_COMPRESSION,
            CompressionType.ZLIB_COMPRESSION,
            CompressionType.ZSTD_COMPRESSION,
            CompressionType.BZLIB2_COMPRESSION,
        )) {
            val dir = java.io.File(cacheDir, "rocksdb-${compression}-${System.nanoTime()}")
            try {
                Options().setCreateIfMissing(true).setCompressionType(compression).use { options ->
                    RocksDB.open(options, dir.path).use { db ->
                        db.put("key".encodeToByteArray(), value)
                        FlushOptions().setWaitForFlush(true).use { db.flush(it) }
                    }
                    RocksDB.open(options, dir.path).use { db ->
                        org.junit.Assert.assertArrayEquals(value, db.get("key".encodeToByteArray()))
                        val table = db.getPropertiesOfAllTables().values.single()
                        org.junit.Assert.assertTrue(table.compressionName != "NoCompression")
                    }
                }
            } finally {
                dir.deleteRecursively()
            }
        }
    }

    @Test
    fun openDBWriteAndReadValue() {
        val dir = Files.createTempDirectory("rocksdb")
        val db = RocksDB.open(dir.toUri().path)
        assertEquals(CompressionType.LZ4_COMPRESSION, db.options.compressionType())
        assertEquals(dir.toUri().path, db.name)
        db.put("key".encodeToByteArray(), "value".encodeToByteArray())
        assertEquals("value", db.get("key".encodeToByteArray()).decodeToString())
    }

    @Test
    fun openDBWriteAndReadValueLZ4() {
        val dir = Files.createTempDirectory("rocksdb-lz4")
        val options = Options().apply {
            setCreateIfMissing(true)
            setCompressionType(CompressionType.LZ4_COMPRESSION)
        }
        val db = RocksDB.open(options, dir.toUri().path)
        assertEquals(dir.toUri().path, db.name)
        db.put("key".encodeToByteArray(), "value".encodeToByteArray())
        assertEquals("value", db.get("key".encodeToByteArray()).decodeToString())
    }


    @Test
    fun openDBWriteAndReadValueLZ4HC() {
        val dir = Files.createTempDirectory("rocksdb-lz4hc")
        val options = Options().apply {
            setCreateIfMissing(true)
            setCompressionType(CompressionType.LZ4HC_COMPRESSION)
        }
        val db = RocksDB.open(options, dir.toUri().path)
        assertEquals(dir.toUri().path, db.name)
        db.put("key".encodeToByteArray(), "value".encodeToByteArray())
        assertEquals("value", db.get("key".encodeToByteArray()).decodeToString())
    }
    @Test
    fun openDBWriteAndReadValueZLib() {
        val dir = Files.createTempDirectory("rocksdb-zlib")
        val options = Options().apply {
            setCreateIfMissing(true)
            setCompressionType(CompressionType.ZLIB_COMPRESSION)
        }
        val db = RocksDB.open(options, dir.toUri().path)
        assertEquals(dir.toUri().path, db.name)
        db.put("key".encodeToByteArray(), "value".encodeToByteArray())
        assertEquals("value", db.get("key".encodeToByteArray()).decodeToString())
    }

    @Test
    fun openDBWriteAndReadValueSnappy() {
        val dir = Files.createTempDirectory("rocksdb-snappy")
        val options = Options().apply {
            setCreateIfMissing(true)
            setCompressionType(CompressionType.SNAPPY_COMPRESSION)
        }
        val db = RocksDB.open(options, dir.toUri().path)
        assertEquals(dir.toUri().path, db.name)
        db.put("key".encodeToByteArray(), "value".encodeToByteArray())
        assertEquals("value", db.get("key".encodeToByteArray()).decodeToString())
    }

    @Test
    fun openDBWriteAndReadValueBz2() {
        val dir = Files.createTempDirectory("rocksdb-bz2")
        val options = Options().apply {
            setCreateIfMissing(true)
            setCompressionType(CompressionType.BZLIB2_COMPRESSION)
        }
        val db = RocksDB.open(options, dir.toUri().path)
        assertEquals(dir.toUri().path, db.name)
        db.put("key".encodeToByteArray(), "value".encodeToByteArray())
        assertEquals("value", db.get("key".encodeToByteArray()).decodeToString())
    }

    @Test
    fun openDBWriteAndReadValueZstd() {
        val dir = Files.createTempDirectory("rocksdb-zstd")
        val options = Options().apply {
            setCreateIfMissing(true)
            setCompressionType(CompressionType.ZSTD_COMPRESSION)
        }
        val db = RocksDB.open(options, dir.toUri().path)
        assertEquals(dir.toUri().path, db.name)
        db.put("key".encodeToByteArray(), "value".encodeToByteArray())
        assertEquals("value", db.get("key".encodeToByteArray()).decodeToString())
    }
}
