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
interface StudyLogDao {
    @Insert
    suspend fun insertStudyLog(item: StudyLogEntity)
    @Query("SELECT COALESCE(SUM(studySeconds), 0) FROM study_logs")
    fun observeTotalStudySeconds(): Flow<Int>

    @Query("SELECT COALESCE(SUM(studySeconds), 0) FROM study_logs WHERE studiedAt >= :fromMillis")
    fun observeStudySecondsFrom(fromMillis: Long): Flow<Int>

    @Query("SELECT * FROM quiz_attempts ORDER BY finishedAt DESC LIMIT 1")
    suspend fun getLastAttempt(): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId")
    suspend fun getAttempt(attemptId: Long): QuizAttemptEntity?

    @Query("SELECT * FROM study_logs ORDER BY studiedAt DESC")
    fun observeStudyLogs(): Flow<List<StudyLogEntity>>

    @Query("SELECT DISTINCT (studiedAt / 86400000) FROM study_logs ORDER BY 1 DESC")
    fun observeStudyDays(): Flow<List<Long>>
    @Query("DELETE FROM study_logs")
    suspend fun deleteStudyLogs()
}
