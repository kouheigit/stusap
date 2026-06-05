package com.example.vocabapp.domain.model

/** 文書の種別。設問セット内では複数種別が混在しうる（例: notice + email）。 */
enum class DocumentKind { ARTICLE, EMAIL, NOTICE }
