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
interface QuizDao {
    @Insert
    suspend fun insertQuizAttempt(item: QuizAttemptEntity): Long

    @Insert
    suspend fun insertQuizAnswers(items: List<QuizAttemptAnswerEntity>)
    @Query("SELECT * FROM quiz_attempts ORDER BY finishedAt DESC LIMIT 1")
    suspend fun getLastAttempt(): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId")
    suspend fun getAttempt(attemptId: Long): QuizAttemptEntity?
    @Query("DELETE FROM quiz_attempt_answers")
    suspend fun deleteAttemptAnswers()

    @Query("DELETE FROM quiz_attempts")
    suspend fun deleteAttempts()
    @Query("SELECT * FROM quiz_attempt_answers WHERE quizAttemptId = :attemptId AND isCorrect = 0 ORDER BY answeredAt")
    suspend fun getWrongAnswersForAttempt(attemptId: Long): List<QuizAttemptAnswerEntity>
}
