package com.example.vocabapp

import java.util.Locale
import org.xmlpull.v1.XmlPullParser

internal fun parseSharedStrings(bytes: ByteArray): List<String> {
    debugImportLog("parseSharedStrings: parsing")
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
                XmlPullParser.TEXT -> if (insideSi) current.appendLimited(parser.text)
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    if (tag == "si") {
                        if (values.size >= MAX_IMPORT_ROWS * MAX_IMPORT_COLUMNS) {
                            throw IllegalArgumentException("Excelファイルの文字列数が上限を超えています")
                        }
                        values += current.toString()
                        insideSi = false
                    }
                }
            }
        }
    } catch (e: IllegalArgumentException) {
        throw e
    } catch (e: Exception) {
        errorImportLog("parseSharedStrings: parse error")
        throw IllegalArgumentException("Excelファイルの文字列情報を読み込めませんでした", e)
    }
    debugImportLog("parseSharedStrings: parsed ${values.size} shared strings")
    return values
}

internal fun parseWorksheetRows(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
    debugImportLog("parseWorksheetRows: parsing")
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
                        "row" -> {
                            if (rows.size >= MAX_IMPORT_ROWS + 1) {
                                throw IllegalArgumentException("読み込み行数は${MAX_IMPORT_ROWS}件以内にしてください")
                            }
                            currentRow = mutableListOf()
                        }
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
                    if (readingValue || readingInlineText) cellValue.appendLimited(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    when (tag) {
                        "v" -> readingValue = false
                        "t" -> readingInlineText = false
                        "c" -> {
                            currentRow?.let { row ->
                                val columnIndex = xlsxColumnIndex(cellReference)
                                if (columnIndex >= MAX_IMPORT_COLUMNS) {
                                    throw IllegalArgumentException("列数は${MAX_IMPORT_COLUMNS}列以内にしてください")
                                }
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
    } catch (e: IllegalArgumentException) {
        throw e
    } catch (e: Exception) {
        errorImportLog("parseWorksheetRows: parse error")
        throw IllegalArgumentException("Excelシートの解析に失敗しました", e)
    }
    debugImportLog("parseWorksheetRows: parsed ${rows.size} rows")
    return rows
}

private fun StringBuilder.appendLimited(text: String) {
    if (length + text.length > MAX_IMPORT_CELL_CHARS) {
        throw IllegalArgumentException("セルは${MAX_IMPORT_CELL_CHARS}文字以内にしてください")
    }
    append(text)
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
