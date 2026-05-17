package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "custom_idioms")
data class CustomIdiomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val english: String,
    val meaning: String,
    val addedAt: Long
)
