package com.example.vocabapp.domain.usecase

import com.example.vocabapp.domain.model.PassageQuestion
import com.example.vocabapp.domain.model.PassageSet
import com.example.vocabapp.domain.usecase.passage.PassageScoreCalculator
import com.example.vocabapp.domain.usecase.passage.PassageSessionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PassageSessionEngineTest {
    private val engine = PassageSessionEngine(PassageScoreCalculator())

    private fun question(number: String, answerIndex: Int) = PassageQuestion(
        number = number,
        stem = "stem $number",
        options = listOf("a", "b", "c", "d"),
        answerIndex = answerIndex
    )

    private val set = PassageSet(
        id = "set-1",
        instruction = "read",
        documents = emptyList(),
        questions = listOf(question("1", 0), question("2", 1), question("3", 2)),
        timeLimitSec = 120
    )

    @Test
    fun start_initializesAtFirstQuestionWithEmptySelections() {
        val state = engine.start(set)

        assertEquals("set-1", state.setId)
        assertEquals(0, state.currentIndex)
        assertEquals(listOf(null, null, null), state.selections)
        assertEquals(120, state.remainingSec)
        assertFalse(state.finished)
        assertNull(state.score)
    }

    @Test
    fun start_usesDefaultTimeLimitWhenUnset() {
        val state = engine.start(set.copy(timeLimitSec = null))

        assertEquals(300, state.remainingSec)
    }

    @Test
    fun select_recordsChoiceForCurrentQuestionOnly() {
        val state = engine.select(engine.start(set), 2)

        assertEquals(listOf(2, null, null), state.selections)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun next_advancesWithoutFinishingUntilLastQuestion() {
        val state = engine.next(engine.start(set), set)

        assertEquals(1, state.currentIndex)
        assertFalse(state.finished)
        assertNull(state.score)
    }

    @Test
    fun next_onLastQuestion_finishesAndScores() {
        var state = engine.start(set)
        state = engine.select(state, 0) // correct
        state = engine.next(state, set)
        state = engine.select(state, 1) // correct
        state = engine.next(state, set)
        state = engine.select(state, 0) // wrong (answer is 2)
        state = engine.next(state, set)

        assertTrue(state.finished)
        assertEquals(3, state.score?.total)
        assertEquals(2, state.score?.correct)
        assertEquals(1, state.score?.wrong)
    }

    @Test
    fun selectAndNext_ignoredAfterFinish() {
        var state = engine.start(set)
        repeat(set.questions.size) { state = engine.next(state, set) }
        val finished = state

        assertTrue(finished.finished)
        assertEquals(finished, engine.select(finished, 0))
        assertEquals(finished, engine.next(finished, set))
    }

    @Test
    fun tick_decrementsRemainingSeconds() {
        val state = engine.tick(engine.start(set), set, seconds = 30)

        assertEquals(90, state.remainingSec)
        assertFalse(state.finished)
    }

    @Test
    fun tick_toZero_finishesAndScoresFromCurrentSelections() {
        var state = engine.start(set)
        state = engine.select(state, 0) // correct on Q1
        state = engine.tick(state, set, seconds = 120)

        assertEquals(0, state.remainingSec)
        assertTrue(state.finished)
        assertEquals(1, state.score?.correct)
        assertEquals(2, state.score?.wrong)
    }
}
