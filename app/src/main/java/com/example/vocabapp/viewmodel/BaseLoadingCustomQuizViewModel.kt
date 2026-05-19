package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizQuestion
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.QuizState
import com.example.vocabapp.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ローディング状態・結果・エラーを持つカスタムクイズ用ViewModel基底クラス。
 *
 * サブクラスは initialTrainingId / buildQuiz / finishQuiz / loadErrorMessage を
 * 実装するだけで新しいクイズ種別に対応できる。
 */
abstract class BaseLoadingCustomQuizViewModel(
    savedStateHandle: SavedStateHandle,
    protected val repository: CustomContentRepository,
    protected val quizRepository: QuizRepository,
    initialTrainingId: Int
) : ViewModel() {

    val contentType: String = checkNotNull(savedStateHandle["type"])
    protected val contentTypeEnum: ContentType = ContentType.fromRouteValue(contentType)

    private val _state = MutableStateFlow(
        QuizState(trainingId = initialTrainingId, lessonId = contentTypeEnum.lessonId)
    )
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private val _loadState = MutableStateFlow<UiState<QuizState>>(UiState.Loading)
    val loadState: StateFlow<UiState<QuizState>> = _loadState.asStateFlow()

    private val _result = MutableStateFlow<QuizResult?>(null)
    val result: StateFlow<QuizResult?> = _result.asStateFlow()

    private val quizSession = QuizSession(viewModelScope, _state) { current, answers ->
        val quizResult = finishQuiz(current, answers)
        _result.value = quizResult
        _state.value = current.copy(finishedAttemptId = quizResult.attemptId)
    }

    init {
        viewModelScope.launch {
            _loadState.value = UiState.Loading
            runCatching {
                val startedAt = System.currentTimeMillis()
                val questions = buildQuiz()
                _state.value = _state.value.copy(questions = questions, startedAt = startedAt)
                quizSession.resetQuestionTimer()
                if (questions.isNotEmpty()) quizSession.startTimer()
                _state.value
            }.onSuccess { quizState ->
                _loadState.value = UiState.Success(quizState)
            }.onFailureUnlessCancellation { throwable ->
                _loadState.value = UiState.Error(loadErrorMessage(), throwable)
            }
        }
    }

    protected abstract suspend fun buildQuiz(): List<QuizQuestion>
    protected abstract suspend fun finishQuiz(current: QuizState, answers: List<AnswerRecord>): QuizResult
    protected abstract fun loadErrorMessage(): String

    fun onScreenStarted() = quizSession.resumeTimerIfNeeded()
    fun onScreenStopped() = quizSession.pauseTimer()
    fun submit(choiceId: Int?) = quizSession.submit(choiceId)
}
