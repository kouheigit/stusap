package com.example.vocabapp.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepository @Inject constructor(
    private val vocabRepository: VocabRepository
) {
    fun observeHomeSummary() = vocabRepository.observeHomeSummary()
    fun observeLessons() = vocabRepository.observeLessons()
    fun observeIdiomLessons() = vocabRepository.observeIdiomLessons()
    fun observeTrainings(lessonId: Int) = vocabRepository.observeTrainings(lessonId)
    fun observeStudyLogs() = vocabRepository.observeStudyLogs()
    suspend fun getTrainingRange(trainingId: Int) = vocabRepository.getTrainingRange(trainingId)
}
