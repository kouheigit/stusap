package com.example.vocabapp

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

internal fun decodeCsvBytes(bytes: ByteArray): String {
    val withoutBom = if (bytes.startsWith(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))) {
        bytes.copyOfRange(3, bytes.size)
    } else {
        bytes
    }
    return runCatching { strictDecode(withoutBom, StandardCharsets.UTF_8) }
        .getOrElse { strictDecode(withoutBom, Charset.forName("MS932")) }
}

internal fun strictDecode(bytes: ByteArray, charset: Charset): String =
    charset.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(bytes))
        .toString()

internal fun ByteArray.startsWith(prefix: ByteArray): Boolean =
    size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
