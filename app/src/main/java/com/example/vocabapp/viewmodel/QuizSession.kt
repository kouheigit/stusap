package com.example.vocabapp.viewmodel

import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.QuizConstants
import com.example.vocabapp.domain.model.QuizState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * クイズの進行制御（タイマー・回答受付・次問移行・終了処理）を担う内部クラス。
 *
 * ViewModel から切り出すことで、クイズ種別をまたいで同一のタイマーロジックを再利用できる。
 * タイマーは画面の onStop/onStart に連動して一時停止・再開し、応答時間を正確に計測する。
 */
internal class QuizSession(
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<QuizState>,
    private val onFinish: suspend (QuizState, List<AnswerRecord>) -> Unit
) {
    private val answers = mutableListOf<AnswerRecord>()
    private var questionStartedAt = System.currentTimeMillis()
    private var timerJob: Job? = null
    private var isTimerActive = false
    private var pausedAt: Long? = null

    fun resetQuestionTimer() {
        questionStartedAt = System.currentTimeMillis()
        pausedAt = null
    }

    fun startTimer() {
        isTimerActive = true
        pausedAt?.let { paused ->
            // 一時停止していた時間だけ開始時刻を後ろにずらし、応答時間の計測を正確に保つ
            questionStartedAt += System.currentTimeMillis() - paused
            pausedAt = null
        }
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

    fun pauseTimer() {
        isTimerActive = false
        // 既に pausedAt が記録されている場合は上書きしない（二重停止で開始時刻がずれるのを防ぐ）
        if (pausedAt == null) {
            pausedAt = System.currentTimeMillis()
        }
        timerJob?.cancel()
        timerJob = null
    }

    fun resumeTimerIfNeeded() {
        val current = state.value
        // 停止中 かつ 問題が存在 かつ 未回答 かつ 未完了 のときだけ再開する
        // 画面復帰時に回答済み・終了済みの状態でタイマーが誤起動するのを防ぐ
        if (!isTimerActive && current.questions.isNotEmpty() && !current.isAnswered && !current.isFinished) {
            startTimer()
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
            // 正誤フィードバックをユーザーが視認できる時間（900ms）を確保してから次へ進む
            delay(900)
            nextOrFinish()
        }
    }

    private suspend fun nextOrFinish() {
        val current = state.value
        if (current.currentIndex >= current.questions.lastIndex) {
            pauseTimer()
            onFinish(current, answers.toList())
        } else {
            resetQuestionTimer()
            state.value = current.copy(
                currentIndex = current.currentIndex + 1,
                selectedChoiceId = null,
                isAnswered = false,
                isCorrect = null,
                remainingMillis = QuizConstants.TIMER_MILLIS
            )
        }
    }
}
