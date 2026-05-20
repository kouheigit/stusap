package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.ReviewWordEntity
import com.example.vocabapp.domain.model.ReviewReason
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

@Singleton
class ReviewRepository @Inject constructor(
    private val dao: AppDao
) {
    fun observeReviewWords() = dao.observeReviewWords().map { items -> items.map { it.toDomain() } }

    /**
     * 単語を復習リストへ追加または更新する。
     *
     * @param wordId 対象の単語ID
     * @param reason 登録理由。手動チェック時はデフォルトの [ReviewReason.CHECKED] を使用する。
     */
    suspend fun addReviewWord(wordId: Int, reason: ReviewReason = ReviewReason.CHECKED) {
        val now = System.currentTimeMillis()
        val current = dao.getReviewByWordId(wordId)
        val wrongIncrement = if (reason == ReviewReason.CHECKED) 0 else 1
        if (current == null) {
            dao.insertReviewWord(
                ReviewWordEntity(
                    wordId = wordId,
                    addedReason = reason.dbValue,
                    isActive = true,
                    addedAt = now,
                    lastReviewedAt = null,
                    wrongCount = wrongIncrement,
                    correctCount = 0
                )
            )
        } else {
            dao.updateReviewWord(
                current.copy(
                    addedReason = reason.dbValue,
                    isActive = true,
                    wrongCount = current.wrongCount + wrongIncrement
                )
            )
        }
    }

    suspend fun removeReviewWord(wordId: Int) = dao.deactivateReview(wordId)
}
