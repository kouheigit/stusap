package com.example.vocabapp.domain.model

/** メール文書のヘッダー。`email` 以外では存在しない。 */
data class DocumentHeader(
    val to: String,
    val from: String,
    val date: String,
    val subject: String
)

/**
 * 設問が参照する1つの文書。`documents` の並び順がクイズ画面での提示順序。
 *
 * @property header メール文書のみ持つ宛先・送信元・日付・件名
 * @property title 記事・通知の見出し（メールでは持たないことが多い）
 */
data class PassageDocument(
    val kind: DocumentKind,
    val header: DocumentHeader? = null,
    val title: String? = null,
    val body: String
)
