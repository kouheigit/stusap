package com.example.vocabapp.domain.usecase.quiz

import javax.inject.Inject

/**
 * クイズ終了時に表示・保存するスコア情報。
 *
 * @property total 採点対象の問題数
 * @property correct 正解数
 * @property wrong 不正解数
 * @property accuracy 正答率（0から100）
 * @property studySeconds 学習に使った秒数
 * @property starCount 正答率から算出した星の数
 */
data class QuizScore(
    val total: Int,
    val correct: Int,
    val wrong: Int,
    val accuracy: Float,
    val studySeconds: Int,
    val starCount: Int
)

/**
 * クイズの正答率、学習秒数、星評価を一箇所で計算するUseCase。
 *
 * ViewModelやRepositoryに採点条件を分散させないための境界として使う。
 */
class QuizScoreCalculator @Inject constructor() {
    /**
     * クイズ回答履歴からスコアを算出する。
     *
     * @param startedAt クイズ開始時刻（epoch milliseconds）
     * @param finishedAt クイズ終了時刻（epoch milliseconds）
     * @param correctCount 正解数
     * @param answeredCount 回答済み問題数
     * @return 表示と保存に使うスコア情報
     */
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
        accuracy >= THREE_STAR_ACCURACY_THRESHOLD -> THREE_STAR_COUNT
        accuracy >= TWO_STAR_ACCURACY_THRESHOLD -> TWO_STAR_COUNT
        accuracy >= ONE_STAR_ACCURACY_THRESHOLD -> ONE_STAR_COUNT
        else -> 0
    }

    private companion object {
        private const val THREE_STAR_ACCURACY_THRESHOLD = 90f
        private const val TWO_STAR_ACCURACY_THRESHOLD = 70f
        private const val ONE_STAR_ACCURACY_THRESHOLD = 50f
        private const val THREE_STAR_COUNT = 3
        private const val TWO_STAR_COUNT = 2
        private const val ONE_STAR_COUNT = 1
    }
}
