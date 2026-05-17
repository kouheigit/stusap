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
interface TrainingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainings(items: List<TrainingEntity>)
    @Query("SELECT * FROM trainings WHERE lessonId = :lessonId ORDER BY displayOrder")
    fun observeTrainings(lessonId: Int): Flow<List<TrainingEntity>>

    @Query("SELECT id FROM trainings WHERE lessonId = :lessonId")
    suspend fun getTrainingIdsForLesson(lessonId: Int): List<Int>

    @Query("SELECT * FROM trainings WHERE id = :trainingId")
    suspend fun getTraining(trainingId: Int): TrainingEntity?

    @Query("SELECT * FROM trainings WHERE lessonId = :lessonId AND displayOrder > :displayOrder ORDER BY displayOrder LIMIT 1")
    suspend fun getNextTraining(lessonId: Int, displayOrder: Int): TrainingEntity?

    @Query("SELECT trainingId, MIN(id) AS firstId FROM words GROUP BY trainingId")
    fun observeFirstWordIds(): Flow<List<TrainingFirstWordRow>>
}
