package com.example.vocabapp.ui.screen.passage

import androidx.compose.ui.graphics.Color

internal val PassageReviewCorrect = Color(0xFF4ABFC2)
internal val PassageReviewWrong = Color(0xFFD33EA7)
internal val PassageReviewSectionFill = Color(0xFFEAF4FB)
internal val PassageReviewSectionLine = Color(0xFFD5E1EA)
internal val PassageReviewMuted = Color(0xFF8A97A2)

internal data class PassageQuestionReview(
    val question: PassageQuestion,
    val selectedIndex: Int?
) {
    val isCorrect: Boolean
        get() = selectedIndex == question.answerIndex

    val selectedText: String?
        get() = selectedIndex?.let { question.options.getOrNull(it) }

    val correctText: String
        get() = question.options.getOrElse(question.answerIndex) { question.options.firstOrNull().orEmpty() }
}

internal fun PassageSet.reviews(state: PassageSessionState): List<PassageQuestionReview> {
    return questions.mapIndexed { index, question ->
        PassageQuestionReview(
            question = question,
            selectedIndex = state.selections.getOrNull(index)
        )
    }
}

internal fun passageChoiceLabel(index: Int): String = ('A' + index).toString()
