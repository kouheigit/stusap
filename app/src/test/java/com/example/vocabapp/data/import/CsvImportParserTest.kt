package com.example.vocabapp.data.import

import com.example.vocabapp.decodeCsvBytes
import java.nio.charset.Charset
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvImportParserTest {
    @Test
    fun decodeCsvBytes_removesUtf8Bom() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "english,meaning".toByteArray()

        assertEquals("english,meaning", decodeCsvBytes(bytes))
    }

    @Test
    fun decodeCsvBytes_fallsBackToMs932() {
        val bytes = "英語,日本語".toByteArray(Charset.forName("MS932"))

        assertEquals("英語,日本語", decodeCsvBytes(bytes))
    }
}
