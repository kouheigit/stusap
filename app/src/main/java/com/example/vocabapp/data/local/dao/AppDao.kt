package com.example.vocabapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
import com.example.vocabapp.data.local.entity.WordRelationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun lessonCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(items: List<LessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainings(items: List<TrainingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(items: List<WordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoices(items: List<WordChoiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(items: List<WordRelationEntity>)

    @Transaction
    suspend fun seedIfNeeded(
        lessons: List<LessonEntity>,
        trainings: List<TrainingEntity>,
        words: List<WordEntity>,
        choices: List<WordChoiceEntity>,
        relations: List<WordRelationEntity>
    ) {
        if (lessonCount() == 0) {
            insertLessons(lessons)
            insertTrainings(trainings)
            insertWords(words)
            insertChoices(choices)
            insertRelations(relations)
        }
    }

    @Query("SELECT * FROM lessons ORDER BY scoreTarget, displayOrder")
    fun observeLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLesson(lessonId: Int): LessonEntity?

    @Query("SELECT * FROM trainings WHERE lessonId = :lessonId ORDER BY displayOrder")
    fun observeTrainings(lessonId: Int): Flow<List<TrainingEntity>>

    @Query("SELECT id FROM trainings WHERE lessonId = :lessonId")
    suspend fun getTrainingIdsForLesson(lessonId: Int): List<Int>

    @Query("SELECT * FROM trainings WHERE id = :trainingId")
    suspend fun getTraining(trainingId: Int): TrainingEntity?

    @Query("SELECT * FROM trainings WHERE lessonId = :lessonId AND displayOrder > :displayOrder ORDER BY displayOrder LIMIT 1")
    suspend fun getNextTraining(lessonId: Int, displayOrder: Int): TrainingEntity?

    @Query("SELECT * FROM words WHERE trainingId = :trainingId ORDER BY displayOrder")
    suspend fun getWordsByTraining(trainingId: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWord(wordId: Int): WordEntity?

    @Query("SELECT * FROM word_choices WHERE wordId = :wordId ORDER BY displayOrder")
    suspend fun getChoices(wordId: Int): List<WordChoiceEntity>

    @Query("SELECT * FROM word_relations WHERE wordId = :wordId")
    suspend fun getRelations(wordId: Int): List<WordRelationEntity>

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

    @Insert
    suspend fun insertQuizAttempt(item: QuizAttemptEntity): Long

    @Insert
    suspend fun insertQuizAnswers(items: List<QuizAttemptAnswerEntity>)

    @Insert
    suspend fun insertStudyLog(item: StudyLogEntity)

    @Query("SELECT * FROM user_progress WHERE lessonId = :lessonId AND ((:trainingId IS NULL AND trainingId IS NULL) OR trainingId = :trainingId) LIMIT 1")
    suspend fun getProgress(lessonId: Int, trainingId: Int?): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(item: UserProgressEntity)

    @Query("SELECT * FROM user_progress")
    fun observeProgress(): Flow<List<UserProgressEntity>>

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

    @Query("DELETE FROM quiz_attempt_answers")
    suspend fun deleteAttemptAnswers()

    @Query("DELETE FROM quiz_attempts")
    suspend fun deleteAttempts()

    @Query("DELETE FROM study_logs")
    suspend fun deleteStudyLogs()

    @Query("DELETE FROM user_progress")
    suspend fun deleteProgress()

    @Query("DELETE FROM review_words")
    suspend fun deleteReviews()

    @Insert
    suspend fun insertCustomWord(item: CustomWordEntity): Long

    @Query("SELECT * FROM custom_words ORDER BY addedAt DESC")
    fun observeCustomWords(): Flow<List<CustomWordEntity>>

    @Query("SELECT * FROM custom_words ORDER BY RANDOM()")
    suspend fun getAllCustomWords(): List<CustomWordEntity>

    @Query("SELECT COUNT(*) FROM custom_words")
    suspend fun customWordCount(): Int

    @Transaction
    suspend fun resetLearningData() {
        deleteAttemptAnswers()
        deleteAttempts()
        deleteStudyLogs()
        deleteProgress()
        deleteReviews()
    }
}
