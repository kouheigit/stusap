package com.example.vocabapp

import com.example.vocabapp.ui.navigation.Route

internal enum class BulkImportKind {
    WordIdiom,
    Sentence
}

internal data class BulkImportOption(
    val kind: BulkImportKind,
    val route: String
)

internal fun bulkImportOptions(): List<BulkImportOption> =
    listOf(
        BulkImportOption(BulkImportKind.WordIdiom, Route.WordImport.path),
        BulkImportOption(BulkImportKind.Sentence, Route.SentenceImport.path)
    )
