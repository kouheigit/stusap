package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WordRepository
) : ViewModel() {
    val trainingId: Int = checkNotNull(savedStateHandle["trainingId"])
    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()
    private val _index = MutableStateFlow(0)
    val index: StateFlow<Int> = _index.asStateFlow()
    private val _revealed = MutableStateFlow(false)
    val revealed: StateFlow<Boolean> = _revealed.asStateFlow()

    init { viewModelScope.launch { _words.value = repository.getWordsForTraining(trainingId) } }

    fun next() {
        if (_index.value < _words.value.lastIndex) {
            _index.value++
            _revealed.value = false
        }
    }

    fun prev() {
        if (_index.value > 0) {
            _index.value--
            _revealed.value = false
        }
    }

    fun toggleReveal() { _revealed.value = !_revealed.value }
}
