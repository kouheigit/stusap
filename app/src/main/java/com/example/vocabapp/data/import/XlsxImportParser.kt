package com.example.vocabapp

import java.io.ByteArrayInputStream
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
    debugImportLog("parseXlsxRows: start, bytes=${bytes.size}")
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
                debugImportLog("parseXlsxRows: ZIP entry name=$name isDir=${entry.isDirectory}")
                var entryBytes: ByteArray? = null
                if (!entry.isDirectory && name in XLSX_TARGET_ENTRIES) {
                    try {
                        entryBytes = zip.readBytesWithLimit(MAX_XLSX_ENTRY_BYTES)
                    } catch (e: Exception) {
                        warnImportLog("parseXlsxRows: readBytes error on '$name': ${e.javaClass.simpleName}: ${e.message}")
                    }
                }
                if (entryBytes != null) {
                    entries[name] = entryBytes
                    debugImportLog("parseXlsxRows: captured $name (${entryBytes.size} bytes)")
                }
                try {
                    zip.closeEntry()
                } catch (e: java.util.zip.ZipException) {
                    warnImportLog("parseXlsxRows: closeEntry ZipException on '$name': ${e.message} (ignoring CRC issue)")
                } catch (e: Exception) {
                    warnImportLog("parseXlsxRows: closeEntry error on '$name': ${e.javaClass.simpleName}: ${e.message}")
                }
                entry = try { zip.nextEntry } catch (e: java.util.zip.ZipException) {
                    warnImportLog("parseXlsxRows: ZipException advancing to next entry: ${e.message}")
                    break
                }
            }
        }
    } catch (e: java.util.zip.ZipException) {
        errorImportLog("parseXlsxRows: fatal ZipException reading XLSX ZIP: ${e.message}")
        if (entries.isEmpty()) error("ZIPファイルの読み込みに失敗しました: ${e.message}")
    }
    debugImportLog("parseXlsxRows: captured entries=${entries.keys}")

    val sheetPath = parseWorkbookSheetPath(entries["xl/_rels/workbook.xml.rels"])
        ?: "xl/worksheets/sheet1.xml"
    debugImportLog("parseXlsxRows: resolved sheetPath=$sheetPath")

    if (!entries.containsKey(sheetPath) && sheetPath != "xl/worksheets/sheet1.xml") {
        debugImportLog("parseXlsxRows: sheet at $sheetPath not captured, re-scanning ZIP")
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null && !entries.containsKey(sheetPath)) {
                    if (!entry.isDirectory && entry.name == sheetPath) {
                        entries[sheetPath] = zip.readBytesWithLimit(MAX_XLSX_ENTRY_BYTES)
                        debugImportLog("parseXlsxRows: re-scan captured $sheetPath")
                    }
                    try { zip.closeEntry() } catch (e: java.util.zip.ZipException) {
                        warnImportLog("parseXlsxRows: re-scan closeEntry error: ${e.message}")
                    }
                    entry = try { zip.nextEntry } catch (e: java.util.zip.ZipException) { break }
                }
            }
        } catch (e: Exception) {
            warnImportLog("parseXlsxRows: re-scan failed: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    val sheet = entries[sheetPath]
        ?: error("Excelファイルの1枚目のシートを読み込めませんでした (パス: $sheetPath, 取得済みエントリ: ${entries.keys})")
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
