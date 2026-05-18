package com.example.vocabapp

internal fun parseCsvRows(input: String): List<List<String>> {
    if (input.isEmpty()) return emptyList()
    val rows = mutableListOf<List<String>>()
    val currentRow = mutableListOf<String>()
    val currentCell = StringBuilder()
    var inQuotes = false
    var index = 0

    while (index < input.length) {
        val char = input[index]
        when {
            char == '"' && inQuotes && index + 1 < input.length && input[index + 1] == '"' -> {
                if (currentCell.length >= MAX_IMPORT_CELL_CHARS) {
                    throw IllegalArgumentException("セルは${MAX_IMPORT_CELL_CHARS}文字以内にしてください")
                }
                currentCell.append('"')
                index++
            }
            char == '"' -> inQuotes = !inQuotes
            char == ',' && !inQuotes -> {
                if (currentRow.size + 1 > MAX_IMPORT_COLUMNS) {
                    throw IllegalArgumentException("列数は${MAX_IMPORT_COLUMNS}列以内にしてください")
                }
                currentRow += currentCell.toString()
                currentCell.clear()
            }
            (char == '\n' || char == '\r') && !inQuotes -> {
                if (currentRow.size + 1 > MAX_IMPORT_COLUMNS) {
                    throw IllegalArgumentException("列数は${MAX_IMPORT_COLUMNS}列以内にしてください")
                }
                currentRow += currentCell.toString()
                currentCell.clear()
                if (currentRow.any { it.isNotBlank() }) {
                    if (rows.size >= MAX_IMPORT_ROWS + 1) {
                        throw IllegalArgumentException("読み込み行数は${MAX_IMPORT_ROWS}件以内にしてください")
                    }
                    rows += currentRow.toList()
                }
                currentRow.clear()
                if (char == '\r' && index + 1 < input.length && input[index + 1] == '\n') {
                    index++
                }
            }
            else -> {
                if (currentCell.length >= MAX_IMPORT_CELL_CHARS) {
                    throw IllegalArgumentException("セルは${MAX_IMPORT_CELL_CHARS}文字以内にしてください")
                }
                currentCell.append(char)
            }
        }
        index++
    }

    if (currentRow.size + 1 > MAX_IMPORT_COLUMNS) {
        throw IllegalArgumentException("列数は${MAX_IMPORT_COLUMNS}列以内にしてください")
    }
    currentRow += currentCell.toString()
    if (currentRow.any { it.isNotBlank() }) {
        if (rows.size >= MAX_IMPORT_ROWS + 1) {
            throw IllegalArgumentException("読み込み行数は${MAX_IMPORT_ROWS}件以内にしてください")
        }
        rows += currentRow.toList()
    }
    return rows
}
