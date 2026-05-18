package com.example.vocabapp

import java.nio.charset.StandardCharsets
import org.junit.Assert.assertThrows
import org.junit.Test

class XlsxWorksheetParserTest {
    @Test
    fun parseWorksheetRows_rejectsTooManyColumns() {
        val cells = (0..MAX_IMPORT_COLUMNS).joinToString("") { index ->
            val column = ('A'.code + index).toChar()
            """<c r="${column}1"><v>x</v></c>"""
        }
        val sheet = """<worksheet><sheetData><row>$cells</row></sheetData></worksheet>"""

        assertThrows(RuntimeException::class.java) {
            parseWorksheetRows(sheet.toByteArray(StandardCharsets.UTF_8), emptyList())
        }
    }

    @Test
    fun parseWorksheetRows_rejectsTooLongCell() {
        val value = "a".repeat(MAX_IMPORT_CELL_CHARS + 1)
        val sheet = """<worksheet><sheetData><row><c r="A1"><v>$value</v></c></row></sheetData></worksheet>"""

        assertThrows(RuntimeException::class.java) {
            parseWorksheetRows(sheet.toByteArray(StandardCharsets.UTF_8), emptyList())
        }
    }

    @Test
    fun parseWorksheetRows_rejectsTooManyRows() {
        val rows = (0..MAX_IMPORT_ROWS + 1).joinToString("") { """<row><c r="A1"><v>x</v></c></row>""" }
        val sheet = """<worksheet><sheetData>$rows</sheetData></worksheet>"""

        assertThrows(RuntimeException::class.java) {
            parseWorksheetRows(sheet.toByteArray(StandardCharsets.UTF_8), emptyList())
        }
    }
}
