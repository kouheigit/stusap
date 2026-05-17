package com.example.vocabapp.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val vocabRepository: VocabRepository
) {
    fun observeReviewWords() = vocabRepository.observeReviewWords()
    suspend fun addReviewWord(wordId: Int, reason: String = "checked") = vocabRepository.addReviewWord(wordId, reason)
    suspend fun removeReviewWord(wordId: Int) = vocabRepository.removeReviewWord(wordId)
}
