package com.example.vocabapp.domain.usecase.lesson

import com.example.vocabapp.data.repository.LessonRepository
import javax.inject.Inject

class GetTrainingsUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    operator fun invoke(lessonId: Int) = repository.observeTrainings(lessonId)
}
