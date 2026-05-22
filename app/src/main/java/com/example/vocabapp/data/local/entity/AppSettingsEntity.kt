package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "daily_review_goal", defaultValue = "0") val dailyReviewGoal: Int = 0,
    @ColumnInfo(name = "is_study_reminder_enabled", defaultValue = "1") val isStudyReminderEnabled: Boolean = true
)
