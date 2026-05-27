package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizQuestion
import com.example.vocabapp.domain.model.QuizState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * カスタムコンテンツの全件ランダムクイズ用ViewModel基底クラス。
 *
 * ローディング状態や結果ナビゲーションを持たない単純なクイズフロー共通実装。
 * サブクラスは contentType と buildQuiz() を実装するだけでよい。
 */
abstract class BaseSimpleCustomQuizViewModel(
    protected val repository: CustomContentRepository,
    protected val quizRepository: QuizRepository
) : ViewModel() {

    protected abstract val contentType: ContentType
    protected abstract suspend fun buildQuiz(): List<QuizQuestion>

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private val quizSession = QuizSession(viewModelScope, _state) { current, answers ->
        val result = quizRepository.finishRandomCustomQuiz(
            type = contentType.routeValue,
            startedAt = current.startedAt,
            answers = answers,
            questions = current.questions
        )
        _state.value = current.copy(finishedAttemptId = result.attemptId)
    }

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val questions = buildQuiz()
            _state.value = QuizState(questions = questions, startedAt = startedAt)
            quizSession.resetQuestionTimer()
            if (questions.isNotEmpty()) quizSession.startTimer()
        }
    }

    fun onScreenStarted() = quizSession.resumeTimerIfNeeded()
    fun onScreenStopped() = quizSession.pauseTimer()
    fun submit(choiceId: Int?) = quizSession.submit(choiceId)

    override fun onCleared() {
        quizSession.clear()
        _state.value = QuizState()
        super.onCleared()
    }
}
