package com.example.vocabapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class WordImportHeaderTest {

    @Test
    fun resolveWordImportColumns_withHeader_usesHeaderIndexesAndSkipsFirstRow() {
        val columns = resolveWordImportColumns(listOf("meaning", "type", "english"))

        assertEquals(2, columns?.englishIndex)
        assertEquals(0, columns?.meaningIndex)
        assertEquals(1, columns?.typeIndex)
        assertEquals(1, columns?.dataStartIndex)
    }

    @Test
    fun resolveWordImportColumns_withoutHeader_usesFirstTwoColumnsFromFirstRow() {
        val columns = resolveWordImportColumns(listOf("take care of", "世話をする"))

        assertEquals(0, columns?.englishIndex)
        assertEquals(1, columns?.meaningIndex)
        assertEquals(-1, columns?.exampleIndex)
        assertEquals(-1, columns?.exampleTranslationIndex)
        assertEquals(-1, columns?.typeIndex)
        assertEquals(0, columns?.dataStartIndex)
    }

    @Test
    fun resolveWordImportColumns_withPartialHeader_returnsNull() {
        val columns = resolveWordImportColumns(listOf("english", "日本語訳"))

        assertEquals(null, columns)
    }

    @Test
    fun resolveWordImportColumns_withoutHeaderAndOnlyOneColumn_returnsNull() {
        val columns = resolveWordImportColumns(listOf("apple"))

        assertEquals(null, columns)
    }

    @Test
    fun validateWordImportRow_withoutTypeUsesWordDefaultEvenWhenEnglishHasSpaces() {
        val errors = mutableListOf<String>()

        val result = validateWordImportRow(
            rowNumber = 2,
            row = listOf("ice cream", "アイスクリーム"),
            english = "ice cream",
            meaning = "アイスクリーム",
            example = "",
            exampleTranslation = "",
            rawType = "",
            defaultType = CUSTOM_WORD_TYPE,
            addError = { _, reason, _ -> errors += reason }
        )

        assertEquals(CUSTOM_WORD_TYPE, result)
        assertEquals(emptyList<String>(), errors)
    }

    @Test
    fun validateWordImportRow_withoutTypeUsesPhraseDefaultForIdiomImport() {
        val errors = mutableListOf<String>()

        val result = validateWordImportRow(
            rowNumber = 2,
            row = listOf("take care of", "世話をする"),
            english = "take care of",
            meaning = "世話をする",
            example = "",
            exampleTranslation = "",
            rawType = "",
            defaultType = CUSTOM_PHRASE_TYPE,
            addError = { _, reason, _ -> errors += reason }
        )

        assertEquals(CUSTOM_PHRASE_TYPE, result)
        assertEquals(emptyList<String>(), errors)
    }
}
