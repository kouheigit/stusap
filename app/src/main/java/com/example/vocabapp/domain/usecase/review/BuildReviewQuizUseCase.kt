package com.example.vocabapp.domain.usecase.review

import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.data.repository.ReviewRepository
import javax.inject.Inject

class BuildReviewQuizUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke() = repository.buildReviewQuiz()
}

class GetReviewWordsUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    operator fun invoke() = repository.observeReviewWords()
}
