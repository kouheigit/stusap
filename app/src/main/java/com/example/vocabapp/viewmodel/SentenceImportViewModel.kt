package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomImportRepository
import com.example.vocabapp.domain.model.SentenceImportPreview
import com.example.vocabapp.domain.model.SentenceImportResult
import com.example.vocabapp.util.AppDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SentenceImportViewModel @Inject constructor(
    private val repository: CustomImportRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {
    private val _preview = MutableStateFlow<SentenceImportPreview?>(null)
    val preview: StateFlow<SentenceImportPreview?> = _preview.asStateFlow()
    private val _result = MutableStateFlow<SentenceImportResult?>(null)
    val result: StateFlow<SentenceImportResult?> = _result.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()
    private val _fileName = MutableStateFlow<String?>(null)
    val fileName: StateFlow<String?> = _fileName.asStateFlow()
    private val _remainingCapacity = MutableStateFlow<Int?>(null)
    val remainingCapacity: StateFlow<Int?> = _remainingCapacity.asStateFlow()

    init {
        loadRemainingCapacity()
    }

    fun loadCsv(csvText: String, fileName: String? = null) {
        loadRowsInBackground(fileName) { repository.previewCustomSentenceCsv(csvText) }
    }

    fun loadRows(rows: List<List<String>>, fileName: String? = null) {
        loadRowsInBackground(fileName) { repository.previewCustomSentenceRows(rows) }
    }

    private fun loadRowsInBackground(fileName: String?, loadPreview: suspend () -> SentenceImportPreview) {
        viewModelScope.launch {
            _isLoading.value = true
            _result.value = null
            if (fileName != null) _fileName.value = fileName
            runCatching {
                withContext(dispatchers.default) {
                    loadPreview()
                }
            }.onSuccess { loadedPreview ->
                _preview.value = loadedPreview.copy(sourceFileName = _fileName.value)
            }.onFailureUnlessCancellation { error ->
                _preview.value = null
                sendMessage(error.message ?: "文章ファイルの読み込みに失敗しました")
            }
            _isLoading.value = false
        }
    }

    fun registerPreview() {
        val currentPreview = _preview.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                withContext(dispatchers.io) {
                    repository.importCustomSentences(currentPreview)
                }
            }.onSuccess { importResult ->
                _result.value = importResult
                _preview.value = null
                loadRemainingCapacity()
            }.onFailureUnlessCancellation { error ->
                sendMessage(error.message ?: "文章の登録に失敗しました")
            }
            _isLoading.value = false
        }
    }

    fun loadRemainingCapacity() {
        viewModelScope.launch {
            runCatching {
                withContext(dispatchers.io) {
                    repository.remainingCustomContentCapacity()
                }
            }.onSuccess { capacity ->
                _remainingCapacity.value = capacity
            }.onFailureUnlessCancellation {}
        }
    }

    fun resetForNewFile() {
        _preview.value = null
        _result.value = null
        _fileName.value = null
    }

    fun showMessage(message: String) {
        sendMessage(message)
        _isLoading.value = false
    }

    fun showLoading() {
        _isLoading.value = true
        _preview.value = null
        _result.value = null
    }

    fun clearTransientState() {
        _preview.value = null
        _result.value = null
        _fileName.value = null
        _isLoading.value = false
    }

    override fun onCleared() {
        clearTransientState()
        super.onCleared()
    }

    private fun sendMessage(message: String) {
        _messages.tryEmit(message)
    }
}
