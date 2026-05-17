package com.example.vocabapp

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Locale
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

internal fun parseWorkbookSheetPath(relsBytes: ByteArray?): String? {
    if (relsBytes == null) {
        debugImportLog("parseWorkbookSheetPath: no workbook.xml.rels found, using default")
        return null
    }
    return try {
        val parser = newXmlParser(relsBytes)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                val localName = parser.name.substringAfterLast(':')
                if (localName == "Relationship") {
                    val type = parser.getAttributeValue(null, "Type").orEmpty()
                    val target = parser.getAttributeValue(null, "Target").orEmpty()
                    debugImportLog("parseWorkbookSheetPath: Relationship type=$type target=$target")
                    if (type.endsWith("/worksheet") && target.isNotBlank()) {
                        val resolved = if (target.startsWith("/")) {
                            target.trimStart('/')
                        } else {
                            "xl/$target"
                        }
                        debugImportLog("parseWorkbookSheetPath: resolved sheet path=$resolved")
                        return resolved
                    }
                }
            }
        }
        warnImportLog("parseWorkbookSheetPath: no worksheet relationship found in workbook.xml.rels")
        null
    } catch (e: Exception) {
        warnImportLog("parseWorkbookSheetPath: failed to parse workbook relationships: ${e.javaClass.simpleName}: ${e.message}")
        null
    }
}

internal fun newXmlParser(bytes: ByteArray): XmlPullParser {
    val factory = XmlPullParserFactory.newInstance().apply {
        isNamespaceAware = false
    }
    return factory.newPullParser().apply {
        setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        setInput(InputStreamReader(ByteArrayInputStream(bytes), StandardCharsets.UTF_8))
    }
}

internal fun parseSharedStrings(bytes: ByteArray): List<String> {
    debugImportLog("parseSharedStrings: parsing ${bytes.size} bytes")
    val parser = newXmlParser(bytes)
    val values = mutableListOf<String>()
    var insideSi = false
    var current = StringBuilder()

    try {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    if (tag == "si") {
                        insideSi = true
                        current = StringBuilder()
                    }
                }
                XmlPullParser.TEXT -> if (insideSi) current.append(parser.text)
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    if (tag == "si") {
                        values += current.toString()
                        insideSi = false
                    }
                }
            }
        }
    } catch (e: Exception) {
        errorImportLog("parseSharedStrings: parse error after ${values.size} entries: ${e.javaClass.simpleName}: ${e.message}")
    }
    debugImportLog("parseSharedStrings: parsed ${values.size} shared strings")
    return values
}

internal fun parseWorksheetRows(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
    debugImportLog("parseWorksheetRows: parsing ${bytes.size} bytes, sharedStrings=${sharedStrings.size}")
    val parser = newXmlParser(bytes)
    val rows = mutableListOf<List<String>>()
    var currentRow: MutableList<String>? = null
    var cellReference = ""
    var cellType = ""
    var cellValue = StringBuilder()
    var readingValue = false
    var readingInlineText = false

    try {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    when (tag) {
                        "row" -> currentRow = mutableListOf()
                        "c" -> {
                            cellReference = parser.getAttributeValue(null, "r").orEmpty()
                            cellType = parser.getAttributeValue(null, "t").orEmpty()
                            cellValue = StringBuilder()
                        }
                        "v" -> readingValue = true
                        "t" -> if (cellType == "inlineStr") readingInlineText = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (readingValue || readingInlineText) cellValue.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    when (tag) {
                        "v" -> readingValue = false
                        "t" -> readingInlineText = false
                        "c" -> {
                            currentRow?.let { row ->
                                val columnIndex = xlsxColumnIndex(cellReference)
                                while (row.size < columnIndex) row += ""
                                row += resolveXlsxCellValue(cellValue.toString(), cellType, sharedStrings)
                            }
                        }
                        "row" -> {
                            currentRow?.dropLastWhile { it.isBlank() }
                                ?.takeIf { row -> row.any { it.isNotBlank() } }
                                ?.let(rows::add)
                            currentRow = null
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        errorImportLog("parseWorksheetRows: parse error after ${rows.size} rows: ${e.javaClass.simpleName}: ${e.message}")
    }
    debugImportLog("parseWorksheetRows: parsed ${rows.size} rows")
    return rows
}

internal fun resolveXlsxCellValue(rawValue: String, type: String, sharedStrings: List<String>): String {
    val resolved = when (type) {
        "s" -> {
            val idx = rawValue.toIntOrNull()
            if (idx == null) {
                warnImportLog("resolveXlsxCellValue: shared string index not an int: '$rawValue'")
                ""
            } else {
                sharedStrings.getOrNull(idx).also {
                    if (it == null) warnImportLog("resolveXlsxCellValue: shared string index $idx out of range (size=${sharedStrings.size})")
                }.orEmpty()
            }
        }
        "b" -> if (rawValue == "1") "TRUE" else "FALSE"
        "e" -> ""
        else -> rawValue
    }.trim()
    return resolved
}

internal fun xlsxColumnIndex(reference: String): Int {
    if (reference.isBlank()) {
        warnImportLog("xlsxColumnIndex: empty cell reference, defaulting to column 0")
        return 0
    }
    var result = 0
    val letters = reference.takeWhile { it.isLetter() }.uppercase(Locale.ROOT)
    if (letters.isEmpty()) {
        warnImportLog("xlsxColumnIndex: no letter prefix in reference '$reference', defaulting to column 0")
        return 0
    }
    letters.forEach { char ->
        result = result * 26 + (char - 'A' + 1)
    }
    return maxOf(result - 1, 0)
}
