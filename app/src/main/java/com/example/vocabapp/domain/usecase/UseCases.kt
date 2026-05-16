package com.example.vocabapp.domain.usecase

import com.example.vocabapp.data.repository.VocabRepository
import com.example.vocabapp.domain.model.AnswerRecord
import javax.inject.Inject

class GetLessonsUseCase @Inject constructor(
    private val repository: VocabRepository
) {
    operator fun invoke() = repository.observeLessons()
}

class GetIdiomLessonsUseCase @Inject constructor(
    private val repository: VocabRepository
) {
    operator fun invoke() = repository.observeIdiomLessons()
}

class GetTrainingsUseCase @Inject constructor(
    private val repository: VocabRepository
) {
    operator fun invoke(lessonId: Int) = repository.observeTrainings(lessonId)
}

class StartQuizUseCase @Inject constructor(
    private val repository: VocabRepository
) {
    suspend fun training(trainingId: Int) = repository.buildTrainingQuiz(trainingId)
    suspend fun review() = repository.buildReviewQuiz()
}

class FinishQuizUseCase @Inject constructor(
    private val repository: VocabRepository
) {
    suspend operator fun invoke(
        trainingId: Int?,
        lessonId: Int?,
        isReview: Boolean,
        startedAt: Long,
        answers: List<AnswerRecord>
    ) = repository.finishQuiz(trainingId, lessonId, isReview, startedAt, answers)
}

class AddReviewWordUseCase @Inject constructor(
    private val repository: VocabRepository
) {
    suspend operator fun invoke(wordId: Int) = repository.addReviewWord(wordId)
}

class GetReviewWordsUseCase @Inject constructor(
    private val repository: VocabRepository
) {
    operator fun invoke() = repository.observeReviewWords()
}
