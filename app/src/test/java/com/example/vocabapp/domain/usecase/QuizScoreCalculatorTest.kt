package com.example.vocabapp.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizScoreCalculatorTest {
    private val calculator = QuizScoreCalculator()

    @Test
    fun calculate_clampsCorrectCountAndComputesWrongCount() {
        val score = calculator.calculate(
            startedAt = 1_000L,
            finishedAt = 11_000L,
            correctCount = 12,
            answeredCount = 10
        )

        assertEquals(10, score.total)
        assertEquals(10, score.correct)
        assertEquals(0, score.wrong)
        assertEquals(100f, score.accuracy)
        assertEquals(10, score.studySeconds)
        assertEquals(3, score.starCount)
    }

    @Test
    fun calculate_usesOneQuestionMinimumForEmptyAnswers() {
        val score = calculator.calculate(
            startedAt = 1_000L,
            finishedAt = 1_200L,
            correctCount = 0,
            answeredCount = 0
        )

        assertEquals(1, score.total)
        assertEquals(0, score.correct)
        assertEquals(1, score.wrong)
        assertEquals(0f, score.accuracy)
        assertEquals(1, score.studySeconds)
        assertEquals(0, score.starCount)
    }

    @Test
    fun calculate_assignsExpectedStarThresholds() {
        assertEquals(1, calculator.calculate(0L, 1_000L, 1, 2).starCount)
        assertEquals(2, calculator.calculate(0L, 1_000L, 7, 10).starCount)
        assertEquals(3, calculator.calculate(0L, 1_000L, 9, 10).starCount)
    }
}
