package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val customContentRepository: CustomContentRepository
) : ViewModel() {
    fun resetProgress() {
        viewModelScope.launch { settingsRepository.resetLearningData() }
    }

    fun deleteAllCustomWordsAndIdioms() {
        viewModelScope.launch {
            customContentRepository.deleteAllCustomWordsAndIdioms()
        }
    }

    fun deleteAllCustomSentences() {
        viewModelScope.launch { customContentRepository.deleteAllCustomSentences() }
    }
}
