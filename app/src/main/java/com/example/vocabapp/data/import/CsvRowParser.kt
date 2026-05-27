package com.example.vocabapp

import java.io.Reader
import java.io.PushbackReader
import java.io.StringReader

internal fun parseCsvRows(input: String): List<List<String>> {
    return parseCsvRows(StringReader(input))
}

internal fun parseCsvRows(reader: Reader): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val currentRow = mutableListOf<String>()
    val currentCell = StringBuilder()
    var inQuotes = false
    var pendingCarriageReturn = false
    var sawAnyChar = false
    val input = PushbackReader(reader, 1)

    fun finishCell() {
        if (currentRow.size + 1 > MAX_IMPORT_COLUMNS) {
            throw IllegalArgumentException("列数は${MAX_IMPORT_COLUMNS}列以内にしてください")
        }
        currentRow += currentCell.toString()
        currentCell.clear()
    }

    fun finishRow() {
        finishCell()
        if (currentRow.any { it.isNotBlank() }) {
            if (rows.size >= MAX_IMPORT_ROWS + 1) {
                throw IllegalArgumentException("読み込み行数は${MAX_IMPORT_ROWS}件以内にしてください")
            }
            rows += currentRow.toList()
        }
        currentRow.clear()
    }

    fun appendCellChar(char: Char) {
        if (currentCell.length >= MAX_IMPORT_CELL_CHARS) {
            throw IllegalArgumentException("セルは${MAX_IMPORT_CELL_CHARS}文字以内にしてください")
        }
        currentCell.append(char)
    }

    fun consume(char: Char) {
        when {
            char == '"' -> inQuotes = !inQuotes
            char == ',' && !inQuotes -> {
                finishCell()
            }
            (char == '\n' || char == '\r') && !inQuotes -> {
                finishRow()
            }
            else -> {
                appendCellChar(char)
            }
        }
    }

    while (true) {
        val read = input.read()
        if (read == -1) break
        val char = read.toChar()
        sawAnyChar = true
        if (pendingCarriageReturn) {
            pendingCarriageReturn = false
            if (char == '\n') continue
        }
        if (char == '"' && inQuotes) {
            val next = input.read()
            if (next == '"'.code) {
                appendCellChar('"')
            } else {
                if (next != -1) input.unread(next)
                inQuotes = false
            }
            continue
        }
        if (char == '\r' && !inQuotes) {
            pendingCarriageReturn = true
            consume(char)
        } else {
            consume(char)
        }
    }

    if (!sawAnyChar) return emptyList()
    if (currentRow.isNotEmpty() || currentCell.isNotEmpty()) {
        finishRow()
    }
    return rows
}
