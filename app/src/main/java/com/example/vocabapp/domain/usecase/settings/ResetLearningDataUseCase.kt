package com.example.vocabapp.domain.usecase.settings

import com.example.vocabapp.data.repository.SettingsRepository
import javax.inject.Inject

class ResetLearningDataUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() = repository.resetLearningData()
}
