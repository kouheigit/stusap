package com.example.vocabapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_passage_questions",
    foreignKeys = [
        ForeignKey(
            entity = CustomPassageSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("setId")]
)
data class CustomPassageQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setId: Int,
    val number: String,
    val stem: String,
    val optionsText: String,
    val answerIndex: Int,
    val explanation: String?,
    val displayOrder: Int
)
