package com.example.vocabapp.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val vocabRepository: VocabRepository
) {
    suspend fun resetLearningData() = vocabRepository.resetLearningData()
}
