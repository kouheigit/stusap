package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.QuizAttemptAnswerEntity
import com.example.vocabapp.data.local.entity.QuizAttemptEntity
import com.example.vocabapp.data.local.entity.ReviewWordEntity
import com.example.vocabapp.data.local.entity.StudyLogEntity
import com.example.vocabapp.data.local.entity.UserProgressEntity
import com.example.vocabapp.data.local.entity.WordChoiceEntity
import com.example.vocabapp.data.local.entity.WordEntity
import com.example.vocabapp.data.seed.SeedData
import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.HomeSummary
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.domain.model.LessonStatus
import com.example.vocabapp.domain.model.QuizQuestion
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.domain.model.WordChoice
import com.example.vocabapp.domain.model.WordRelation
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class VocabRepository @Inject constructor(
    private val dao: AppDao
) {
    suspend fun seedIfNeeded() {
        dao.seedIfNeeded(
            SeedData.lessons,
            SeedData.trainings,
            SeedData.words,
            SeedData.choices,
            SeedData.relations
        )
    }

    fun observeHomeSummary(): Flow<HomeSummary> {
        val weekStart = Instant.now()
            .atZone(ZoneId.systemDefault())
            .with(DayOfWeek.MONDAY)
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return combine(
            dao.observeTotalStudySeconds(),
            dao.observeStudySecondsFrom(weekStart),
            dao.observeLessons(),
            dao.observeProgress(),
            dao.observeActiveReviews()
        ) { totalSeconds, weekSeconds, lessons, progress, reviews ->
            HomeSummary(
                totalStudySeconds = totalSeconds,
                weekStudySeconds = weekSeconds,
                masteredLessons = progress.count { it.trainingId == null && it.isMastered },
                totalLessons = lessons.size,
                reviewCount = reviews.count { it.isActive }
            )
        }
    }

    fun observeLessons(): Flow<List<Lesson>> =
        combine(dao.observeLessons(), dao.observeProgress()) { lessons, progress ->
            lessons.map { lesson ->
                val lessonProgress = progress.filter { it.lessonId == lesson.id && it.trainingId != null }
                val masteredTrainings = lessonProgress.count { it.bestStarCount >= 3 }
                val completedTrainings = lessonProgress.count { it.studyCount > 0 }
                val status = when {
                    masteredTrainings == 10 -> LessonStatus.Master
                    completedTrainings == 10 -> LessonStatus.Complete
                    completedTrainings > 0 -> LessonStatus.InProgress
                    else -> LessonStatus.NotStarted
                }
                Lesson(
                    id = lesson.id,
                    scoreTarget = lesson.scoreTarget,
                    title = lesson.title,
                    wordStartNumber = lesson.wordStartNumber,
                    wordEndNumber = lesson.wordEndNumber,
                    progressRate = masteredTrainings / 10f,
                    status = status,
                    masteredTrainings = masteredTrainings,
                    lastStudiedAt = lessonProgress.mapNotNull { it.lastStudiedAt }.maxOrNull()
                )
            }
        }

    fun observeTrainings(lessonId: Int): Flow<List<Training>> =
        combine(dao.observeTrainings(lessonId), dao.observeProgress()) { trainings, progress ->
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
                    lastStudiedAt = item?.lastStudiedAt
                )
            }
        }

    fun observeReviewWords(): Flow<List<Word>> =
        dao.observeReviewWords().map { items -> items.map { it.toDomain() } }

    fun observeStudyLogs(): Flow<List<StudyLogEntity>> = dao.observeStudyLogs()

    suspend fun buildTrainingQuiz(trainingId: Int): Pair<Int?, List<QuizQuestion>> {
        val training = dao.getTraining(trainingId)
        val words = dao.getWordsByTraining(trainingId).take(10)
        return training?.lessonId to words.map { word ->
            QuizQuestion(
                word = word.toDomain(),
                choices = dao.getChoices(word.id).shuffled().map { it.toDomain() }
            )
        }.shuffled()
    }

    suspend fun buildReviewQuiz(): List<QuizQuestion> =
        dao.getReviewQuizWords(10).map { word ->
            QuizQuestion(
                word = word.toDomain(),
                choices = dao.getChoices(word.id).shuffled().map { it.toDomain() }
            )
        }.shuffled()

    suspend fun getWordDetail(wordId: Int): Pair<Word?, List<WordRelation>> {
        val word = dao.getWord(wordId)?.toDomain()
        val relations = dao.getRelations(wordId).map {
            WordRelation(it.relatedWord, it.relatedMeaning)
        }
        return word to relations
    }

    suspend fun finishQuiz(
        trainingId: Int?,
        lessonId: Int?,
        isReview: Boolean,
        startedAt: Long,
        answers: List<AnswerRecord>
    ): Long {
        val finishedAt = System.currentTimeMillis()
        val total = answers.size.coerceAtLeast(1)
        val correct = answers.count { it.isCorrect }
        val wrong = answers.count { !it.isCorrect }
        val accuracy = correct * 100f / total
        val studySeconds = ((finishedAt - startedAt) / 1000).toInt().coerceAtLeast(1)
        val starCount = when {
            accuracy >= 90f -> 3
            accuracy >= 70f -> 2
            accuracy >= 50f -> 1
            else -> 0
        }
        val attemptId = dao.insertQuizAttempt(
            QuizAttemptEntity(
                trainingId = trainingId,
                isReview = isReview,
                startedAt = startedAt,
                finishedAt = finishedAt,
                totalQuestions = total,
                correctCount = correct,
                wrongCount = wrong,
                accuracy = accuracy,
                studySeconds = studySeconds,
                starCount = starCount
            )
        )
        dao.insertQuizAnswers(
            answers.map {
                QuizAttemptAnswerEntity(
                    quizAttemptId = attemptId,
                    wordId = it.wordId,
                    selectedChoiceId = it.selectedChoiceId,
                    isCorrect = it.isCorrect,
                    answeredAt = it.answeredAt,
                    responseMillis = it.responseMillis,
                    selectedUnknown = it.selectedUnknown
                )
            }
        )
        dao.insertStudyLog(
            StudyLogEntity(
                studiedAt = finishedAt,
                lessonId = lessonId,
                trainingId = trainingId,
                studySeconds = studySeconds,
                correctCount = correct,
                wrongCount = wrong
            )
        )
        if (trainingId != null && lessonId != null) {
            updateTrainingProgress(lessonId, trainingId, accuracy, starCount, finishedAt)
            updateLessonMaster(lessonId, finishedAt)
        }
        answers.filter { !it.isCorrect || it.selectedUnknown }.forEach {
            addReviewWord(it.wordId, if (it.selectedUnknown) "unknown" else "wrong")
        }
        answers.filter { isReview && it.isCorrect }.forEach {
            markReviewCorrect(it.wordId, finishedAt)
        }
        return attemptId
    }

    suspend fun getResult(attemptId: Long): QuizResult? =
        dao.getAttempt(attemptId)?.let {
            QuizResult(
                attemptId = it.id,
                trainingId = it.trainingId,
                isReview = it.isReview,
                totalQuestions = it.totalQuestions,
                correctCount = it.correctCount,
                wrongCount = it.wrongCount,
                accuracy = it.accuracy,
                studySeconds = it.studySeconds,
                starCount = it.starCount
            )
        }

    suspend fun addReviewWord(wordId: Int, reason: String = "checked") {
        val now = System.currentTimeMillis()
        val current = dao.getReviewByWordId(wordId)
        if (current == null) {
            dao.insertReviewWord(
                ReviewWordEntity(
                    wordId = wordId,
                    addedReason = reason,
                    isActive = true,
                    addedAt = now,
                    lastReviewedAt = null,
                    wrongCount = if (reason == "checked") 0 else 1,
                    correctCount = 0
                )
            )
        } else {
            dao.updateReviewWord(
                current.copy(
                    addedReason = reason,
                    isActive = true,
                    addedAt = current.addedAt,
                    wrongCount = current.wrongCount + if (reason == "checked") 0 else 1
                )
            )
        }
    }

    suspend fun removeReviewWord(wordId: Int) {
        dao.deactivateReview(wordId)
    }

    suspend fun resetLearningData() {
        dao.resetLearningData()
    }

    private suspend fun markReviewCorrect(wordId: Int, reviewedAt: Long) {
        val current = dao.getReviewByWordId(wordId) ?: return
        dao.updateReviewWord(
            current.copy(
                lastReviewedAt = reviewedAt,
                correctCount = current.correctCount + 1,
                isActive = current.correctCount + 1 < 2
            )
        )
    }

    private suspend fun updateTrainingProgress(
        lessonId: Int,
        trainingId: Int,
        accuracy: Float,
        starCount: Int,
        studiedAt: Long
    ) {
        val current = dao.getProgress(lessonId, trainingId)
        dao.upsertProgress(
            UserProgressEntity(
                id = current?.id ?: 0,
                lessonId = lessonId,
                trainingId = trainingId,
                studyCount = (current?.studyCount ?: 0) + 1,
                bestAccuracy = maxOf(current?.bestAccuracy ?: 0f, accuracy),
                bestStarCount = maxOf(current?.bestStarCount ?: 0, starCount),
                lastStudiedAt = studiedAt,
                isMastered = starCount >= 3 || current?.isMastered == true
            )
        )
    }

    private suspend fun updateLessonMaster(lessonId: Int, studiedAt: Long) {
        // Lesson-level progress is derived live in lists; this row exists for summary counts.
        val current = dao.getProgress(lessonId, null)
        val trainingRows = dao.getTrainingIdsForLesson(lessonId)
        val mastered = trainingRows.all { trainingId ->
            dao.getProgress(lessonId, trainingId)?.bestStarCount == 3
        }
        dao.upsertProgress(
            UserProgressEntity(
                id = current?.id ?: 0,
                lessonId = lessonId,
                trainingId = null,
                studyCount = (current?.studyCount ?: 0) + 1,
                bestAccuracy = current?.bestAccuracy ?: 0f,
                bestStarCount = if (mastered) 3 else current?.bestStarCount ?: 0,
                lastStudiedAt = studiedAt,
                isMastered = mastered
            )
        )
    }

    private fun WordEntity.toDomain(): Word =
        Word(
            id = id,
            trainingId = trainingId,
            english = english,
            meaning = meaning,
            phonetic = phonetic,
            partOfSpeech = partOfSpeech,
            exampleSentence = exampleSentence,
            exampleTranslation = exampleTranslation,
            audioUrl = audioUrl,
            exampleAudioUrl = exampleAudioUrl,
            displayOrder = displayOrder
        )

    private fun WordChoiceEntity.toDomain(): WordChoice =
        WordChoice(
            id = id,
            wordId = wordId,
            choiceText = choiceText,
            isCorrect = isCorrect,
            displayOrder = displayOrder
        )
}
