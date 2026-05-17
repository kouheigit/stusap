package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_words",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wordId"], unique = true)]
)
data class ReviewWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Int,
    val addedReason: String,
    val isActive: Boolean,
    val addedAt: Long,
    val lastReviewedAt: Long?,
    val wrongCount: Int,
    val correctCount: Int
)
