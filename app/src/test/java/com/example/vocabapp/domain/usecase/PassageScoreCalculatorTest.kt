package com.example.vocabapp.domain.usecase

import com.example.vocabapp.domain.model.PassageQuestion
import com.example.vocabapp.domain.usecase.passage.PassageScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class PassageScoreCalculatorTest {
    private val calculator = PassageScoreCalculator()

    private fun question(number: String, answerIndex: Int) = PassageQuestion(
        number = number,
        stem = "stem $number",
        options = listOf("a", "b", "c", "d"),
        answerIndex = answerIndex
    )

    private val questions = listOf(
        question("1", 0),
        question("2", 1),
        question("3", 2),
        question("4", 3)
    )

    @Test
    fun calculate_allCorrect_is100Percent() {
        val score = calculator.calculate(questions, listOf(0, 1, 2, 3))

        assertEquals(4, score.total)
        assertEquals(4, score.correct)
        assertEquals(0, score.wrong)
        assertEquals(100f, score.accuracy)
    }

    @Test
    fun calculate_allWrong_isZeroPercent() {
        val score = calculator.calculate(questions, listOf(1, 2, 3, 0))

        assertEquals(0, score.correct)
        assertEquals(4, score.wrong)
        assertEquals(0f, score.accuracy)
    }

    @Test
    fun calculate_partial_computesAccuracy() {
        val score = calculator.calculate(questions, listOf(0, 1, 9, 9))

        assertEquals(2, score.correct)
        assertEquals(2, score.wrong)
        assertEquals(50f, score.accuracy)
    }

    @Test
    fun calculate_unansweredCountsAsWrong() {
        val score = calculator.calculate(questions, listOf(0, null, null, 3))

        assertEquals(2, score.correct)
        assertEquals(2, score.wrong)
    }

    @Test
    fun calculate_missingSelectionsTreatedAsUnanswered() {
        val score = calculator.calculate(questions, listOf(0, 1))

        assertEquals(2, score.correct)
        assertEquals(2, score.wrong)
    }

    @Test
    fun calculate_emptyQuestions_isZeroAccuracyNotDivideByZero() {
        val score = calculator.calculate(emptyList(), emptyList())

        assertEquals(0, score.total)
        assertEquals(0f, score.accuracy)
    }
}
