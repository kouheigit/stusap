package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.dao.AppDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dao: AppDao
) {
    suspend fun resetLearningData() = dao.resetLearningData()
}
