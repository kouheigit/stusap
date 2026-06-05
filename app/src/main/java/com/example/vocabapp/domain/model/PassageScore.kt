package com.example.vocabapp.domain.model

/**
 * クイズのスコア。
 *
 * @property total 設問数
 * @property correct 正解数
 * @property wrong 不正解数（未回答を含む）
 * @property accuracy 正答率（0から100）
 */
data class PassageScore(
    val total: Int,
    val correct: Int,
    val wrong: Int,
    val accuracy: Float
)
