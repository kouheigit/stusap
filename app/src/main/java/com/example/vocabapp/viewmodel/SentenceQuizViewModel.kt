package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.QuizRepository
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
class SentenceQuizViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SentenceQuizState())
    val state: StateFlow<SentenceQuizState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val questions = repository.buildSentenceQuiz()
            _state.value = SentenceQuizState(questions = questions, startedAt = startedAt)
        }
    }

    fun selectWord(choiceIndex: Int) {
        val current = _state.value
        if (current.isAnswered || current.isFinished) return
        val question = current.currentQuestion ?: return
        if (current.selectedChoiceIndices.contains(choiceIndex)) return
        if (current.selectedWords.size >= 4) return
        val word = question.shuffledChoices.getOrNull(choiceIndex) ?: return
        val newWords = current.selectedWords + word
        val newIndices = current.selectedChoiceIndices + choiceIndex
        if (newWords.size == 4) {
            val isCorrect = newWords == question.answers
            _state.value = current.copy(
                selectedWords = newWords,
                selectedChoiceIndices = newIndices,
                isAnswered = true,
                isCorrect = isCorrect,
                correctCount = current.correctCount + if (isCorrect) 1 else 0,
                wrongCount = current.wrongCount + if (isCorrect) 0 else 1
            )
        } else {
            _state.value = current.copy(selectedWords = newWords, selectedChoiceIndices = newIndices)
        }
    }

    fun undoLastWord() {
        val current = _state.value
        if (current.isAnswered || current.selectedWords.isEmpty()) return
        _state.value = current.copy(
            selectedWords = current.selectedWords.dropLast(1),
            selectedChoiceIndices = current.selectedChoiceIndices.dropLast(1)
        )
    }

    fun nextQuestion() {
        val current = _state.value
        if (!current.isAnswered) return
        if (current.currentIndex >= current.questions.lastIndex) {
            viewModelScope.launch {
                val r = repository.finishSentenceQuiz(
                    startedAt = current.startedAt,
                    correctCount = current.correctCount,
                    totalQuestions = current.questions.size
                )
                _state.value = current.copy(result = r)
            }
        } else {
            _state.value = current.copy(
                currentIndex = current.currentIndex + 1,
                selectedWords = emptyList(),
                selectedChoiceIndices = emptyList(),
                isAnswered = false,
                isCorrect = null
            )
        }
    }
}
@HiltViewModel
class AddSentenceViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun save(sentence: String, meaning: String) {
        if (sentence.isBlank() || meaning.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.addCustomSentence(sentence.trim(), meaning.trim())
            }.onSuccess {
                _saved.value = true
            }.onFailureUnlessCancellation {}
        }
    }

    fun resetSaved() { _saved.value = false }
}

@HiltViewModel
class CustomSentenceListViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    val sentences: StateFlow<List<CustomSentenceEntity>> = repository.observeCustomSentences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Int) {
        viewModelScope.launch { repository.deleteCustomSentence(id) }
    }
}
