package com.example.vocabapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomTrainingTargetTest {

    @Test
    fun customTrainingTargets_setOneUsesImportedRowsOneToTen() {
        val words = studyWords(25)

        val targets = words.customTrainingTargets(setNumber = 1)

        assertEquals((1..10).map { "word$it" }, targets.map { it.english })
    }

    @Test
    fun customTrainingTargets_setTwoUsesImportedRowsElevenToTwenty() {
        val words = studyWords(25)

        val targets = words.customTrainingTargets(setNumber = 2)

        assertEquals((11..20).map { "word$it" }, targets.map { it.english })
    }

    @Test
    fun customTrainingTargets_setThreeUsesRemainingRowsOnly() {
        val words = studyWords(25)

        val targets = words.customTrainingTargets(setNumber = 3)

        assertEquals((21..25).map { "word$it" }, targets.map { it.english })
    }

    @Test
    fun customTrainingTargets_setOneDoesNotUseNewestRowsFromHundredItems() {
        val words = studyWords(100)

        val targets = words.customTrainingTargets(setNumber = 1)

        assertEquals((1..10).map { "word$it" }, targets.map { it.english })
    }

    @Test
    fun customTrainingTargets_setTenUsesRowsNinetyOneToOneHundred() {
        val words = studyWords(100)

        val targets = words.customTrainingTargets(setNumber = 10)

        assertEquals((91..100).map { "word$it" }, targets.map { it.english })
    }

    @Test
    fun customTrainingTargets_outOfRangeSetReturnsEmptyList() {
        val words = studyWords(12)

        val targets = words.customTrainingTargets(setNumber = 3)

        assertTrue(targets.isEmpty())
    }

    @Test
    fun customTrainingStartIndex_clampsInvalidSetNumberToFirstSet() {
        assertEquals(0, customTrainingStartIndex(setNumber = 0))
        assertEquals(0, customTrainingStartIndex(setNumber = -1))
    }

    private fun studyWords(count: Int): List<CustomStudyWord> =
        (1..count).map { index ->
            CustomStudyWord(
                id = index,
                english = "word$index",
                meaning = "meaning$index",
                exampleSentence = "",
                exampleTranslation = ""
            )
        }
}
