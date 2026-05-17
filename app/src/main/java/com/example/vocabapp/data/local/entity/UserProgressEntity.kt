package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
