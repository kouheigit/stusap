package com.example.vocabapp.ui.screen.passage

internal data class PassageSet(
    val id: String,
    val instruction: String,
    val documents: List<PassageDocument>,
    val questions: List<PassageQuestion>,
    val timeLimitSec: Int?
)

internal data class PassageDocument(
    val kind: PassageDocumentKind,
    val header: PassageEmailHeader? = null,
    val title: String? = null,
    val body: String
)

internal enum class PassageDocumentKind {
    Article,
    Email,
    Notice
}

internal data class PassageEmailHeader(
    val to: String,
    val from: String,
    val date: String,
    val subject: String
)

internal data class PassageQuestion(
    val number: String,
    val stem: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String? = null
)

internal data class PassageSessionState(
    val setId: String,
    val currentIndex: Int,
    val selections: List<Int?>,
    val remainingSec: Int,
    val finished: Boolean,
    val score: Int
) {
    val selectedIndex: Int?
        get() = selections.getOrNull(currentIndex)

    val isCurrentAnswered: Boolean
        get() = selectedIndex != null
}
