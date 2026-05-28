package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.LessonRepository
import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.data.repository.customContentTypeForTrainingId
import com.example.vocabapp.data.repository.customTrainingSetNumber
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.QuizState
import com.example.vocabapp.domain.usecase.quiz.FinishQuizUseCase
import com.example.vocabapp.domain.usecase.quiz.StartQuizUseCase
import com.example.vocabapp.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * 通常レッスンと復習クイズの進行状態を管理するViewModel。
 *
 * 問題取得、回答送信、タイマー更新、完了時の結果保存を担当する。
 */
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startQuizUseCase: StartQuizUseCase,
    private val finishQuizUseCase: FinishQuizUseCase
) : ViewModel() {
    private val trainingId: Int? = savedStateHandle.get<Int>("trainingId")?.takeIf { it > 0 }
    private val isReview: Boolean = savedStateHandle["isReview"] ?: false
    private val _state = MutableStateFlow(QuizState(isReview = isReview, trainingId = trainingId))
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val _loadState = MutableStateFlow<UiState<QuizState>>(UiState.Loading)
    val loadState: StateFlow<UiState<QuizState>> = _loadState.asStateFlow()
    private val quizSession = QuizSession(viewModelScope, _state) { current, answers ->
        val attemptId = finishQuizUseCase(
            trainingId = current.trainingId,
            lessonId = current.lessonId,
            isReview = current.isReview,
            startedAt = current.startedAt,
            answers = answers
        )
        _state.value = current.copy(finishedAttemptId = attemptId)
    }

    init {
        viewModelScope.launch {
            _loadState.value = UiState.Loading
            runCatching {
                val startedAt = System.currentTimeMillis()
                if (isReview) {
                    val questions = startQuizUseCase.review()
                    _state.value = _state.value.copy(questions = questions, startedAt = startedAt)
                } else if (trainingId != null) {
                    val (lessonId, questions) = startQuizUseCase.training(trainingId)
                    _state.value = _state.value.copy(
                        questions = questions,
                        startedAt = startedAt,
                        lessonId = lessonId
                    )
                }
                quizSession.resetQuestionTimer()
                if (_state.value.questions.isNotEmpty()) quizSession.startTimer()
                _state.value
            }.onSuccess { quizState ->
                _loadState.value = UiState.Success(quizState)
            }.onFailureUnlessCancellation { throwable ->
                _loadState.value = UiState.Error(
                    message = "クイズの取得に失敗しました",
                    throwable = throwable
                )
            }
        }
    }

    fun onScreenStarted() = quizSession.resumeTimerIfNeeded()

    fun onScreenStopped() = quizSession.pauseTimer()

    /**
     * 選択した回答を現在の問題へ送信する。
     *
     * @param choiceId 選択肢ID。未選択回答の場合はnull。
     */
    fun submit(choiceId: Int?) = quizSession.submit(choiceId)

    override fun onCleared() {
        quizSession.clear()
        _state.value = QuizState(isReview = isReview, trainingId = trainingId)
        _loadState.value = UiState.Loading
        super.onCleared()
    }
}

@HiltViewModel
/**
 * 結果画面で表示する採点結果とトレーニング名を取得するViewModel。
 */
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    private val lessonRepository: LessonRepository,
    private val customContentRepository: CustomContentRepository
) : ViewModel() {
    private val attemptId: Long = checkNotNull(savedStateHandle["attemptId"])
    private val _result = MutableStateFlow<QuizResult?>(null)
    val result: StateFlow<QuizResult?> = _result.asStateFlow()
    private val _resultState = MutableStateFlow<UiState<QuizResult?>>(UiState.Loading)
    val resultState: StateFlow<UiState<QuizResult?>> = _resultState.asStateFlow()
    private val _trainingLabel = MutableStateFlow<String?>(null)
    val trainingLabel: StateFlow<String?> = _trainingLabel.asStateFlow()
    private val _hasNextCustomSet = MutableStateFlow(false)
    val hasNextCustomSet: StateFlow<Boolean> = _hasNextCustomSet.asStateFlow()

    init {
        viewModelScope.launch {
            _resultState.value = UiState.Loading
            runCatching {
                val quizResult = quizRepository.getResult(attemptId)
                _result.value = quizResult
                quizResult?.trainingId?.let { trainingId ->
                    _trainingLabel.value = lessonRepository.getTrainingRange(trainingId)
                    val customType = customContentTypeForTrainingId(trainingId)
                    if (customType != null) {
                        val nextSet = customTrainingSetNumber(customType.routeValue, trainingId) + 1
                        _hasNextCustomSet.value = customContentRepository.hasCustomTrainingSet(
                            customType.routeValue,
                            nextSet
                        )
                    }
                }
                quizResult
            }.onSuccess { quizResult ->
                _resultState.value = UiState.Success(quizResult)
            }.onFailureUnlessCancellation { throwable ->
                _resultState.value = UiState.Error(
                    message = "クイズ結果の取得に失敗しました",
                    throwable = throwable
                )
            }
        }
    }
}
