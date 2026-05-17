package com.example.vocabapp.domain.usecase.review

import com.example.vocabapp.data.repository.ReviewRepository
import javax.inject.Inject

class AddReviewWordUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(wordId: Int) = repository.addReviewWord(wordId)
}

typealias AddReviewUseCase = AddReviewWordUseCase
