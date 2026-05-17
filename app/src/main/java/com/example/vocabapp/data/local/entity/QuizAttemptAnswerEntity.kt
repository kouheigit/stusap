package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_attempt_answers",
    foreignKeys = [
        ForeignKey(
            entity = QuizAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizAttemptId"],
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
    val selectedUnknown: Boolean,
    @ColumnInfo(defaultValue = "0") val wordTrainingId: Int = 0,
    @ColumnInfo(defaultValue = "") val wordEnglish: String = "",
    @ColumnInfo(defaultValue = "") val wordMeaning: String = "",
    @ColumnInfo(defaultValue = "") val wordPhonetic: String = "",
    @ColumnInfo(defaultValue = "") val wordPartOfSpeech: String = "",
    @ColumnInfo(defaultValue = "") val wordExampleSentence: String = "",
    @ColumnInfo(defaultValue = "") val wordExampleTranslation: String = "",
    val wordAudioUrl: String? = null,
    val wordExampleAudioUrl: String? = null,
    @ColumnInfo(defaultValue = "0") val wordDisplayOrder: Int = 0
)
