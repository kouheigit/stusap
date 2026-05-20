package com.example.vocabapp.domain.model

enum class ReviewReason(val dbValue: String) {
    WRONG("wrong"),
    UNKNOWN("unknown"),
    CHECKED("checked")
}
