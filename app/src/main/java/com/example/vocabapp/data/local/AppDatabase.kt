package com.example.vocabapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vocabapp.data.local.dao.AppDao
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

@Database(
    entities = [
        CustomWordEntity::class,
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
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `custom_words` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `english` TEXT NOT NULL,
                        `meaning` TEXT NOT NULL,
                        `addedAt` INTEGER NOT NULL
                    )
                """)
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_progress ADD COLUMN lastAccuracy REAL NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE user_progress SET lastAccuracy = bestAccuracy WHERE lastAccuracy = 0 AND bestAccuracy > 0")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE custom_words ADD COLUMN exampleSentence TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE custom_words ADD COLUMN exampleTranslation TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE custom_words ADD COLUMN wordType TEXT NOT NULL DEFAULT 'word'")
            }
        }
    }
}
