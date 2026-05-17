package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
