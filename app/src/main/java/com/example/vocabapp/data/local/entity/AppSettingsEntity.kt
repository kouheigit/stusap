package com.example.vocabapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "stockalert_threshold", defaultValue = "0") val stockAlertThreshold: Int = 0,
    @ColumnInfo(name = "is_active", defaultValue = "1") val isActive: Boolean = true
)
