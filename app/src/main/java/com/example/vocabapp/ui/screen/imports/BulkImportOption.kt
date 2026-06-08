package com.example.vocabapp

import com.example.vocabapp.ui.navigation.Route

internal enum class BulkImportKind {
    Word,
    Idiom,
    Sentence
}

internal data class BulkImportOption(
    val kind: BulkImportKind,
    val route: String
)

internal fun bulkImportOptions(): List<BulkImportOption> =
    listOf(
        BulkImportOption(BulkImportKind.Word, Route.WordImport.path),
        BulkImportOption(BulkImportKind.Idiom, Route.IdiomImport.path),
        BulkImportOption(BulkImportKind.Sentence, Route.SentenceImport.path)
    )
