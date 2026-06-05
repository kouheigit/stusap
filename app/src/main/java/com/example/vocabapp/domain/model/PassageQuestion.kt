package com.example.vocabapp.domain.model

/**
 * 4択（最小2・最大4）の設問。
 *
 * @property number "1-1" のような設問番号
 * @property answerIndex `options` に対するゼロ始まりの正解インデックス
 */
data class PassageQuestion(
    val number: String,
    val stem: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String? = null
)
