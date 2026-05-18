package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.domain.model.WordRelation
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.combine

@Singleton
class WordRepository @Inject constructor(
    private val dao: AppDao
) {
    fun observeWordDetail(wordId: Int) =
        combine(dao.observeWord(wordId), dao.observeRelations(wordId)) { word, relations ->
            word?.toDomain() to relations.map {
                WordRelation(it.relatedWord, it.relatedMeaning)
            }
        }

    suspend fun getWordsForTraining(trainingId: Int) =
        dao.getWordsByTraining(trainingId).map { it.toDomain() }

    suspend fun setWordFavorite(wordId: Int, isFavorite: Boolean) =
        dao.setWordFavorite(wordId, isFavorite)

    suspend fun setWordLearned(wordId: Int, isLearned: Boolean) =
        dao.setWordLearned(wordId, isLearned)

    suspend fun addCustomWord(english: String, meaning: String) {
        ensureCustomContentCapacity(1)
        val trimmedEnglish = english.trim().take(MAX_CUSTOM_ENGLISH_CHARS)
        val trimmedMeaning = meaning.trim().take(MAX_CUSTOM_MEANING_CHARS)
        if (trimmedEnglish.isBlank() || trimmedMeaning.isBlank()) return
        dao.insertCustomWord(
            CustomWordEntity(
                english = trimmedEnglish,
                meaning = trimmedMeaning,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun setCustomWordFavorite(id: Int, isFavorite: Boolean) =
        dao.setCustomWordFavorite(id, isFavorite)

    suspend fun setCustomWordLearned(id: Int, isLearned: Boolean) =
        dao.setCustomWordLearned(id, isLearned)

    private suspend fun ensureCustomContentCapacity(additionalCount: Int) {
        val currentCount = dao.customWordCount() + dao.customIdiomCount() + dao.customSentenceCount()
        if (currentCount + additionalCount > MAX_CUSTOM_CONTENT_ITEMS) {
            throw IllegalArgumentException("登録上限（${MAX_CUSTOM_CONTENT_ITEMS}件）を超えています")
        }
    }
}
