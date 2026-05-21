package com.example.vocabapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_sentences")
data class CustomSentenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sentence: String,
    val meaning: String,
    val addedAt: Long,
    val importedFromFile: String? = null
)
