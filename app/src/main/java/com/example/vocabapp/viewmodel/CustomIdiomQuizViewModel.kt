package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * カスタム英熟語全体から出題するクイズを管理するViewModel。
 */
class CustomIdiomQuizViewModel @Inject constructor(
    private val repository: CustomContentRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val quizSession = QuizSession(viewModelScope, _state) { current, answers ->
        val quizResult = quizRepository.finishRandomCustomQuiz(
            type = ContentType.IDIOM.routeValue,
            startedAt = current.startedAt,
            answers = answers,
            questions = current.questions
        )
        _state.value = current.copy(finishedAttemptId = quizResult.attemptId)
    }

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val questions = repository.buildCustomIdiomQuiz()
            _state.value = QuizState(questions = questions, startedAt = startedAt)
            quizSession.resetQuestionTimer()
            if (questions.isNotEmpty()) quizSession.startTimer()
        }
    }

    fun onScreenStarted() = quizSession.resumeTimerIfNeeded()

    fun onScreenStopped() = quizSession.pauseTimer()

    fun submit(choiceId: Int?) = quizSession.submit(choiceId)
}

