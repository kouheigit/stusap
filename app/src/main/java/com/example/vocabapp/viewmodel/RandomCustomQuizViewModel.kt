package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizQuestion
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.QuizState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
/**
 * カスタム単語・熟語からランダムに出題するクイズを管理するViewModel。
 */
class RandomCustomQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: CustomContentRepository,
    quizRepository: QuizRepository
) : BaseLoadingCustomQuizViewModel(
    savedStateHandle = savedStateHandle,
    repository = repository,
    quizRepository = quizRepository,
    initialTrainingId = ContentType.fromRouteValue(
        checkNotNull(savedStateHandle["type"])
    ).randomTrainingId
) {
    override suspend fun buildQuiz(): List<QuizQuestion> =
        repository.buildRandomCustomQuiz(contentType)

    override suspend fun finishQuiz(current: QuizState, answers: List<AnswerRecord>): QuizResult =
        quizRepository.finishRandomCustomQuiz(
            type = contentType,
            startedAt = current.startedAt,
            answers = answers,
            questions = current.questions
        )

    override fun loadErrorMessage() = "ランダムクイズの取得に失敗しました"
}
