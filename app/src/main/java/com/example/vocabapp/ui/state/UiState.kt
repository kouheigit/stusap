package com.example.vocabapp.ui.state

/**
 * 画面に表示する非同期データの状態を統一して表す。
 *
 * @param T 成功時に保持するデータ型
 */
sealed class UiState<out T> {
    /** 読み込み中の状態。 */
    data object Loading : UiState<Nothing>()

    /**
     * データ取得に成功した状態。
     *
     * @property data 画面へ渡すデータ
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * データ取得または処理に失敗した状態。
     *
     * @property message ユーザーまたはログ向けの概要メッセージ
     * @property throwable 原因例外。原因がない場合はnull。
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()
}
