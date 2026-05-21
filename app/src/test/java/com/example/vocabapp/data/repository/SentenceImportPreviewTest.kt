package com.example.vocabapp.data.repository

import com.example.vocabapp.parseCsvRows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SentenceImportPreviewTest {

    private fun buildCsv(vararg lines: String): String = lines.joinToString("\n")

    @Test
    fun parseCsvRows_emptyInput_returnsEmptyList() {
        val result = parseCsvRows("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseCsvRows_headerOnly_returnsSingleRow() {
        val result = parseCsvRows("sentence,meaning")
        assertEquals(1, result.size)
        assertEquals(listOf<String>("sentence", "meaning"), result[0])
    }

    @Test
    fun parseCsvRows_twoDataRows_returnsThreeRows() {
        val csv = buildCsv(
            "sentence,meaning",
            "Hello world this is a test sentence,テスト文",
            "Another long enough sentence here,別のテスト"
        )
        val result = parseCsvRows(csv)
        assertEquals(3, result.size)
    }

    @Test
    fun parseCsvRows_quotedCell_handlesCommaInside() {
        val csv = "sentence,meaning\n\"Hello, world this is a test\",意味"
        val result = parseCsvRows(csv)
        assertEquals(2, result.size)
        assertEquals("Hello, world this is a test", result[1][0])
    }

    @Test
    fun normalizeEnglish_lowercasesAndTrims() {
        assertEquals("hello world", "  Hello World  ".normalizeEnglish())
    }

    @Test
    fun getOrEmpty_validIndex_returnsValue() {
        val list = listOf("a", "b", "c")
        assertEquals("b", list.getOrEmpty(1))
    }

    @Test
    fun getOrEmpty_outOfBoundsIndex_returnsEmpty() {
        val list = listOf("a")
        assertEquals("", list.getOrEmpty(5))
    }

    @Test
    fun getOrEmpty_negativeIndex_returnsEmpty() {
        val list = listOf("a")
        assertEquals("", list.getOrEmpty(-1))
    }
}
