package com.example.vocabapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.data.local.entity.LessonEntity
import com.example.vocabapp.data.local.entity.QuizAttemptAnswerEntity
import com.example.vocabapp.data.local.entity.QuizAttemptEntity
import com.example.vocabapp.data.local.entity.ReviewWordEntity
import com.example.vocabapp.data.local.entity.StudyLogEntity
import com.example.vocabapp.data.local.entity.TrainingEntity
import com.example.vocabapp.data.local.entity.UserProgressEntity
import com.example.vocabapp.data.local.entity.WordChoiceEntity
import com.example.vocabapp.data.local.entity.WordEntity
import com.example.vocabapp.data.local.entity.TrainingFirstWordRow
import com.example.vocabapp.data.local.entity.WordRelationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review_words WHERE wordId = :wordId LIMIT 1")
    suspend fun getReviewByWordId(wordId: Int): ReviewWordEntity?

    @Query("SELECT rw.* FROM review_words rw WHERE rw.isActive = 1 ORDER BY rw.addedAt DESC")
    fun observeActiveReviews(): Flow<List<ReviewWordEntity>>

    @Query("SELECT w.* FROM words w INNER JOIN review_words rw ON rw.wordId = w.id WHERE rw.isActive = 1 ORDER BY rw.addedAt DESC")
    fun observeReviewWords(): Flow<List<WordEntity>>

    @Query("SELECT w.* FROM words w INNER JOIN review_words rw ON rw.wordId = w.id WHERE rw.isActive = 1 ORDER BY rw.addedAt DESC LIMIT :limit")
    suspend fun getReviewQuizWords(limit: Int): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewWord(item: ReviewWordEntity): Long

    @Update
    suspend fun updateReviewWord(item: ReviewWordEntity)

    @Query("UPDATE review_words SET isActive = 0 WHERE wordId = :wordId")
    suspend fun deactivateReview(wordId: Int)
    @Query("DELETE FROM review_words")
    suspend fun deleteReviews()
}
