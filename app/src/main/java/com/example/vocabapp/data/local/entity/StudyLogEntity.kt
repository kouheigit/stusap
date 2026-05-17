package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
