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
