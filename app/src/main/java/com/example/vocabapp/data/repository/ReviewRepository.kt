package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.ReviewWordEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

@Singleton
class ReviewRepository @Inject constructor(
    private val dao: AppDao
) {
    fun observeReviewWords() = dao.observeReviewWords().map { items -> items.map { it.toDomain() } }

    suspend fun addReviewWord(wordId: Int, reason: String = "checked") {
        val now = System.currentTimeMillis()
        val current = dao.getReviewByWordId(wordId)
        if (current == null) {
            dao.insertReviewWord(
                ReviewWordEntity(
                    wordId = wordId,
                    addedReason = reason,
                    isActive = true,
                    addedAt = now,
                    lastReviewedAt = null,
                    wrongCount = if (reason == "checked") 0 else 1,
                    correctCount = 0
                )
            )
        } else {
            dao.updateReviewWord(
                current.copy(
                    addedReason = reason,
                    isActive = true,
                    wrongCount = current.wrongCount + if (reason == "checked") 0 else 1
                )
            )
        }
    }

    suspend fun removeReviewWord(wordId: Int) = dao.deactivateReview(wordId)
}
