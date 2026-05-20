package com.example.vocabapp.domain.model

/**
 * 復習単語リストへ登録する際の理由を表す。
 *
 * @property dbValue DB の addedReason カラムに永続化する文字列。
 *   既存レコードとの互換性のため lowercase を維持している。
 */
enum class ReviewReason(val dbValue: String) {
    /** クイズで誤答した単語。wrongCount をインクリメントする。 */
    WRONG("wrong"),

    /** 「わからない」を選択した単語。wrongCount をインクリメントする。 */
    UNKNOWN("unknown"),

    /** 単語詳細画面などで手動チェックした単語。wrongCount は変更しない。 */
    CHECKED("checked")
}
