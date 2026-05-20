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
    CHECKED("checked");

    companion object {
        /** DB から読み出した文字列を enum へ変換する。未知の値は [CHECKED] として扱う。 */
        fun fromDbValue(value: String): ReviewReason =
            entries.firstOrNull { it.dbValue == value } ?: CHECKED
    }
}
