package com.example.vocabapp

import com.example.vocabapp.util.debugImportLog

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream
import java.util.Locale

internal fun Context.readImportFileAsCsv(uri: Uri): String {
    debugImportLog("readImportFileAsCsv: start")
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytesWithLimit(MAX_IMPORT_FILE_BYTES) }
        ?: error("ファイルを開けませんでした")
    if (bytes.isEmpty()) error("ファイルが空です")

    val fileName = queryDisplayName(uri).lowercase(Locale.ROOT)
    val mimeType = contentResolver.getType(uri).orEmpty().lowercase(Locale.ROOT)
    debugImportLog("readImportFileAsCsv: file metadata checked")

    val isZipMagic = bytes.startsWith(byteArrayOf(0x50, 0x4B, 0x03, 0x04))
    val isOle2Magic = bytes.startsWith(byteArrayOf(0xD0.toByte(), 0xCF.toByte(), 0x11, 0xE0.toByte()))
    val isXlsx = isZipMagic ||
        fileName.endsWith(".xlsx") ||
        mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    val isOldXls = isOle2Magic ||
        (!isZipMagic && fileName.endsWith(".xls")) ||
        (!isZipMagic && mimeType == "application/vnd.ms-excel")
    debugImportLog("readImportFileAsCsv: file type resolved")

    if (isOldXls && !isXlsx) {
        error("古い .xls 形式は未対応です。Excelで .xlsx または CSV として保存してから選択してください")
    }

    return if (isXlsx) {
        debugImportLog("readImportFileAsCsv: parsing as XLSX")
        parseXlsxRows(bytes).toCsvText()
    } else {
        debugImportLog("readImportFileAsCsv: parsing as CSV")
        decodeCsvBytes(bytes)
    }
}

internal fun Context.queryDisplayName(uri: Uri): String {
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return cursor.getString(index).orEmpty().sanitizeDisplayName()
        }
    }
    return uri.lastPathSegment.orEmpty().sanitizeDisplayName()
}

internal fun String.sanitizeDisplayName(): String =
    filterNot { it.isISOControl() }.take(128)

internal fun InputStream.readBytesWithLimit(maxBytes: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var total = 0
    while (true) {
        val read = read(buffer)
        if (read == -1) break
        total += read
        if (total > maxBytes) {
            error("ファイルサイズが上限を超えています")
        }
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}
