package com.example.vocabapp.ui.screen.passage

import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.Success
import com.example.vocabapp.ui.theme.TextMuted

internal val PassageReviewCorrect = Success
internal val PassageReviewWrong = Danger
internal val PassageReviewSectionFill = SoftBlue
internal val PassageReviewSectionLine = SoftBlue
internal val PassageReviewMuted = TextMuted

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
