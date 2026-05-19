package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.QuizState
import com.example.vocabapp.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * カスタム単語・熟語からランダムに出題するクイズを管理するViewModel。
 */
class RandomCustomQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CustomContentRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {
    val contentType: String = checkNotNull(savedStateHandle["type"])
    private val contentTypeEnum = ContentType.fromRouteValue(contentType)
    private val _state = MutableStateFlow(
        QuizState(
            trainingId = contentTypeEnum.randomTrainingId,
            lessonId = contentTypeEnum.lessonId
        )
    )
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val _loadState = MutableStateFlow<UiState<QuizState>>(UiState.Loading)
    val loadState: StateFlow<UiState<QuizState>> = _loadState.asStateFlow()
    private val _result = MutableStateFlow<QuizResult?>(null)
    val result: StateFlow<QuizResult?> = _result.asStateFlow()
    private val quizSession = QuizSession(viewModelScope, _state) { current, answers ->
        val quizResult = quizRepository.finishRandomCustomQuiz(
            type = contentType,
            startedAt = current.startedAt,
            answers = answers,
            questions = current.questions
        )
        _result.value = quizResult
        _state.value = current.copy(finishedAttemptId = quizResult.attemptId)
    }

    init {
        viewModelScope.launch {
            _loadState.value = UiState.Loading
            runCatching {
                val startedAt = System.currentTimeMillis()
                val questions = repository.buildRandomCustomQuiz(contentType)
                _state.value = _state.value.copy(questions = questions, startedAt = startedAt)
                quizSession.resetQuestionTimer()
                if (questions.isNotEmpty()) quizSession.startTimer()
                _state.value
            }.onSuccess { quizState ->
                _loadState.value = UiState.Success(quizState)
            }.onFailureUnlessCancellation { throwable ->
                _loadState.value = UiState.Error("ランダムクイズの取得に失敗しました", throwable)
            }
        }
    }

    fun onScreenStarted() = quizSession.resumeTimerIfNeeded()

    fun onScreenStopped() = quizSession.pauseTimer()

    fun submit(choiceId: Int?) = quizSession.submit(choiceId)
}
