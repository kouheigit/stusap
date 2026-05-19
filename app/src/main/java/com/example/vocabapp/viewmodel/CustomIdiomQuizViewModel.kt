package com.example.vocabapp.viewmodel

import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
/**
 * カスタム英熟語全体から出題するクイズを管理するViewModel。
 */
class CustomIdiomQuizViewModel @Inject constructor(
    repository: CustomContentRepository,
    quizRepository: QuizRepository
) : BaseSimpleCustomQuizViewModel(repository, quizRepository) {

    override val contentType = ContentType.IDIOM

    override suspend fun buildQuiz(): List<QuizQuestion> = repository.buildCustomIdiomQuiz()
}
