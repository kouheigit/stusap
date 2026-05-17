package com.example.vocabapp.domain.usecase.custom

import com.example.vocabapp.data.repository.CustomContentRepository
import javax.inject.Inject

class BuildCustomQuizUseCase @Inject constructor(
    private val repository: CustomContentRepository
) {
    suspend fun training(type: String, setNumber: Int) = repository.buildCustomTrainingQuiz(type, setNumber)
    suspend fun random(type: String) = repository.buildRandomCustomQuiz(type)
}
