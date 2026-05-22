package com.example.vocabapp

import com.example.vocabapp.util.debugImportLog
import com.example.vocabapp.util.errorImportLog
import com.example.vocabapp.util.warnImportLog

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

internal val XLSX_TARGET_ENTRIES = setOf(
    "xl/sharedStrings.xml",
    "xl/worksheets/sheet1.xml",
    "xl/_rels/workbook.xml.rels",
    "xl/workbook.xml"
)

internal fun parseXlsxRows(bytes: ByteArray): List<List<String>> {
    debugImportLog("parseXlsxRows: start")
    if (bytes.size < 4 || !bytes.startsWith(byteArrayOf(0x50, 0x4B, 0x03, 0x04))) {
        errorImportLog("parseXlsxRows: not a valid ZIP/XLSX file (magic bytes mismatch)")
        error("選択されたファイルは有効なExcelファイル(.xlsx)ではありません。ファイル形式を確認してください。")
    }
    val entries = mutableMapOf<String, ByteArray>()
    try {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name
                debugImportLog("parseXlsxRows: scanning ZIP entry")
                var entryBytes: ByteArray? = null
                if (!entry.isDirectory && name in XLSX_TARGET_ENTRIES) {
                    try {
                        entryBytes = zip.readBytesWithLimit(MAX_XLSX_ENTRY_BYTES)
                    } catch (e: IOException) {
                        warnImportLog("parseXlsxRows: ZIP entry read failed")
                    }
                }
                if (entryBytes != null) {
                    entries[name] = entryBytes
                    debugImportLog("parseXlsxRows: captured supported ZIP entry")
                }
                try {
                    zip.closeEntry()
                } catch (e: java.util.zip.ZipException) {
                    warnImportLog("parseXlsxRows: ZIP entry close failed")
                } catch (e: Exception) {
                    warnImportLog("parseXlsxRows: ZIP entry close failed")
                }
                entry = try { zip.nextEntry } catch (e: java.util.zip.ZipException) {
                    warnImportLog("parseXlsxRows: ZIP entry iteration failed")
                    break
                }
            }
        }
    } catch (e: java.util.zip.ZipException) {
        errorImportLog("parseXlsxRows: fatal ZIP read error")
        if (entries.isEmpty()) error("ZIPファイルの読み込みに失敗しました")
    }
    debugImportLog("parseXlsxRows: supported entries captured")

    val sheetPath = parseWorkbookSheetPath(entries["xl/_rels/workbook.xml.rels"])
        ?: "xl/worksheets/sheet1.xml"
    debugImportLog("parseXlsxRows: worksheet path resolved")

    if (!entries.containsKey(sheetPath) && sheetPath != "xl/worksheets/sheet1.xml") {
        debugImportLog("parseXlsxRows: re-scanning ZIP for worksheet")
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null && !entries.containsKey(sheetPath)) {
                    if (!entry.isDirectory && entry.name == sheetPath) {
                        try {
                            entries[sheetPath] = zip.readBytesWithLimit(MAX_XLSX_ENTRY_BYTES)
                            debugImportLog("parseXlsxRows: re-scan captured worksheet")
                        } catch (e: IOException) {
                            warnImportLog("parseXlsxRows: re-scan worksheet read failed")
                        }
                    }
                    try { zip.closeEntry() } catch (e: java.util.zip.ZipException) {
                        warnImportLog("parseXlsxRows: re-scan closeEntry failed")
                    }
                    entry = try { zip.nextEntry } catch (e: java.util.zip.ZipException) { break }
                }
            }
        } catch (e: IOException) {
            warnImportLog("parseXlsxRows: re-scan failed")
        }
    }

    val sheet = entries[sheetPath]
        ?: error("Excelファイルの1枚目のシートを読み込めませんでした")
    val sharedStrings = entries["xl/sharedStrings.xml"]?.let(::parseSharedStrings).orEmpty()
    debugImportLog("parseXlsxRows: sharedStrings.size=${sharedStrings.size}")
    return parseWorksheetRows(sheet, sharedStrings)
}

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
                    debugImportLog("parseWorkbookSheetPath: worksheet relationship found")
                    if (type.endsWith("/worksheet") && target.isNotBlank()) {
                        val resolved = if (target.startsWith("/")) {
                            target.trimStart('/')
                        } else {
                            "xl/$target"
                        }
                        debugImportLog("parseWorkbookSheetPath: worksheet path resolved")
                        return resolved
                    }
                }
            }
        }
        warnImportLog("parseWorkbookSheetPath: no worksheet relationship found")
        null
    } catch (e: Exception) {
        warnImportLog("parseWorkbookSheetPath: failed to parse workbook relationships")
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

internal fun List<List<String>>.toCsvText(): String {
    debugImportLog("toCsvText: converting ${size} rows to CSV")
    return joinToString("\n") { row ->
        row.joinToString(",") { cell ->
            val normalized = cell.replace("\r\n", " ").replace("\r", " ").replace("\n", " ")
            val escaped = normalized.replace("\"", "\"\"")
            if (escaped.any { it == ',' || it == '"' }) "\"$escaped\"" else escaped
        }
    }.also { debugImportLog("toCsvText: result length=${it.length}") }
}
