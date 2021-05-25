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
        val a = Files.createTempDirectory("rocksdb")
        val db = RocksDB.open(a.toUri().path)
        assertEquals(a.toUri().path, db.name)
        db.put("key".encodeToByteArray(), "value".encodeToByteArray())
        assertEquals("value", db.get("key".encodeToByteArray()).decodeToString())
    }
}
