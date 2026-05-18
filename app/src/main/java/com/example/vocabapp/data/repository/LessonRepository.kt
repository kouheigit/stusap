package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.LessonEntity
import com.example.vocabapp.domain.model.HomeSummary
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.domain.model.LessonStatus
import com.example.vocabapp.domain.model.Training
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

@Singleton
class LessonRepository @Inject constructor(
    private val dao: AppDao
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeHomeSummary(): Flow<HomeSummary> {
        val base = observeCurrentWeekStartMillis().flatMapLatest { weekStart ->
            combine(
                dao.observeTotalStudySeconds(),
                dao.observeStudySecondsFrom(weekStart),
                dao.observeLessons(),
                dao.observeProgress(),
                dao.observeActiveReviews()
            ) { totalSeconds, weekSeconds, lessons, progress, reviews ->
                val vocabLessons = lessons.filter { it.id < 100 }
                val idiomLessons = lessons.filter { it.id >= 100 }
                val vocabLessonIds = vocabLessons.mapTo(mutableSetOf()) { it.id }
                val idiomLessonIds = idiomLessons.mapTo(mutableSetOf()) { it.id }
                HomeSummary(
                    totalStudySeconds = totalSeconds,
                    weekStudySeconds = weekSeconds,
                    masteredLessons = progress.count {
                        it.trainingId == null && it.isMastered && it.lessonId in vocabLessonIds
                    },
                    totalLessons = vocabLessons.size,
                    reviewCount = reviews.count { it.isActive },
                    idiomMasteredLessons = progress.count {
                        it.trainingId == null && it.isMastered && it.lessonId in idiomLessonIds
                    },
                    idiomTotalLessons = idiomLessons.size
                )
            }
        }
        val withStreak = combine(base, dao.observeStudyDays()) { summary, days ->
            summary.copy(streakDays = calculateStreak(days))
        }
        return combine(withStreak, dao.observeCustomSentences()) { summary, sentences ->
            summary.copy(sentenceCount = sentences.size)
        }
    }

    fun observeLessons(): Flow<List<Lesson>> = observeLessonsFiltered { it.id < 100 }

    fun observeIdiomLessons(): Flow<List<Lesson>> = observeLessonsFiltered { it.id >= 100 }

    fun observeTrainings(lessonId: Int): Flow<List<Training>> =
        combine(dao.observeTrainings(lessonId), dao.observeProgress(), dao.observeFirstWordIds()) {
            trainings, progress, firstWords ->
            val firstWordMap = firstWords.associate { it.trainingId to it.firstId }
            trainings.map { training ->
                val item = progress.firstOrNull { it.trainingId == training.id }
                Training(
                    id = training.id,
                    lessonId = training.lessonId,
                    title = training.title,
                    wordStartNumber = training.wordStartNumber,
                    wordEndNumber = training.wordEndNumber,
                    studyCount = item?.studyCount ?: 0,
                    bestAccuracy = item?.bestAccuracy ?: 0f,
                    bestStarCount = item?.bestStarCount ?: 0,
                    lastStudiedAt = item?.lastStudiedAt,
                    lastAccuracy = item?.let { if (it.lastAccuracy > 0f) it.lastAccuracy else it.bestAccuracy } ?: 0f,
                    firstWordId = firstWordMap[training.id] ?: training.wordStartNumber
                )
            }
        }

    fun observeStudyLogs() = dao.observeStudyLogs()

    suspend fun getTrainingRange(trainingId: Int): String? =
        dao.getTraining(trainingId)?.let { "${it.wordStartNumber}〜${it.wordEndNumber}語" }

    private fun observeCurrentWeekStartMillis(zoneId: ZoneId = ZoneId.systemDefault()): Flow<Long> =
        flow {
            while (true) {
                val now = ZonedDateTime.now(zoneId)
                emit(weekStartMillis(now))
                val nextDayStart = now.toLocalDate().plusDays(1).atStartOfDay(zoneId)
                val delayMillis = Duration.between(now, nextDayStart).toMillis().coerceAtLeast(1_000L)
                delay(delayMillis)
            }
        }.distinctUntilChanged()

    private fun weekStartMillis(now: ZonedDateTime): Long =
        now.with(DayOfWeek.MONDAY)
            .toLocalDate()
            .atStartOfDay(now.zone)
            .toInstant()
            .toEpochMilli()

    private fun observeLessonsFiltered(predicate: (LessonEntity) -> Boolean): Flow<List<Lesson>> =
        combine(dao.observeLessons(), dao.observeProgress()) { lessons, progress ->
            lessons.filter(predicate).map { lesson ->
                val trainingCount = trainingCountForLesson(lesson.id)
                val lessonProgress = progress.filter { it.lessonId == lesson.id && it.trainingId != null }
                val masteredTrainings = lessonProgress.count { it.bestStarCount >= 3 }
                val completedTrainings = lessonProgress.count { it.studyCount > 0 }
                val status = when {
                    masteredTrainings >= trainingCount -> LessonStatus.Master
                    completedTrainings >= trainingCount -> LessonStatus.Complete
                    completedTrainings > 0 -> LessonStatus.InProgress
                    else -> LessonStatus.NotStarted
                }
                Lesson(
                    id = lesson.id,
                    scoreTarget = lesson.scoreTarget,
                    title = lesson.title,
                    wordStartNumber = lesson.wordStartNumber,
                    wordEndNumber = lesson.wordEndNumber,
                    progressRate = masteredTrainings / trainingCount.toFloat(),
                    status = status,
                    masteredTrainings = masteredTrainings,
                    lastStudiedAt = lessonProgress.mapNotNull { it.lastStudiedAt }.maxOrNull()
                )
            }
        }

    private fun trainingCountForLesson(lessonId: Int) = if (lessonId >= 100) 3 else 10

    private fun calculateStreak(studyDays: List<Long>): Int {
        if (studyDays.isEmpty()) return 0
        val todayDay = System.currentTimeMillis() / 86_400_000L
        var streak = 0
        var expected = todayDay
        for (day in studyDays) {
            if (day == expected || (streak == 0 && day == expected - 1)) {
                streak++
                expected = day - 1
            } else {
                break
            }
        }
        return streak
    }
}
