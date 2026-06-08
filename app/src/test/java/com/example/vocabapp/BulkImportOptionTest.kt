package com.example.vocabapp

import com.example.vocabapp.ui.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Test

class BulkImportOptionTest {

    @Test
    fun bulkImportOptions_includeWordIdiomAndSentenceDestinations() {
        val options = bulkImportOptions()

        assertEquals(
            listOf(BulkImportKind.Word, BulkImportKind.Idiom, BulkImportKind.Sentence),
            options.map { it.kind }
        )
        assertEquals(
            listOf(Route.WordImport.path, Route.IdiomImport.path, Route.SentenceImport.path),
            options.map { it.route }
        )
    }
}
