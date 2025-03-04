package org.rocksdb

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.file.Files

@ExperimentalStdlibApi
@RunWith(AndroidJUnit4::class)
class RocksDBBasicTest {
    @Test
    fun openDBWriteAndReadValue() {
        val dir = Files.createTempDirectory("rocksdb")
        val db = RocksDB.open(dir.toUri().path)
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
}
