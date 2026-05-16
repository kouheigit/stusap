package com.example.vocabapp.domain.usecase

import javax.inject.Inject

data class QuizScore(
    val total: Int,
    val correct: Int,
    val wrong: Int,
    val accuracy: Float,
    val studySeconds: Int,
    val starCount: Int
)

class QuizScoreCalculator @Inject constructor() {
    fun calculate(
        startedAt: Long,
        finishedAt: Long,
        correctCount: Int,
        answeredCount: Int
    ): QuizScore {
        val total = answeredCount.coerceAtLeast(1)
        val correct = correctCount.coerceIn(0, total)
        val accuracy = correct * 100f / total
        return QuizScore(
            total = total,
            correct = correct,
            wrong = total - correct,
            accuracy = accuracy,
            studySeconds = ((finishedAt - startedAt) / 1000).toInt().coerceAtLeast(1),
            starCount = starCountFor(accuracy)
        )
    }

    private fun starCountFor(accuracy: Float): Int = when {
        accuracy >= 90f -> 3
        accuracy >= 70f -> 2
        accuracy >= 50f -> 1
        else -> 0
    }
}
