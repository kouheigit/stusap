package com.example.vocabapp.viewmodel

import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.QuizState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class QuizSession(
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<QuizState>,
    private val onFinish: suspend (QuizState, List<AnswerRecord>) -> Unit
) {
    private val answers = mutableListOf<AnswerRecord>()
    private var questionStartedAt = System.currentTimeMillis()
    private var timerJob: Job? = null

    fun resetQuestionTimer() {
        questionStartedAt = System.currentTimeMillis()
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                val current = state.value
                if (!current.isAnswered && !current.isFinished) {
                    val next = (current.remainingMillis - 1000L).coerceAtLeast(0L)
                    state.value = current.copy(remainingMillis = next)
                    if (next == 0L) submit(null)
                }
            }
        }
    }

    fun submit(choiceId: Int?) {
        val current = state.value
        if (current.isAnswered || current.isFinished) return
        val question = current.currentQuestion ?: return
        val correctChoice = question.choices.firstOrNull { it.isCorrect }
        val isCorrect = choiceId != null && correctChoice?.id == choiceId
        val now = System.currentTimeMillis()
        answers += AnswerRecord(
            wordId = question.word.id,
            selectedChoiceId = choiceId,
            isCorrect = isCorrect,
            answeredAt = now,
            responseMillis = (now - questionStartedAt).toInt(),
            selectedUnknown = choiceId == null
        )
        state.value = current.copy(
            selectedChoiceId = choiceId,
            isAnswered = true,
            isCorrect = isCorrect,
            correctCount = current.correctCount + if (isCorrect) 1 else 0,
            wrongCount = current.wrongCount + if (isCorrect) 0 else 1
        )
        scope.launch {
            delay(900)
            nextOrFinish()
        }
    }

    private suspend fun nextOrFinish() {
        val current = state.value
        if (current.currentIndex >= current.questions.lastIndex) {
            timerJob?.cancel()
            onFinish(current, answers.toList())
        } else {
            resetQuestionTimer()
            state.value = current.copy(
                currentIndex = current.currentIndex + 1,
                selectedChoiceId = null,
                isAnswered = false,
                isCorrect = null,
                remainingMillis = 30000L
            )
        }
    }
}
