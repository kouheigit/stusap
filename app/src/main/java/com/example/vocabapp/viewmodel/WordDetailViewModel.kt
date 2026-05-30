package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.CustomImportRepository
import com.example.vocabapp.data.repository.ReviewRepository
import com.example.vocabapp.data.repository.WordRepository
import com.example.vocabapp.domain.model.HomeSummary
import com.example.vocabapp.domain.model.SentenceQuizState
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.QuizState
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.domain.model.WordImportPreview
import com.example.vocabapp.domain.model.WordImportResult
import com.example.vocabapp.domain.model.WordRelation
import com.example.vocabapp.domain.usecase.quiz.FinishQuizUseCase
import com.example.vocabapp.domain.usecase.lesson.GetIdiomLessonsUseCase
import com.example.vocabapp.domain.usecase.lesson.GetLessonsUseCase
import com.example.vocabapp.domain.usecase.review.GetReviewWordsUseCase
import com.example.vocabapp.domain.usecase.lesson.GetTrainingsUseCase
import com.example.vocabapp.domain.usecase.quiz.StartQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val wordId: Int = checkNotNull(savedStateHandle["wordId"])
    private val _word = MutableStateFlow<Word?>(null)
    val word: StateFlow<Word?> = _word.asStateFlow()
    private val _relations = MutableStateFlow<List<WordRelation>>(emptyList())
    val relations: StateFlow<List<WordRelation>> = _relations.asStateFlow()

    init {
        wordRepository.observeWordDetail(wordId)
            .onEach { detail ->
                _word.value = detail.first
                _relations.value = detail.second
            }
            .launchIn(viewModelScope)
    }

    fun addReview() {
        viewModelScope.launch { reviewRepository.addReviewWord(wordId) }
    }

    fun setFavorite(isFavorite: Boolean) {
        viewModelScope.launch { wordRepository.setWordFavorite(wordId, isFavorite) }
    }

    fun setLearned(isLearned: Boolean) {
        viewModelScope.launch { wordRepository.setWordLearned(wordId, isLearned) }
    }
}
@HiltViewModel
class AddWordViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun save(english: String, meaning: String) {
        if (english.isBlank() || meaning.isBlank()) return
        viewModelScope.launch {
            val trimmed = english.trim()
            runCatching {
                if (trimmed.contains(Regex("\\s"))) {
                    repository.addCustomIdiom(trimmed, meaning.trim())
                } else {
                    repository.addCustomWord(trimmed, meaning.trim())
                }
            }.onSuccess {
                _saved.value = true
            }.onFailureUnlessCancellation {}
        }
    }

    fun resetSaved() { _saved.value = false }
}

@HiltViewModel
class WordImportViewModel @Inject constructor(
    private val repository: CustomImportRepository
) : ViewModel() {
    private var defaultType: String = "word"

    fun setDefaultType(type: String) { defaultType = type }

    private val _preview = MutableStateFlow<WordImportPreview?>(null)
    val preview: StateFlow<WordImportPreview?> = _preview.asStateFlow()
    private val _result = MutableStateFlow<WordImportResult?>(null)
    val result: StateFlow<WordImportResult?> = _result.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun loadCsv(csvText: String) {
        loadRowsInBackground { repository.previewCustomWordCsv(csvText, defaultType) }
    }

    fun loadRows(rows: List<List<String>>) {
        loadRowsInBackground { repository.previewCustomWordRows(rows, defaultType) }
    }

    private fun loadRowsInBackground(loadPreview: suspend () -> WordImportPreview) {
        viewModelScope.launch {
            _isLoading.value = true
            _result.value = null
            runCatching {
                withContext(Dispatchers.Default) {
                    loadPreview()
                }
            }.onSuccess { loadedPreview ->
                _preview.value = loadedPreview
            }.onFailureUnlessCancellation { error ->
                _preview.value = null
                sendMessage(error.message ?: "CSVの読み込みに失敗しました")
            }
            _isLoading.value = false
        }
    }

    fun registerPreview() {
        val currentPreview = _preview.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.importCustomWords(currentPreview)
                }
            }.onSuccess { importResult ->
                _result.value = importResult
                _preview.value = null
            }.onFailureUnlessCancellation { error ->
                sendMessage(error.message ?: "登録に失敗しました")
            }
            _isLoading.value = false
        }
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
