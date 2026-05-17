package com.example.vocabapp.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    private val vocabRepository: VocabRepository
) {
    fun observeWordDetail(wordId: Int) = vocabRepository.observeWordDetail(wordId)
    suspend fun getWordsForTraining(trainingId: Int) = vocabRepository.getWordsForTraining(trainingId)
    suspend fun setWordFavorite(wordId: Int, isFavorite: Boolean) = vocabRepository.setWordFavorite(wordId, isFavorite)
    suspend fun setWordLearned(wordId: Int, isLearned: Boolean) = vocabRepository.setWordLearned(wordId, isLearned)
    suspend fun addCustomWord(english: String, meaning: String) = vocabRepository.addCustomWord(english, meaning)
    suspend fun setCustomWordFavorite(id: Int, isFavorite: Boolean) = vocabRepository.setCustomWordFavorite(id, isFavorite)
    suspend fun setCustomWordLearned(id: Int, isLearned: Boolean) = vocabRepository.setCustomWordLearned(id, isLearned)
}
