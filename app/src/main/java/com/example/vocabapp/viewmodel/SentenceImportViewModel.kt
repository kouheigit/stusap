package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.domain.model.SentenceImportPreview
import com.example.vocabapp.domain.model.SentenceImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SentenceImportViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    private val _preview = MutableStateFlow<SentenceImportPreview?>(null)
    val preview: StateFlow<SentenceImportPreview?> = _preview.asStateFlow()
    private val _result = MutableStateFlow<SentenceImportResult?>(null)
    val result: StateFlow<SentenceImportResult?> = _result.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadCsv(csvText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            _result.value = null
            runCatching {
                withContext(Dispatchers.Default) {
                    repository.previewCustomSentenceCsv(csvText)
                }
            }.onSuccess { loadedPreview ->
                _preview.value = loadedPreview
            }.onFailureUnlessCancellation { error ->
                _preview.value = null
                _message.value = error.message ?: "文章ファイルの読み込みに失敗しました"
            }
            _isLoading.value = false
        }
    }

    fun registerPreview() {
        val currentPreview = _preview.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.importCustomSentences(currentPreview)
                }
            }.onSuccess { importResult ->
                _result.value = importResult
            }.onFailureUnlessCancellation { error ->
                _message.value = error.message ?: "文章の登録に失敗しました"
            }
            _isLoading.value = false
        }
    }

    fun showMessage(message: String) {
        _message.value = message
        _isLoading.value = false
    }

    fun showLoading() {
        _isLoading.value = true
        _message.value = null
        _preview.value = null
        _result.value = null
    }
}
