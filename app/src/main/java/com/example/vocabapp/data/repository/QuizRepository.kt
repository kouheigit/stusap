package com.example.vocabapp.data.repository

import androidx.room.withTransaction
import com.example.vocabapp.data.local.AppDatabase
import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.QuizAttemptEntity
import com.example.vocabapp.data.local.entity.ReviewWordEntity
import com.example.vocabapp.data.local.entity.StudyLogEntity
import com.example.vocabapp.data.local.entity.UserProgressEntity
import com.example.vocabapp.data.local.entity.WordEntity
import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.QuizQuestion
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.SentenceQuestion
import com.example.vocabapp.domain.model.SentenceQuizResult
import com.example.vocabapp.domain.model.WordChoice
import com.example.vocabapp.domain.usecase.quiz.QuizScoreCalculator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val database: AppDatabase,
    private val quizScoreCalculator: QuizScoreCalculator,
    private val runtime: QuizRuntime
) {
    private val dao: AppDao = database.appDao()

    suspend fun buildTrainingQuiz(trainingId: Int): Pair<Int?, List<QuizQuestion>> {
        val training = dao.getTraining(trainingId)
        val words = dao.getWordsByTraining(trainingId).take(10)
        return training?.lessonId to words.map { word ->
            QuizQuestion(
                word = word.toDomain(),
                choices = runtime.shuffled(dao.getChoices(word.id)).map { it.toDomain() }
            )
        }.let(runtime::shuffled)
    }

    suspend fun buildReviewQuiz(): List<QuizQuestion> {
        val words = dao.getReviewQuizWords(10)
        return words.mapNotNull { word ->
            val savedChoices = dao.getChoices(word.id).map { it.toDomain() }
            val choices = runtime.shuffled(savedChoices.ifEmpty { buildReviewFallbackChoices(word, words) })
                .mapIndexed { index, choice -> choice.copy(displayOrder = index) }
            if (choices.isEmpty()) {
                null
            } else {
                QuizQuestion(word = word.toDomain(), choices = choices)
            }
        }.let(runtime::shuffled)
    }

    suspend fun finishQuiz(
        trainingId: Int?,
        lessonId: Int?,
        isReview: Boolean,
        startedAt: Long,
        answers: List<AnswerRecord>
    ): Long = database.withTransaction {
        val finishedAt = runtime.nowMillis()
        val score = quizScoreCalculator.calculate(startedAt, finishedAt, answers.count { it.isCorrect }, answers.size)
        val attemptId = dao.insertQuizAttempt(
            QuizAttemptEntity(
                trainingId = trainingId,
                isReview = isReview,
                startedAt = startedAt,
                finishedAt = finishedAt,
                totalQuestions = score.total,
                correctCount = score.correct,
                wrongCount = score.wrong,
                accuracy = score.accuracy,
                studySeconds = score.studySeconds,
                starCount = score.starCount
            )
        )
        dao.insertQuizAnswers(answers.map { answer -> answer.toAnswerEntity(attemptId, dao.getWord(answer.wordId)?.toDomain()) })
        dao.insertStudyLog(
            StudyLogEntity(
                studiedAt = finishedAt,
                lessonId = lessonId,
                trainingId = trainingId,
                studySeconds = score.studySeconds,
                correctCount = score.correct,
                wrongCount = score.wrong
            )
        )
        if (trainingId != null && lessonId != null) {
            updateTrainingProgress(lessonId, trainingId, score.accuracy, score.starCount, finishedAt)
            updateLessonMaster(lessonId, finishedAt)
        }
        answers.filter { !it.isCorrect || it.selectedUnknown }.forEach {
            addReviewWord(it.wordId, if (it.selectedUnknown) "unknown" else "wrong")
        }
        answers.filter { isReview && it.isCorrect }.forEach {
            markReviewCorrect(it.wordId, finishedAt)
        }
        attemptId
    }

    suspend fun getResult(attemptId: Long): QuizResult? =
        dao.getAttempt(attemptId)?.let { attempt ->
            QuizResult(
                attemptId = attempt.id,
                trainingId = attempt.trainingId,
                isReview = attempt.isReview,
                totalQuestions = attempt.totalQuestions,
                correctCount = attempt.correctCount,
                wrongCount = attempt.wrongCount,
                accuracy = attempt.accuracy,
                studySeconds = attempt.studySeconds,
                starCount = attempt.starCount,
                wrongWords = dao.getWrongAnswersForAttempt(attemptId).mapNotNull { it.toSnapshotWord() }
            )
        }

    suspend fun finishCustomQuiz(
        type: String,
        setNumber: Int,
        startedAt: Long,
        answers: List<AnswerRecord>,
        questions: List<QuizQuestion>
    ): QuizResult {
        val lessonId = customLessonId(type)
        val trainingId = customTrainingId(type, setNumber)
        return finishCustomQuizInternal(lessonId, trainingId, startedAt, answers, questions, updateProgress = true)
    }

    suspend fun finishRandomCustomQuiz(
        type: String,
        startedAt: Long,
        answers: List<AnswerRecord>,
        questions: List<QuizQuestion>
    ): QuizResult {
        val lessonId = customLessonId(type)
        val trainingId = randomCustomTrainingId(type)
        return finishCustomQuizInternal(lessonId, trainingId, startedAt, answers, questions, updateProgress = false)
    }

    suspend fun buildSentenceQuiz(setNumber: Int? = null): List<SentenceQuestion> {
        val all = dao.getAllCustomSentences()
        if (all.isEmpty()) return emptyList()
        val targets = if (setNumber != null) {
            all.drop((setNumber - 1).coerceAtLeast(0) * 10).take(10)
        } else {
            runtime.shuffled(all).take(minOf(10, all.size))
        }
        return targets.mapNotNull { buildSentenceQuestion(it) }
    }

    suspend fun finishSentenceQuiz(
        startedAt: Long,
        correctCount: Int,
        totalQuestions: Int
    ): SentenceQuizResult = database.withTransaction {
        val finishedAt = runtime.nowMillis()
        val score = quizScoreCalculator.calculate(startedAt, finishedAt, correctCount, totalQuestions)
        dao.insertQuizAttempt(
            QuizAttemptEntity(
                trainingId = CUSTOM_SENTENCE_LESSON_ID,
                isReview = false,
                startedAt = startedAt,
                finishedAt = finishedAt,
                totalQuestions = score.total,
                correctCount = score.correct,
                wrongCount = score.wrong,
                accuracy = score.accuracy,
                studySeconds = score.studySeconds,
                starCount = score.starCount
            )
        )
        dao.insertStudyLog(
            StudyLogEntity(
                studiedAt = finishedAt,
                lessonId = CUSTOM_SENTENCE_LESSON_ID,
                trainingId = null,
                studySeconds = score.studySeconds,
                correctCount = score.correct,
                wrongCount = score.wrong
            )
        )
        SentenceQuizResult(score.total, score.correct, score.wrong, score.accuracy, score.studySeconds, score.starCount)
    }

    private suspend fun finishCustomQuizInternal(
        lessonId: Int,
        trainingId: Int,
        startedAt: Long,
        answers: List<AnswerRecord>,
        questions: List<QuizQuestion>,
        updateProgress: Boolean
    ): QuizResult = database.withTransaction {
        val finishedAt = runtime.nowMillis()
        val score = quizScoreCalculator.calculate(startedAt, finishedAt, answers.count { it.isCorrect }, answers.size)
        val attemptId = dao.insertQuizAttempt(
            QuizAttemptEntity(
                trainingId = trainingId,
                isReview = false,
                startedAt = startedAt,
                finishedAt = finishedAt,
                totalQuestions = score.total,
                correctCount = score.correct,
                wrongCount = score.wrong,
                accuracy = score.accuracy,
                studySeconds = score.studySeconds,
                starCount = score.starCount
            )
        )
        val questionMap = questions.associateBy { it.word.id }
        dao.insertQuizAnswers(answers.map { answer -> answer.toAnswerEntity(attemptId, questionMap[answer.wordId]?.word) })
        dao.insertStudyLog(
            StudyLogEntity(
                studiedAt = finishedAt,
                lessonId = lessonId,
                trainingId = trainingId,
                studySeconds = score.studySeconds,
                correctCount = score.correct,
                wrongCount = score.wrong
            )
        )
        if (updateProgress) {
            updateTrainingProgress(lessonId, trainingId, score.accuracy, score.starCount, finishedAt)
        }
        QuizResult(
            attemptId = attemptId,
            trainingId = trainingId,
            isReview = false,
            totalQuestions = score.total,
            correctCount = score.correct,
            wrongCount = score.wrong,
            accuracy = score.accuracy,
            studySeconds = score.studySeconds,
            starCount = score.starCount,
            wrongWords = answers.filter { !it.isCorrect }.mapNotNull { questionMap[it.wordId]?.word }
        )
    }

    private fun buildReviewFallbackChoices(word: WordEntity, reviewWords: List<WordEntity>): List<WordChoice> {
        val correctMeaning = word.meaning.trim()
        if (correctMeaning.isEmpty()) return emptyList()
        val correct = WordChoice(id = -1, wordId = word.id, choiceText = correctMeaning, isCorrect = true, displayOrder = 0)
        val wrongs = reviewWords
            .filter { it.id != word.id }
            .map { it.meaning.trim() }
            .filter { it.isNotEmpty() && it != correctMeaning }
            .distinct()
            .let(runtime::shuffled)
            .take(3)
            .mapIndexed { index, meaning ->
                WordChoice(id = -index - 2, wordId = word.id, choiceText = meaning, isCorrect = false, displayOrder = index + 1)
            }
        return listOf(correct) + wrongs
    }

    private suspend fun addReviewWord(wordId: Int, reason: String) {
        val now = runtime.nowMillis()
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
                    wrongCount = current.wrongCount + if (reason == "checked") 0 else 1
                )
            )
        }
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
                isMastered = starCount >= 3 || current?.isMastered == true,
                lastAccuracy = accuracy
            )
        )
    }

    private suspend fun updateLessonMaster(lessonId: Int, studiedAt: Long) {
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
                isMastered = mastered,
                lastAccuracy = current?.lastAccuracy ?: 0f
            )
        )
    }

    private fun buildSentenceQuestion(entity: CustomSentenceEntity): SentenceQuestion? {
        val sentence = entity.sentence.trim()
        val bracketPattern = Regex("\\[([^\\]]+)\\]")
        val matches = bracketPattern.findAll(sentence).toList()
        if (matches.size >= 4) {
            val answers = matches.take(4).map { it.groupValues[1] }
            val markers = listOf("①", "②", "③", "④")
            var template = sentence
            matches.take(4).forEachIndexed { index, match ->
                template = template.replace(match.value, markers[index])
            }
            return SentenceQuestion(entity.id, template, answers, runtime.shuffled(answers), entity.meaning)
        }
        val rawWords = sentence.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (rawWords.size < 6) return null
        val maxStart = rawWords.size - 4
        val minStart = if (rawWords.size >= 7) 1 else 0
        val start = if (maxStart > minStart) runtime.randomInt(minStart, maxStart) else minStart.coerceAtMost(maxStart)
        val answerSlice = rawWords.subList(start, start + 4)
        val answers = answerSlice.map { it.trimEnd('.', ',', '!', '?', ';', ':') }
        val markers = listOf("①", "②", "③", "④")
        val templateWords = rawWords.mapIndexed { index, word ->
            if (index in start until start + 4) {
                val marker = markers[index - start]
                val trailing = word.drop(word.trimEnd('.', ',', '!', '?', ';', ':').length)
                if (trailing.isNotEmpty()) "$marker$trailing" else marker
            } else {
                word
            }
        }
        return SentenceQuestion(entity.id, templateWords.joinToString(" "), answers, runtime.shuffled(answers), entity.meaning)
    }
}
