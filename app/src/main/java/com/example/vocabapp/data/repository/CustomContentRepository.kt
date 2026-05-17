package com.example.vocabapp.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomContentRepository @Inject constructor(
    private val vocabRepository: VocabRepository
) {
    fun observeCustomWords() = vocabRepository.observeCustomWords()
    fun observeCustomIdioms() = vocabRepository.observeCustomIdioms()
    fun observeCustomTrainings(type: String) = vocabRepository.observeCustomTrainings(type)
    fun observeCustomSentences() = vocabRepository.observeCustomSentences()
    suspend fun previewCustomWordCsv(csvText: String) = vocabRepository.previewCustomWordCsv(csvText)
    suspend fun importCustomWords(preview: com.example.vocabapp.domain.model.WordImportPreview) = vocabRepository.importCustomWords(preview)
    suspend fun addCustomWord(english: String, meaning: String) = vocabRepository.addCustomWord(english, meaning)
    suspend fun addCustomIdiom(english: String, meaning: String) = vocabRepository.addCustomIdiom(english, meaning)
    suspend fun addCustomSentence(sentence: String, meaning: String) = vocabRepository.addCustomSentence(sentence, meaning)
    suspend fun deleteCustomWord(id: Int) = vocabRepository.deleteCustomWord(id)
    suspend fun deleteCustomIdiom(id: Int) = vocabRepository.deleteCustomIdiom(id)
    suspend fun deleteCustomSentence(id: Int) = vocabRepository.deleteCustomSentence(id)
    suspend fun deleteAllCustomWordsAndIdioms() = vocabRepository.deleteAllCustomWordsAndIdioms()
    suspend fun deleteAllCustomSentences() = vocabRepository.deleteAllCustomSentences()
    suspend fun setCustomWordFavorite(id: Int, isFavorite: Boolean) = vocabRepository.setCustomWordFavorite(id, isFavorite)
    suspend fun setCustomWordLearned(id: Int, isLearned: Boolean) = vocabRepository.setCustomWordLearned(id, isLearned)
    suspend fun buildCustomTrainingQuiz(type: String, setNumber: Int) = vocabRepository.buildCustomTrainingQuiz(type, setNumber)
    suspend fun buildRandomCustomQuiz(type: String) = vocabRepository.buildRandomCustomQuiz(type)
    suspend fun buildCustomIdiomQuiz() = vocabRepository.buildCustomIdiomQuiz()
    suspend fun buildCustomWordQuiz() = vocabRepository.buildCustomWordQuiz()
}
