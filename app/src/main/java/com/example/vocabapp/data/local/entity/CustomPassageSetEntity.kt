package com.example.vocabapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_passage_sets")
data class CustomPassageSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val documentKind: String,
    val instruction: String,
    val body: String,
    val timeLimitSec: Int?,
    val addedAt: Long
)

data class CustomPassageSummary(
    val id: Int,
    val title: String,
    val documentKind: String,
    val questionCount: Int,
    val timeLimitSec: Int?,
    val addedAt: Long
)
