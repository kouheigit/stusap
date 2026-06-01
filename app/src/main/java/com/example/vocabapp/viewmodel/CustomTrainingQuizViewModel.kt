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
 * 10件単位のカスタムトレーニングクイズを管理するViewModel。
 */
class CustomTrainingQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: CustomContentRepository,
    quizRepository: QuizRepository
) : BaseLoadingCustomQuizViewModel(
    savedStateHandle = savedStateHandle,
    repository = repository,
    quizRepository = quizRepository,
    initialTrainingId = ContentType.fromRouteValue(
        checkNotNull(savedStateHandle["type"])
    ).trainingId(checkNotNull(savedStateHandle["setNumber"]))
) {
    val setNumber: Int = checkNotNull(savedStateHandle["setNumber"])

    init {
        loadQuiz()
    }

    override suspend fun buildQuiz(): List<QuizQuestion> =
        repository.buildCustomTrainingQuiz(contentType, setNumber)

    override suspend fun finishQuiz(current: QuizState, answers: List<AnswerRecord>): QuizResult =
        quizRepository.finishCustomQuiz(
            type = contentType,
            setNumber = setNumber,
            startedAt = current.startedAt,
            answers = answers,
            questions = current.questions
        )

    override fun loadErrorMessage() = "カスタムクイズの取得に失敗しました"
}
