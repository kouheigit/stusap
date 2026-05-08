package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

data class TrainingFirstWordRow(
    @ColumnInfo(name = "trainingId") val trainingId: Int,
    @ColumnInfo(name = "firstId") val firstId: Int
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Int,
    val scoreTarget: Int,
    val title: String,
    val wordStartNumber: Int,
    val wordEndNumber: Int,
    val displayOrder: Int
)

@Entity(
    tableName = "trainings",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId")]
)
data class TrainingEntity(
    @PrimaryKey val id: Int,
    val lessonId: Int,
    val title: String,
    val wordStartNumber: Int,
    val wordEndNumber: Int,
    val displayOrder: Int
)

@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = TrainingEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trainingId")]
)
data class WordEntity(
    @PrimaryKey val id: Int,
    val trainingId: Int,
    val english: String,
    val meaning: String,
    val phonetic: String,
    val partOfSpeech: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val audioUrl: String?,
    val exampleAudioUrl: String?,
    val displayOrder: Int
)

@Entity(
    tableName = "word_choices",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordId")]
)
data class WordChoiceEntity(
    @PrimaryKey val id: Int,
    val wordId: Int,
    val choiceText: String,
    val isCorrect: Boolean,
    val displayOrder: Int
)

@Entity(
    tableName = "word_relations",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordId")]
)
data class WordRelationEntity(
    @PrimaryKey val id: Int,
    val wordId: Int,
    val relatedWord: String,
    val relatedMeaning: String
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingId: Int?,
    val isReview: Boolean,
    val startedAt: Long,
    val finishedAt: Long,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val accuracy: Float,
    val studySeconds: Int,
    val starCount: Int
)

@Entity(
    tableName = "quiz_attempt_answers",
    foreignKeys = [
        ForeignKey(
            entity = QuizAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizAttemptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quizAttemptId"), Index("wordId")]
)
data class QuizAttemptAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizAttemptId: Long,
    val wordId: Int,
    val selectedChoiceId: Int?,
    val isCorrect: Boolean,
    val answeredAt: Long,
    val responseMillis: Int,
    val selectedUnknown: Boolean
)

@Entity(
    tableName = "review_words",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wordId"], unique = true)]
)
data class ReviewWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Int,
    val addedReason: String,
    val isActive: Boolean,
    val addedAt: Long,
    val lastReviewedAt: Long?,
    val wrongCount: Int,
    val correctCount: Int
)

@Entity(tableName = "study_logs")
data class StudyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studiedAt: Long,
    val lessonId: Int?,
    val trainingId: Int?,
    val studySeconds: Int,
    val correctCount: Int,
    val wrongCount: Int
)

@Entity(
    tableName = "user_progress",
    indices = [Index(value = ["lessonId", "trainingId"], unique = true)]
)
data class UserProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lessonId: Int,
    val trainingId: Int?,
    val studyCount: Int,
    val bestAccuracy: Float,
    val bestStarCount: Int,
    val lastStudiedAt: Long?,
    val isMastered: Boolean,
    @androidx.room.ColumnInfo(defaultValue = "0") val lastAccuracy: Float = 0f
)

@Entity(tableName = "custom_words")
data class CustomWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val english: String,
    val meaning: String,
    val addedAt: Long,
    @androidx.room.ColumnInfo(defaultValue = "") val exampleSentence: String = "",
    @androidx.room.ColumnInfo(defaultValue = "") val exampleTranslation: String = "",
    @androidx.room.ColumnInfo(defaultValue = "word") val wordType: String = "word"
)

@Entity(tableName = "custom_idioms")
data class CustomIdiomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val english: String,
    val meaning: String,
    val addedAt: Long
)
