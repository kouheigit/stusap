package com.example.vocabapp.domain.model

/**
 * 進行中のクイズセッションの状態。`SPEC.md` の SessionState 契約に対応する。
 *
 * @property selections 設問ごとの選択インデックス（未回答は null）。サイズは設問数に等しい。
 * @property score 終了時にのみ非 null。進行中は null。
 */
data class PassageSessionState(
    val setId: String,
    val currentIndex: Int,
    val selections: List<Int?>,
    val remainingSec: Int,
    val finished: Boolean,
    val score: PassageScore? = null
)
