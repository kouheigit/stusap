package com.example.vocabapp.domain.model

/** 長文読解問題に共通する境界値。採点・検証・セッション初期化で参照する。 */
object PassageConstants {
    /** 選択肢数の下限（SPEC: options は 2..4）。 */
    const val MIN_OPTIONS = 2

    /** 選択肢数の上限（SPEC: options は 2..4）。 */
    const val MAX_OPTIONS = 4

    /** `timeLimitSec` 未指定セットに適用する既定の制限時間（秒）。 */
    const val DEFAULT_TIME_LIMIT_SEC = 300
}
