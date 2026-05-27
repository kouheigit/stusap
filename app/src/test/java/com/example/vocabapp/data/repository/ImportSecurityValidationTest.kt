package com.example.vocabapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportSecurityValidationTest {

    @Test
    fun validateWordImportRow_rejectsSpreadsheetFormulaCells() {
        val errors = mutableListOf<String>()

        val result = validateWordImportRow(
            rowNumber = 2,
            row = listOf("=HYPERLINK(\"https://example.com\")", "meaning"),
            english = "=HYPERLINK(\"https://example.com\")",
            meaning = "meaning",
            example = "",
            exampleTranslation = "",
            rawType = "",
            addError = { _, reason, _ -> errors += reason }
        )

        assertEquals(null, result)
        assertEquals(listOf("安全のため = + - @ で始まるセルは取り込めません"), errors)
    }

    @Test
    fun validateSentenceImportRow_rejectsSpreadsheetFormulaCells() {
        val errors = mutableListOf<String>()

        val result = validateSentenceImportRow(
            rowNumber = 2,
            row = listOf("@SUM(A1:A2)", "meaning"),
            sentence = "@SUM(A1:A2)",
            meaning = "meaning",
            addError = { _, reason, _ -> errors += reason }
        )

        assertFalse(result)
        assertEquals(listOf("安全のため = + - @ で始まるセルは取り込めません"), errors)
    }

    @Test
    fun validateSentenceImportRow_acceptsNormalSentence() {
        val errors = mutableListOf<String>()

        val result = validateSentenceImportRow(
            rowNumber = 2,
            row = listOf("This is a normal import sentence", "意味"),
            sentence = "This is a normal import sentence",
            meaning = "意味",
            addError = { _, reason, _ -> errors += reason }
        )

        assertTrue(result)
        assertTrue(errors.isEmpty())
    }
}
