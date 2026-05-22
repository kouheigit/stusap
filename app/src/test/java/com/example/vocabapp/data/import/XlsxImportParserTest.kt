package com.example.vocabapp

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class XlsxImportParserTest {

    @Test
    fun parseXlsxRows_readsInlineStringSentenceRows() {
        val bytes = xlsxBytes(
            "xl/worksheets/sheet1.xml" to """
                <worksheet>
                  <sheetData>
                    <row r="1">
                      <c r="A1" t="inlineStr"><is><t>sentence</t></is></c>
                      <c r="B1" t="inlineStr"><is><t>meaning</t></is></c>
                    </row>
                    <row r="2">
                      <c r="A2" t="inlineStr"><is><t>I might stay as well as join a tour</t></is></c>
                      <c r="B2" t="inlineStr"><is><t>ツアーに参加するよりも家にいるかもしれない</t></is></c>
                    </row>
                  </sheetData>
                </worksheet>
            """.trimIndent()
        )

        assertEquals(
            listOf(
                listOf("sentence", "meaning"),
                listOf("I might stay as well as join a tour", "ツアーに参加するよりも家にいるかもしれない")
            ),
            parseXlsxRows(bytes)
        )
    }

    @Test
    fun parseXlsxRows_readsSharedStringSentenceRows() {
        val bytes = xlsxBytes(
            "xl/sharedStrings.xml" to """
                <sst>
                  <si><t>sentence</t></si>
                  <si><t>meaning</t></si>
                  <si><t>I might stay as well as join a tour</t></si>
                  <si><t>ツアーに参加するよりも家にいるかもしれない</t></si>
                </sst>
            """.trimIndent(),
            "xl/worksheets/sheet1.xml" to """
                <worksheet>
                  <sheetData>
                    <row r="1">
                      <c r="A1" t="s"><v>0</v></c>
                      <c r="B1" t="s"><v>1</v></c>
                    </row>
                    <row r="2">
                      <c r="A2" t="s"><v>2</v></c>
                      <c r="B2" t="s"><v>3</v></c>
                    </row>
                  </sheetData>
                </worksheet>
            """.trimIndent()
        )

        assertEquals(
            listOf(
                listOf("sentence", "meaning"),
                listOf("I might stay as well as join a tour", "ツアーに参加するよりも家にいるかもしれない")
            ),
            parseXlsxRows(bytes)
        )
    }
    @Test
    fun toCsvText_escapesOnlyWhenCompatibilityPathNeedsCsvText() {
        assertEquals(
            "english,meaning\n\"check, confirm\",\"he said \"\"yes\"\"\"",
            listOf(
                listOf("english", "meaning"),
                listOf("check, confirm", "he said \"yes\"")
            ).toCsvText()
        )
    }

    @Test
    fun parseXlsxRows_rejectsOversizedSupportedEntry() {
        val oversizedSharedStrings = "x".repeat(MAX_XLSX_ENTRY_BYTES + 1)
        val bytes = xlsxBytes(
            "xl/sharedStrings.xml" to oversizedSharedStrings,
            "xl/worksheets/sheet1.xml" to "<worksheet><sheetData /></worksheet>"
        )

        assertThrows(IllegalArgumentException::class.java) {
            parseXlsxRows(bytes)
        }
    }

    private fun xlsxBytes(vararg entries: Pair<String, String>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }
}
