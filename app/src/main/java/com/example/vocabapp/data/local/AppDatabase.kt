package com.example.vocabapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.dao.AppSettingsDao
import com.example.vocabapp.data.local.dao.CustomContentDao
import com.example.vocabapp.data.local.dao.LessonDao
import com.example.vocabapp.data.local.dao.QuizDao
import com.example.vocabapp.data.local.dao.ReviewDao
import com.example.vocabapp.data.local.dao.StudyLogDao
import com.example.vocabapp.data.local.dao.TrainingDao
import com.example.vocabapp.data.local.dao.UserProgressDao
import com.example.vocabapp.data.local.dao.WordDao
import com.example.vocabapp.data.local.entity.AppSettingsEntity
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
import com.example.vocabapp.data.local.entity.WordRelationEntity

@Database(
    entities = [
        CustomIdiomEntity::class,
        CustomSentenceEntity::class,
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
        UserProgressEntity::class,
        AppSettingsEntity::class
    ],
    version = 12,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun wordDao(): WordDao
    abstract fun lessonDao(): LessonDao
    abstract fun trainingDao(): TrainingDao
    abstract fun quizDao(): QuizDao
    abstract fun reviewDao(): ReviewDao
    abstract fun studyLogDao(): StudyLogDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun customContentDao(): CustomContentDao
    abstract fun appSettingsDao(): AppSettingsDao

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
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `custom_idioms` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `english` TEXT NOT NULL,
                        `meaning` TEXT NOT NULL,
                        `addedAt` INTEGER NOT NULL
                    )
                """)
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Keep user learning history intact. Seed refreshes must not delete progress-linked data.
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `custom_sentences` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sentence` TEXT NOT NULL,
                        `meaning` TEXT NOT NULL,
                        `addedAt` INTEGER NOT NULL
                    )
                """)
            }
        }
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `app_settings` (
                        `id` INTEGER NOT NULL,
                        `stockalert_threshold` INTEGER NOT NULL DEFAULT 0,
                        `is_active` INTEGER NOT NULL DEFAULT 1,
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE words ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE words ADD COLUMN isLearned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE custom_words ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE custom_words ADD COLUMN isLearned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `quiz_attempt_answers_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `quizAttemptId` INTEGER NOT NULL,
                        `wordId` INTEGER NOT NULL,
                        `selectedChoiceId` INTEGER,
                        `isCorrect` INTEGER NOT NULL,
                        `answeredAt` INTEGER NOT NULL,
                        `responseMillis` INTEGER NOT NULL,
                        `selectedUnknown` INTEGER NOT NULL,
                        `wordTrainingId` INTEGER NOT NULL DEFAULT 0,
                        `wordEnglish` TEXT NOT NULL DEFAULT '',
                        `wordMeaning` TEXT NOT NULL DEFAULT '',
                        `wordPhonetic` TEXT NOT NULL DEFAULT '',
                        `wordPartOfSpeech` TEXT NOT NULL DEFAULT '',
                        `wordExampleSentence` TEXT NOT NULL DEFAULT '',
                        `wordExampleTranslation` TEXT NOT NULL DEFAULT '',
                        `wordAudioUrl` TEXT,
                        `wordExampleAudioUrl` TEXT,
                        `wordDisplayOrder` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`quizAttemptId`) REFERENCES `quiz_attempts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("""
                    INSERT INTO quiz_attempt_answers_new (
                        id, quizAttemptId, wordId, selectedChoiceId, isCorrect, answeredAt,
                        responseMillis, selectedUnknown, wordTrainingId, wordEnglish, wordMeaning,
                        wordPhonetic, wordPartOfSpeech, wordExampleSentence, wordExampleTranslation,
                        wordAudioUrl, wordExampleAudioUrl, wordDisplayOrder
                    )
                    SELECT
                        qaa.id, qaa.quizAttemptId, qaa.wordId, qaa.selectedChoiceId, qaa.isCorrect,
                        qaa.answeredAt, qaa.responseMillis, qaa.selectedUnknown,
                        COALESCE(w.trainingId, 0), COALESCE(w.english, ''), COALESCE(w.meaning, ''),
                        COALESCE(w.phonetic, ''), COALESCE(w.partOfSpeech, ''),
                        COALESCE(w.exampleSentence, ''), COALESCE(w.exampleTranslation, ''),
                        w.audioUrl, w.exampleAudioUrl, COALESCE(w.displayOrder, 0)
                    FROM quiz_attempt_answers qaa
                    LEFT JOIN words w ON w.id = qaa.wordId
                """)
                db.execSQL("DROP TABLE quiz_attempt_answers")
                db.execSQL("ALTER TABLE quiz_attempt_answers_new RENAME TO quiz_attempt_answers")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_attempt_answers_quizAttemptId ON quiz_attempt_answers(quizAttemptId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_attempt_answers_wordId ON quiz_attempt_answers(wordId)")
            }
        }
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE custom_sentences ADD COLUMN importedFromFile TEXT")
            }
        }
        // Renames app_settings columns from legacy inventory-app naming (stockalert_threshold, is_active)
        // to vocabulary-app domain names (daily_review_goal, is_study_reminder_enabled).
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `app_settings_new` (
                        `id` INTEGER NOT NULL,
                        `daily_review_goal` INTEGER NOT NULL DEFAULT 0,
                        `is_study_reminder_enabled` INTEGER NOT NULL DEFAULT 1,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("""
                    INSERT INTO app_settings_new (
                        id, daily_review_goal, is_study_reminder_enabled
                    )
                    SELECT id, stockalert_threshold, is_active FROM app_settings
                """)
                db.execSQL("DROP TABLE app_settings")
                db.execSQL("ALTER TABLE app_settings_new RENAME TO app_settings")
            }
        }
    }
}
