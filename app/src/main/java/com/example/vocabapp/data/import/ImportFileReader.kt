package com.example.vocabapp

import java.io.InputStream

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
