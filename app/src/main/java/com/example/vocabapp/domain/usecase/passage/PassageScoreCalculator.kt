package com.example.vocabapp.domain.usecase.passage

import com.example.vocabapp.domain.model.PassageQuestion
import com.example.vocabapp.domain.model.PassageScore
import javax.inject.Inject

/**
 * 設問と選択結果を突き合わせてスコアを算出するUseCase。
 *
 * 採点条件をセッション・Repository・ViewModel に分散させないための単一の境界。
 * 未回答（選択 null）は不正解として扱う。
 */
class PassageScoreCalculator @Inject constructor() {
    /**
     * 設問群と回答選択からスコアを算出する。
     *
     * @param questions 採点対象の設問
     * @param selections 設問ごとの選択インデックス（未回答は null）
     * @return 正答率を含むスコア
     */
    fun calculate(
        questions: List<PassageQuestion>,
        selections: List<Int?>
    ): PassageScore {
        val total = questions.size
        val correct = questions.withIndex().count { (index, question) ->
            selections.getOrNull(index) == question.answerIndex
        }
        val accuracy = if (total == 0) 0f else correct * 100f / total
        return PassageScore(
            total = total,
            correct = correct,
            wrong = total - correct,
            accuracy = accuracy
        )
    }
}
