package com.example.vocabapp.domain.model

/**
 * 文書群と設問群をまとめた1つの出題単位。
 *
 * @property timeLimitSec 制限時間（秒）。未指定なら呼び出し側の既定値を使う。
 */
data class PassageSet(
    val id: String,
    val instruction: String,
    val documents: List<PassageDocument>,
    val questions: List<PassageQuestion>,
    val timeLimitSec: Int? = null
)
