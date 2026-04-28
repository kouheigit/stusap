package com.example.vocabapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.vocabapp.data.local.dao.AppDao
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

@Database(
    entities = [
        LessonEntity::class,
        TrainingEntity::class,
        WordEntity::class,
        WordChoiceEntity::class,
        WordRelationEntity::class,
        QuizAttemptEntity::class,
        QuizAttemptAnswerEntity::class,
        ReviewWordEntity::class,
        StudyLogEntity::class,
        UserProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
