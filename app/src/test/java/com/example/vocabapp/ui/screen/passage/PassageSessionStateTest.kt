package com.example.vocabapp.ui.screen.passage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PassageSessionStateTest {

    private fun state(selections: List<Int?>) = PassageSessionState(
        setId = "set",
        currentIndex = 0,
        selections = selections,
        remainingSec = 0,
        finished = false,
        score = 0
    )

    @Test
    fun answeredFlags_marksOnlyAnsweredQuestions() {
        val result = state(listOf(1, null, 3)).answeredFlags

        assertEquals(listOf(true, false, true), result)
    }

    @Test
    fun allAnswered_isFalseWhenAnySelectionIsNull() {
        assertFalse(state(listOf(0, null, 2)).allAnswered)
    }

    @Test
    fun allAnswered_isTrueWhenEverySelectionIsSet() {
        assertTrue(state(listOf(0, 1, 2)).allAnswered)
    }

    @Test
    fun allAnswered_isFalseForEmptySelections() {
        assertFalse(state(emptyList()).allAnswered)
    }

    @Test
    fun selectedIndex_followsCurrentIndex() {
        val current = PassageSessionState(
            setId = "set",
            currentIndex = 2,
            selections = listOf(0, 1, 3),
            remainingSec = 0,
            finished = false,
            score = 0
        )

        assertEquals(3, current.selectedIndex)
        assertTrue(current.isCurrentAnswered)
    }
}
