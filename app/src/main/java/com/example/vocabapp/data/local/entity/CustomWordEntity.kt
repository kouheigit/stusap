package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "custom_words")
data class CustomWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val english: String,
    val meaning: String,
    val addedAt: Long,
    @androidx.room.ColumnInfo(defaultValue = "") val exampleSentence: String = "",
    @androidx.room.ColumnInfo(defaultValue = "") val exampleTranslation: String = "",
    @androidx.room.ColumnInfo(defaultValue = "word") val wordType: String = "word",
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isLearned: Boolean = false
)
