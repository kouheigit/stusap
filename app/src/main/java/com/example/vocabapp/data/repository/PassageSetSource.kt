package com.example.vocabapp.data.repository

/**
 * 長文読解問題セットの生データ（JSON 文字列）の供給元。
 *
 * これが Repository の差し替え可能な入口。現在は fixture を読む実装を使うが、
 * 将来の取り込み機能は同じインターフェースで別の供給元（assets / DB / ファイル）を
 * 提供し、`SPEC.md` と同じ JSON 形状を返せばよい。
 */
fun interface PassageSetSource {
    /** 設問セット群を表す JSON 文字列を返す。 */
    suspend fun readRawJson(): String
}
