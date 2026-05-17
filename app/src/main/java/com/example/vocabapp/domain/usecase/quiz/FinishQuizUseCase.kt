package com.example.vocabapp.domain.usecase.quiz

import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.domain.model.AnswerRecord
import javax.inject.Inject

class FinishQuizUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(
        trainingId: Int?,
        lessonId: Int?,
        isReview: Boolean,
        startedAt: Long,
        answers: List<AnswerRecord>
    ) = repository.finishQuiz(trainingId, lessonId, isReview, startedAt, answers)
}
