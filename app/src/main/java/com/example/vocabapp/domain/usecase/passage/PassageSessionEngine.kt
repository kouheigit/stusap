package com.example.vocabapp.domain.usecase.passage

import com.example.vocabapp.domain.model.PassageConstants
import com.example.vocabapp.domain.model.PassageSet
import com.example.vocabapp.domain.model.PassageSessionState
import javax.inject.Inject

/**
 * 長文読解クイズのセッション状態遷移を担う純粋な（副作用のない）エンジン。
 *
 * 各メソッドは現在の状態から次の状態を返すだけで、コルーチンやタイマー駆動を持たない。
 * 実際の時間経過は UI 層が [tick] を周期的に呼ぶことで表現する。これにより
 * 状態遷移ロジックを ViewModel から独立して単体テストできる。
 */
class PassageSessionEngine @Inject constructor(
    private val scoreCalculator: PassageScoreCalculator
) {
    /** セットの先頭からセッションを開始する。選択は全設問分の未回答で初期化する。 */
    fun start(set: PassageSet): PassageSessionState = PassageSessionState(
        setId = set.id,
        currentIndex = 0,
        selections = List(set.questions.size) { null },
        remainingSec = set.timeLimitSec ?: PassageConstants.DEFAULT_TIME_LIMIT_SEC,
        finished = false,
        score = null
    )

    /**
     * 現在の設問に選択肢を記録する。終了済みなら何もしない。
     *
     * 「選択」操作であり、まだ次の設問へは進まない（解答確定は [next]）。
     */
    fun select(state: PassageSessionState, choiceIndex: Int): PassageSessionState {
        if (state.finished) return state
        val updated = state.selections.toMutableList()
        updated[state.currentIndex] = choiceIndex
        return state.copy(selections = updated)
    }

    /**
     * 次の設問へ進む。最終設問で呼ばれた場合は終了し、スコアを確定する。
     * 終了済みなら何もしない。
     */
    fun next(state: PassageSessionState, set: PassageSet): PassageSessionState {
        if (state.finished) return state
        return if (state.currentIndex >= set.questions.lastIndex) {
            finish(state, set)
        } else {
            state.copy(currentIndex = state.currentIndex + 1)
        }
    }

    /**
     * 時間を [seconds] 秒進める。残り時間が 0 に達したらタイマー満了として終了し、
     * スコアを確定する。終了済みなら何もしない。
     */
    fun tick(state: PassageSessionState, set: PassageSet, seconds: Int = 1): PassageSessionState {
        if (state.finished) return state
        val remaining = (state.remainingSec - seconds).coerceAtLeast(0)
        return if (remaining == 0) {
            finish(state.copy(remainingSec = 0), set)
        } else {
            state.copy(remainingSec = remaining)
        }
    }

    private fun finish(state: PassageSessionState, set: PassageSet): PassageSessionState =
        state.copy(
            finished = true,
            score = scoreCalculator.calculate(set.questions, state.selections)
        )
}
