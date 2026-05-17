package com.example.vocabapp.domain.model

/**
 * アプリ内で扱う学習コンテンツの種別。
 *
 * @property routeValue Navigation引数と永続化済み文字列に使う互換値
 */
enum class ContentType(val routeValue: String) {
    WORD("word"),
    IDIOM("idiom");

    companion object {
        /**
         * Navigation引数などの文字列から種別へ変換する。
         *
         * @param value 互換文字列
         * @return 対応するContentType。未定義値はWORDとして扱う。
         */
        fun fromRouteValue(value: String): ContentType =
            entries.firstOrNull { it.routeValue == value } ?: WORD
    }
}
