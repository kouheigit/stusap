package com.example.vocabapp.data.repository

import com.example.vocabapp.parseCsvRows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SentenceImportHeaderTest {

    private fun headerIndexOf(header: String, aliases: Set<String>): Int {
        val headerLower = listOf(header.lowercase())
        return headerLower.indexOfFirst { it in aliases }
    }

    private val sentenceAliases = setOf("sentence", "english", "英文", "文章", "例文", "英語")
    private val meaningAliases = setOf("meaning", "japanese meaning", "日本語の意味", "意味", "japanese", "訳", "和訳", "日本語")

    @Test
    fun sentenceColumn_englishHeader_found() {
        assertEquals(0, headerIndexOf("sentence", sentenceAliases))
        assertEquals(0, headerIndexOf("english", sentenceAliases))
        assertEquals(0, headerIndexOf("英文", sentenceAliases))
        assertEquals(0, headerIndexOf("文章", sentenceAliases))
        assertEquals(0, headerIndexOf("例文", sentenceAliases))
        assertEquals(0, headerIndexOf("英語", sentenceAliases))
    }

    @Test
    fun meaningColumn_japaneseHeaders_found() {
        assertEquals(0, headerIndexOf("meaning", meaningAliases))
        assertEquals(0, headerIndexOf("japanese meaning", meaningAliases))
        assertEquals(0, headerIndexOf("意味", meaningAliases))
        assertEquals(0, headerIndexOf("訳", meaningAliases))
        assertEquals(0, headerIndexOf("和訳", meaningAliases))
        assertEquals(0, headerIndexOf("日本語", meaningAliases))
    }

    @Test
    fun unknownHeader_notFound() {
        assertEquals(-1, headerIndexOf("unknown_column", sentenceAliases))
        assertEquals(-1, headerIndexOf("no_header", meaningAliases))
    }

    @Test
    fun headerSearch_isCaseInsensitive() {
        val aliases = setOf("sentence")
        val headers = listOf("Sentence", "Meaning").map { it.lowercase() }
        assertEquals(0, headers.indexOfFirst { it in aliases })
    }

    @Test
    fun meaningHeader_isCaseInsensitive() {
        val aliases = meaningAliases
        val headers = listOf("Sentence", "Meaning").map { it.lowercase() }
        assertEquals(1, headers.indexOfFirst { it in aliases })
    }
}
