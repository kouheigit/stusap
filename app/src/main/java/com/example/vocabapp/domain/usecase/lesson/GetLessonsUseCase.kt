package com.example.vocabapp.domain.usecase.lesson

import com.example.vocabapp.data.repository.LessonRepository
import javax.inject.Inject

class GetLessonsUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    operator fun invoke() = repository.observeLessons()
}

class GetIdiomLessonsUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    operator fun invoke() = repository.observeIdiomLessons()
}
