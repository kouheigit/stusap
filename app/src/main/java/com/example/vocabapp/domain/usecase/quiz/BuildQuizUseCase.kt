package com.example.vocabapp.domain.usecase.quiz

import com.example.vocabapp.data.repository.QuizRepository
import javax.inject.Inject

class StartQuizUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend fun training(trainingId: Int) = repository.buildTrainingQuiz(trainingId)
    suspend fun review() = repository.buildReviewQuiz()
}

typealias BuildQuizUseCase = StartQuizUseCase
