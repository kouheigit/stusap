package com.example.vocabapp.data.repository

import com.example.vocabapp.domain.model.ImportedSentence
import com.example.vocabapp.domain.model.SentenceImportPreview
import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceImportModelTest {

    @Test
    fun quizReadyCount_countsOnlyQuizReadySentences() {
        val preview = SentenceImportPreview(
            newSentences = listOf(
                ImportedSentence("one two three four five six", "六語文", isQuizReady = true),
                ImportedSentence("too short", "短すぎ", isQuizReady = false),
                ImportedSentence("also six words here right now", "六語文2", isQuizReady = true)
            )
        )
        assertEquals(2, preview.quizReadyCount)
    }

    @Test
    fun quizIncompatibleCount_countsNonReadySentences() {
        val preview = SentenceImportPreview(
            newSentences = listOf(
                ImportedSentence("one two three four five six", "六語文", isQuizReady = true),
                ImportedSentence("too short", "短すぎ", isQuizReady = false)
            )
        )
        assertEquals(1, preview.quizIncompatibleCount)
    }

    @Test
    fun newCount_returnsNewSentencesSize() {
        val preview = SentenceImportPreview(
            newSentences = listOf(
                ImportedSentence("sentence one two three four five", "意味1", isQuizReady = true),
                ImportedSentence("sentence two two three four five", "意味2", isQuizReady = true)
            )
        )
        assertEquals(2, preview.newCount)
    }

    @Test
    fun duplicateCount_includesOmittedDuplicates() {
        val preview = SentenceImportPreview(
            duplicateSentences = listOf(
                ImportedSentence("dup one two three four five", "重複1", isQuizReady = true)
            ),
            omittedDuplicateCount = 5
        )
        assertEquals(6, preview.duplicateCount)
    }

    @Test
    fun errorCount_includesOmittedErrors() {
        val preview = SentenceImportPreview(
            errors = listOf(
                com.example.vocabapp.domain.model.ImportErrorRow(2, "エラー", emptyList())
            ),
            omittedErrorCount = 3
        )
        assertEquals(4, preview.errorCount)
    }

    @Test
    fun sentenceImportResult_defaultQuizReadyInsertedCount_isZero() {
        val result = com.example.vocabapp.domain.model.SentenceImportResult(
            totalRows = 5,
            insertedCount = 4,
            duplicateCount = 1,
            errorCount = 0
        )
        assertEquals(0, result.quizReadyInsertedCount)
    }

    @Test
    fun sentenceImportResult_withQuizReadyCount_tracked() {
        val result = com.example.vocabapp.domain.model.SentenceImportResult(
            totalRows = 5,
            insertedCount = 4,
            duplicateCount = 1,
            errorCount = 0,
            quizReadyInsertedCount = 3
        )
        assertEquals(3, result.quizReadyInsertedCount)
    }

    @Test
    fun quizCompatiblePercent_allReady_returns100() {
        val preview = SentenceImportPreview(
            newSentences = listOf(
                ImportedSentence("one two three four five six", "意味", isQuizReady = true),
                ImportedSentence("two three four five six seven", "意味2", isQuizReady = true)
            )
        )
        assertEquals(100, preview.quizCompatiblePercent)
    }

    @Test
    fun quizCompatiblePercent_noneReady_returns0() {
        val preview = SentenceImportPreview(
            newSentences = listOf(
                ImportedSentence("too short", "短い", isQuizReady = false)
            )
        )
        assertEquals(0, preview.quizCompatiblePercent)
    }

    @Test
    fun quizCompatiblePercent_halfReady_returns50() {
        val preview = SentenceImportPreview(
            newSentences = listOf(
                ImportedSentence("one two three four five six", "意味", isQuizReady = true),
                ImportedSentence("too short", "短い", isQuizReady = false)
            )
        )
        assertEquals(50, preview.quizCompatiblePercent)
    }

    @Test
    fun quizCompatiblePercent_emptyNewSentences_returns0() {
        val preview = SentenceImportPreview(newSentences = emptyList())
        assertEquals(0, preview.quizCompatiblePercent)
    }
}
