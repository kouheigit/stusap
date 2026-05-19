package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * カスタム英熟語登録フォームの保存状態を管理するViewModel。
 */
class AddIdiomViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun save(english: String, meaning: String) {
        if (english.isBlank() || meaning.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.addCustomIdiom(english.trim(), meaning.trim())
            }.onSuccess {
                _saved.value = true
            }.onFailureUnlessCancellation {}
        }
    }

    fun resetSaved() { _saved.value = false }
}

