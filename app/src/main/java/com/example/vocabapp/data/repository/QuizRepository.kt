package com.example.vocabapp.data.repository

import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.QuizQuestion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val vocabRepository: VocabRepository
) {
    suspend fun buildTrainingQuiz(trainingId: Int) = vocabRepository.buildTrainingQuiz(trainingId)
    suspend fun buildReviewQuiz() = vocabRepository.buildReviewQuiz()
    suspend fun finishQuiz(trainingId: Int?, lessonId: Int?, isReview: Boolean, startedAt: Long, answers: List<AnswerRecord>) =
        vocabRepository.finishQuiz(trainingId, lessonId, isReview, startedAt, answers)
    suspend fun getResult(attemptId: Long) = vocabRepository.getResult(attemptId)
    suspend fun buildSentenceQuiz() = vocabRepository.buildSentenceQuiz()
    suspend fun finishSentenceQuiz(startedAt: Long, correctCount: Int, totalQuestions: Int) =
        vocabRepository.finishSentenceQuiz(startedAt, correctCount, totalQuestions)
    suspend fun finishCustomQuiz(type: String, setNumber: Int, startedAt: Long, answers: List<AnswerRecord>, questions: List<QuizQuestion>) =
        vocabRepository.finishCustomQuiz(type, setNumber, startedAt, answers, questions)
    suspend fun finishRandomCustomQuiz(type: String, startedAt: Long, answers: List<AnswerRecord>, questions: List<QuizQuestion>) =
        vocabRepository.finishRandomCustomQuiz(type, startedAt, answers, questions)
}
