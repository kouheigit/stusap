package com.example.vocabapp.domain.model

/**
 * アプリ内で扱う学習コンテンツの種別。
 *
 * 種別ごとの定数（DB上のlessonId、品詞ラベルなど）をここに集約する。
 * 新しい種別を追加する場合はこのenumにエントリを追加するだけでよい。
 *
 * @property routeValue Navigation引数と永続化済み文字列に使う互換値
 * @property lessonId カスタムコンテンツ用の仮想レッスンID（負数）
 * @property partOfSpeech クイズ問題に表示する品詞ラベル
 * @property customTitle トレーニング一覧に表示するコンテンツ名
 * @property wordDomainIdBase ドメインID計算の基準値（wordDomainId = wordDomainIdBase - sourceId）
 * @property choiceIdMultiplier 選択肢IDの乗数（WordChoice.idの一意性確保に使用）
 */
enum class ContentType(
    val routeValue: String,
    val lessonId: Int,
    val partOfSpeech: String,
    val customTitle: String,
    val wordDomainIdBase: Int,
    val choiceIdMultiplier: Int
) {
    WORD(
        routeValue = "word",
        lessonId = -10_000,
        partOfSpeech = "単語",
        customTitle = "カスタム英単語",
        wordDomainIdBase = -100_000,
        choiceIdMultiplier = -100
    ),
    IDIOM(
        routeValue = "idiom",
        lessonId = -20_000,
        partOfSpeech = "英熟語",
        customTitle = "カスタム英熟語",
        wordDomainIdBase = -200_000,
        choiceIdMultiplier = -200
    );

    /** ランダムクイズ用トレーニングID */
    val randomTrainingId: Int get() = lessonId - RANDOM_TRAINING_OFFSET

    /** セット番号に対応するトレーニングID */
    fun trainingId(setNumber: Int): Int = lessonId - setNumber

    /** DB上のソースIDからドメインモデルのIDを生成する */
    fun wordDomainId(sourceId: Int): Int = wordDomainIdBase - sourceId

    companion object {
        private const val RANDOM_TRAINING_OFFSET = 999

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
