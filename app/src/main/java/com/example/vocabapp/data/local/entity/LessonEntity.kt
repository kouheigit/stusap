package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Int,
    val scoreTarget: Int,
    val title: String,
    val wordStartNumber: Int,
    val wordEndNumber: Int,
    val displayOrder: Int
)
