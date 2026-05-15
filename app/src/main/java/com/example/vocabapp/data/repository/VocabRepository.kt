package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.data.local.entity.QuizAttemptAnswerEntity
import com.example.vocabapp.data.local.entity.QuizAttemptEntity
import com.example.vocabapp.data.local.entity.ReviewWordEntity
import com.example.vocabapp.data.local.entity.StudyLogEntity
import com.example.vocabapp.data.local.entity.UserProgressEntity
import com.example.vocabapp.data.local.entity.WordChoiceEntity
import com.example.vocabapp.data.local.entity.WordEntity
import com.example.vocabapp.data.seed.IdiomSeedData
import com.example.vocabapp.data.seed.SeedData
import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.SentenceQuestion
import com.example.vocabapp.domain.model.SentenceQuizResult
import com.example.vocabapp.domain.model.HomeSummary
import com.example.vocabapp.domain.model.ImportErrorRow
import com.example.vocabapp.domain.model.ImportedWord
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.domain.model.LessonStatus
import com.example.vocabapp.domain.model.QuizQuestion
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.domain.model.WordChoice
import com.example.vocabapp.domain.model.WordImportPreview
import com.example.vocabapp.domain.model.WordImportResult
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

    suspend fun seedIdiomsIfNeeded() {
        dao.seedIdiomsIfNeeded(
            IdiomSeedData.lessons,
            IdiomSeedData.trainings,
            IdiomSeedData.words,
            IdiomSeedData.choices,
            IdiomSeedData.relations
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
        val base = combine(
            dao.observeTotalStudySeconds(),
            dao.observeStudySecondsFrom(weekStart),
            dao.observeLessons(),
            dao.observeProgress(),
            dao.observeActiveReviews()
        ) { totalSeconds, weekSeconds, lessons, progress, reviews ->
            val vocabLessons = lessons.filter { it.id < 100 }
            val idiomLessons = lessons.filter { it.id >= 100 }
            HomeSummary(
                totalStudySeconds = totalSeconds,
                weekStudySeconds = weekSeconds,
                masteredLessons = progress.count { it.trainingId == null && it.isMastered && it.lessonId in 1..99 },
                totalLessons = vocabLessons.size,
                reviewCount = reviews.count { it.isActive },
                idiomMasteredLessons = progress.count { it.trainingId == null && it.isMastered && it.lessonId >= 100 },
                idiomTotalLessons = idiomLessons.size
            )
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

    private fun trainingCountForLesson(lessonId: Int) = if (lessonId >= 100) 3 else 10

    fun observeCustomTrainings(type: String): Flow<List<Training>> {
        val lessonId = customLessonId(type)
        return combine(observeCustomStudyWords(type), dao.observeProgress()) { words, progress ->
            words.chunked(10).mapIndexed { index, chunk ->
                val setNumber = index + 1
                val trainingId = customTrainingId(type, setNumber)
                val item = progress.firstOrNull { it.trainingId == trainingId }
                Training(
                    id = trainingId,
                    lessonId = lessonId,
                    title = customTitle(type),
                    wordStartNumber = index * 10 + 1,
                    wordEndNumber = index * 10 + chunk.size,
                    studyCount = item?.studyCount ?: 0,
                    bestAccuracy = item?.bestAccuracy ?: 0f,
                    bestStarCount = item?.bestStarCount ?: 0,
                    lastStudiedAt = item?.lastStudiedAt,
                    lastAccuracy = item?.let { if (it.lastAccuracy > 0f) it.lastAccuracy else it.bestAccuracy } ?: 0f,
                    firstWordId = chunk.first().id
                )
            }
        }
    }

    private fun observeCustomStudyWords(type: String): Flow<List<CustomStudyWord>> =
        if (type == CUSTOM_TYPE_IDIOM) {
            dao.observeCustomIdiomsInStudyOrder().map { items ->
                items.map { CustomStudyWord(it.id, it.english, it.meaning, "", "") }
            }
        } else {
            dao.observeCustomWordsInStudyOrder().map { items ->
                items.filter { it.wordType != "phrase" }
                    .map { CustomStudyWord(it.id, it.english, it.meaning, it.exampleSentence, it.exampleTranslation) }
            }
        }

    private fun observeLessonsFiltered(predicate: (com.example.vocabapp.data.local.entity.LessonEntity) -> Boolean): Flow<List<Lesson>> =
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

    suspend fun getTrainingRange(trainingId: Int): String? =
        dao.getTraining(trainingId)?.let { "${it.wordStartNumber}〜${it.wordEndNumber}語" }

    suspend fun getWordsForTraining(trainingId: Int): List<Word> =
        dao.getWordsByTraining(trainingId).map { it.toDomain() }

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
        dao.getAttempt(attemptId)?.let { attempt ->
            val wrongWords = dao.getWrongWordsForAttempt(attemptId).map { it.toDomain() }
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
                wrongWords = wrongWords
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

    suspend fun addCustomWord(english: String, meaning: String) {
        dao.insertCustomWord(CustomWordEntity(
            english = english.trim(),
            meaning = meaning.trim(),
            addedAt = System.currentTimeMillis()
        ))
    }

    suspend fun previewCustomWordCsv(csvText: String): WordImportPreview {
        val rows = parseCsvRows(csvText)
        if (rows.isEmpty()) {
            return WordImportPreview(
                errors = listOf(ImportErrorRow(1, "CSVが空です", emptyList()))
            )
        }

        val header = rows.first().map { it.trim() }
        val headerLower = header.map { it.lowercase() }

        val englishAliases = setOf(
            "english", "english phrase", "english word", "英語", "英文", "word", "単語", "英単語"
        )
        val meaningAliases = setOf(
            "meaning", "japanese meaning", "日本語の意味", "意味", "japanese", "訳", "和訳", "日本語"
        )
        val exampleAliases = setOf("example", "example sentence", "例文", "例文（英語）", "english example")
        val exampleTranslationAliases = setOf("example_translation", "例文（日本語）", "例文訳", "japanese example", "和訳例文")
        val typeAliases = setOf("type", "種類", "タイプ")

        fun findIndex(aliases: Set<String>): Int =
            headerLower.indexOfFirst { it in aliases }

        val englishIndex = findIndex(englishAliases)
        val meaningIndex = findIndex(meaningAliases)

        if (englishIndex == -1 || meaningIndex == -1) {
            val missing = buildList {
                if (englishIndex == -1) add("英語(例: english, English Phrase, 英語)")
                if (meaningIndex == -1) add("意味(例: meaning, 日本語の意味, 意味)")
            }.joinToString(", ")
            val found = rows.first().joinToString(", ")
            return WordImportPreview(
                totalRows = rows.drop(1).size,
                errors = listOf(ImportErrorRow(
                    1,
                    "必須ヘッダーが見つかりません: $missing\n検出されたヘッダー: $found",
                    rows.first()
                ))
            )
        }

        val exampleIndex = findIndex(exampleAliases)
        val exampleTranslationIndex = findIndex(exampleTranslationAliases)
        val typeIndex = findIndex(typeAliases)
        val existing = (dao.getNormalizedSeedEnglish() + dao.getNormalizedCustomEnglish()
            + dao.getNormalizedCustomIdiomEnglish()).toMutableSet()
        val seenInCsv = mutableSetOf<String>()
        val newWords = mutableListOf<ImportedWord>()
        val duplicateWords = mutableListOf<ImportedWord>()
        val errors = mutableListOf<ImportErrorRow>()
        val dataRows = rows.drop(1)

        dataRows.forEachIndexed { index, row ->
            val rowNumber = index + 2
            val english = row.getOrEmpty(englishIndex).trim()
            val meaning = row.getOrEmpty(meaningIndex).trim()
            val example = row.getOrEmpty(exampleIndex).trim()
            val exampleTranslation = row.getOrEmpty(exampleTranslationIndex).trim()
            val rawType = row.getOrEmpty(typeIndex).trim().lowercase()

            if (english.isBlank() || meaning.isBlank()) {
                errors += ImportErrorRow(rowNumber, "english と meaning は必須です", row)
                return@forEachIndexed
            }

            val wordType = when {
                rawType.isBlank() -> if (english.contains(Regex("\\s"))) "phrase" else "word"
                rawType == "word" || rawType == "phrase" || rawType == "sentence" -> rawType
                else -> {
                    errors += ImportErrorRow(rowNumber, "type は word / phrase / sentence のみ指定できます", row)
                    return@forEachIndexed
                }
            }

            val imported = ImportedWord(
                english = english,
                meaning = meaning,
                exampleSentence = example,
                exampleTranslation = exampleTranslation,
                type = wordType
            )
            val normalized = english.normalizeEnglish()
            if (normalized in existing || normalized in seenInCsv) {
                duplicateWords += imported
            } else {
                seenInCsv += normalized
                newWords += imported
            }
        }

        return WordImportPreview(
            totalRows = dataRows.size,
            newWords = newWords,
            duplicateWords = duplicateWords,
            errors = errors
        )
    }

    suspend fun importCustomWords(preview: WordImportPreview): WordImportResult {
        val now = System.currentTimeMillis()
        val existing = (dao.getNormalizedSeedEnglish() + dao.getNormalizedCustomEnglish()
            + dao.getNormalizedCustomIdiomEnglish()).toSet()
        val seen = mutableSetOf<String>()
        val eligible = preview.newWords.filter { word ->
            val normalized = word.english.normalizeEnglish()
            normalized !in existing && seen.add(normalized)
        }
        val wordItems = eligible.filter { it.type == "word" }.map { word ->
            CustomWordEntity(
                english = word.english,
                meaning = word.meaning,
                addedAt = now,
                exampleSentence = word.exampleSentence,
                exampleTranslation = word.exampleTranslation,
                wordType = word.type
            )
        }
        val idiomItems = eligible.filter { it.type == "phrase" }.map { word ->
            CustomIdiomEntity(
                english = word.english,
                meaning = word.meaning,
                addedAt = now
            )
        }
        val sentenceItems = eligible.filter { it.type == "sentence" }.map { word ->
            CustomSentenceEntity(
                sentence = word.english,
                meaning = word.meaning,
                addedAt = now
            )
        }
        if (wordItems.isNotEmpty()) dao.insertCustomWords(wordItems)
        if (idiomItems.isNotEmpty()) dao.insertCustomIdioms(idiomItems)
        if (sentenceItems.isNotEmpty()) dao.insertCustomSentences(sentenceItems)
        val lateDuplicates = preview.newWords.size - eligible.size
        return WordImportResult(
            totalRows = preview.totalRows,
            insertedCount = wordItems.size,
            insertedIdiomCount = idiomItems.size + sentenceItems.size,
            duplicateCount = preview.duplicateCount + lateDuplicates,
            errorCount = preview.errorCount
        )
    }

    suspend fun deleteCustomWord(id: Int) { dao.deleteCustomWord(id) }

    suspend fun deleteAllCustomWords() { dao.deleteAllCustomWords() }

    fun observeCustomWords(): Flow<List<CustomWordEntity>> = dao.observeCustomWords()

    suspend fun addCustomIdiom(english: String, meaning: String) {
        dao.insertCustomIdiom(CustomIdiomEntity(
            english = english.trim(),
            meaning = meaning.trim(),
            addedAt = System.currentTimeMillis()
        ))
    }

    fun observeCustomIdioms(): Flow<List<CustomIdiomEntity>> = dao.observeCustomIdioms()

    suspend fun deleteCustomIdiom(id: Int) { dao.deleteCustomIdiom(id) }

    suspend fun deleteAllCustomIdioms() { dao.deleteAllCustomIdioms() }

    suspend fun buildCustomIdiomQuiz(): List<QuizQuestion> {
        val all = dao.getAllCustomIdioms()
        if (all.size < 4) return emptyList()
        return all.shuffled().take(minOf(10, all.size)).map { ci ->
            val wrongPool = all.filter { it.id != ci.id }.shuffled().take(3)
            val correct = WordChoice(id = ci.id * -200, wordId = ci.id, choiceText = ci.meaning, isCorrect = true, displayOrder = 0)
            val wrongs = wrongPool.mapIndexed { i, ww ->
                WordChoice(id = ww.id * -200 - i - 1, wordId = ci.id, choiceText = ww.meaning, isCorrect = false, displayOrder = i + 1)
            }
            QuizQuestion(
                word = Word(id = ci.id, trainingId = -1, english = ci.english, meaning = ci.meaning,
                    phonetic = "", partOfSpeech = "英熟語",
                    exampleSentence = "", exampleTranslation = "",
                    audioUrl = null, exampleAudioUrl = null, displayOrder = 0),
                choices = (listOf(correct) + wrongs).shuffled().mapIndexed { i, c -> c.copy(displayOrder = i) }
            )
        }
    }

    suspend fun buildCustomTrainingQuiz(type: String, setNumber: Int): List<QuizQuestion> {
        val all = getCustomStudyWords(type)
        if (all.size < 4) return emptyList()
        val startIndex = (setNumber - 1).coerceAtLeast(0) * 10
        val targets = all.drop(startIndex).take(10)
        if (targets.isEmpty()) return emptyList()
        val idMultiplier = if (type == CUSTOM_TYPE_IDIOM) -200 else -100
        val partOfSpeech = if (type == CUSTOM_TYPE_IDIOM) "英熟語" else "単語"
        return targets.mapIndexed { questionIndex, target ->
            val wrongPool = all.filter { it.id != target.id }.shuffled().take(3)
            val domainWordId = customWordDomainId(type, target.id)
            val correct = WordChoice(
                id = target.id * idMultiplier,
                wordId = domainWordId,
                choiceText = target.meaning,
                isCorrect = true,
                displayOrder = 0
            )
            val wrongs = wrongPool.mapIndexed { i, wrong ->
                WordChoice(
                    id = wrong.id * idMultiplier - i - 1,
                    wordId = domainWordId,
                    choiceText = wrong.meaning,
                    isCorrect = false,
                    displayOrder = i + 1
                )
            }
            QuizQuestion(
                word = Word(
                    id = domainWordId,
                    trainingId = customTrainingId(type, setNumber),
                    english = target.english,
                    meaning = target.meaning,
                    phonetic = "",
                    partOfSpeech = partOfSpeech,
                    exampleSentence = target.exampleSentence,
                    exampleTranslation = target.exampleTranslation,
                    audioUrl = null,
                    exampleAudioUrl = null,
                    displayOrder = startIndex + questionIndex + 1
                ),
                choices = (listOf(correct) + wrongs).shuffled().mapIndexed { i, c -> c.copy(displayOrder = i) }
            )
        }
    }

    suspend fun buildRandomCustomQuiz(type: String): List<QuizQuestion> {
        val all = getCustomStudyWords(type)
        if (all.size < 4) return emptyList()
        val targets = all.shuffled().take(minOf(10, all.size))
        val idMultiplier = if (type == CUSTOM_TYPE_IDIOM) -200 else -100
        val partOfSpeech = if (type == CUSTOM_TYPE_IDIOM) "英熟語" else "単語"
        return targets.mapIndexed { questionIndex, target ->
            val wrongPool = all.filter { it.id != target.id }.shuffled().take(3)
            val domainWordId = customWordDomainId(type, target.id)
            val correct = WordChoice(
                id = target.id * idMultiplier,
                wordId = domainWordId,
                choiceText = target.meaning,
                isCorrect = true,
                displayOrder = 0
            )
            val wrongs = wrongPool.mapIndexed { i, wrong ->
                WordChoice(
                    id = wrong.id * idMultiplier - i - 1,
                    wordId = domainWordId,
                    choiceText = wrong.meaning,
                    isCorrect = false,
                    displayOrder = i + 1
                )
            }
            QuizQuestion(
                word = Word(
                    id = domainWordId,
                    trainingId = randomCustomTrainingId(type),
                    english = target.english,
                    meaning = target.meaning,
                    phonetic = "",
                    partOfSpeech = partOfSpeech,
                    exampleSentence = target.exampleSentence,
                    exampleTranslation = target.exampleTranslation,
                    audioUrl = null,
                    exampleAudioUrl = null,
                    displayOrder = questionIndex + 1
                ),
                choices = (listOf(correct) + wrongs).shuffled().mapIndexed { i, c -> c.copy(displayOrder = i) }
            )
        }
    }

    suspend fun buildCustomWordQuiz(): List<QuizQuestion> {
        val all = dao.getAllCustomWords().filter { it.wordType != "phrase" }
        if (all.size < 4) return emptyList()
        return all.shuffled().take(minOf(10, all.size)).map { cw ->
            val wrongPool = all.filter { it.id != cw.id }.shuffled().take(3)
            val correct = WordChoice(id = cw.id * -100, wordId = cw.id, choiceText = cw.meaning, isCorrect = true, displayOrder = 0)
            val wrongs = wrongPool.mapIndexed { i, ww ->
                WordChoice(id = ww.id * -100 - i - 1, wordId = cw.id, choiceText = ww.meaning, isCorrect = false, displayOrder = i + 1)
            }
            QuizQuestion(
                word = Word(id = cw.id, trainingId = -1, english = cw.english, meaning = cw.meaning,
                    phonetic = "", partOfSpeech = "単語",
                    exampleSentence = cw.exampleSentence, exampleTranslation = cw.exampleTranslation,
                    audioUrl = null, exampleAudioUrl = null, displayOrder = 0),
                choices = (listOf(correct) + wrongs).shuffled().mapIndexed { i, c -> c.copy(displayOrder = i) }
            )
        }
    }

    suspend fun finishCustomQuiz(
        type: String,
        setNumber: Int,
        startedAt: Long,
        answers: List<AnswerRecord>,
        questions: List<QuizQuestion>
    ): QuizResult {
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
        val lessonId = customLessonId(type)
        val trainingId = customTrainingId(type, setNumber)
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
        updateTrainingProgress(lessonId, trainingId, accuracy, starCount, finishedAt)
        val questionMap = questions.associateBy { it.word.id }
        return QuizResult(
            attemptId = -System.nanoTime(),
            trainingId = trainingId,
            isReview = false,
            totalQuestions = total,
            correctCount = correct,
            wrongCount = wrong,
            accuracy = accuracy,
            studySeconds = studySeconds,
            starCount = starCount,
            wrongWords = answers.filter { !it.isCorrect }.mapNotNull { questionMap[it.wordId]?.word }
        )
    }

    suspend fun finishRandomCustomQuiz(
        type: String,
        startedAt: Long,
        answers: List<AnswerRecord>,
        questions: List<QuizQuestion>
    ): QuizResult {
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
        val lessonId = customLessonId(type)
        val trainingId = randomCustomTrainingId(type)
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
        val questionMap = questions.associateBy { it.word.id }
        return QuizResult(
            attemptId = -System.nanoTime(),
            trainingId = trainingId,
            isReview = false,
            totalQuestions = total,
            correctCount = correct,
            wrongCount = wrong,
            accuracy = accuracy,
            studySeconds = studySeconds,
            starCount = starCount,
            wrongWords = answers.filter { !it.isCorrect }.mapNotNull { questionMap[it.wordId]?.word }
        )
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
                isMastered = starCount >= 3 || current?.isMastered == true,
                lastAccuracy = accuracy
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
                isMastered = mastered,
                lastAccuracy = current?.lastAccuracy ?: 0f
            )
        )
    }

    private fun calculateStreak(studyDays: List<Long>): Int {
        if (studyDays.isEmpty()) return 0
        val todayDay = System.currentTimeMillis() / 86400000L
        var streak = 0
        var expected = todayDay
        for (day in studyDays) {
            if (day == expected || (streak == 0 && day == expected - 1)) {
                streak++
                expected = day - 1
            } else break
        }
        return streak
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

    private fun List<String>.getOrEmpty(index: Int): String =
        if (index >= 0 && index < size) this[index] else ""

    private fun String.normalizeEnglish(): String =
        trim().lowercase()

    private suspend fun getCustomStudyWords(type: String): List<CustomStudyWord> =
        if (type == CUSTOM_TYPE_IDIOM) {
            dao.getCustomIdiomsInStudyOrder()
                .map { CustomStudyWord(it.id, it.english, it.meaning, "", "") }
        } else {
            dao.getCustomWordsInStudyOrder()
                .filter { it.wordType != "phrase" }
                .map { CustomStudyWord(it.id, it.english, it.meaning, it.exampleSentence, it.exampleTranslation) }
        }

    private fun customLessonId(type: String): Int =
        if (type == CUSTOM_TYPE_IDIOM) CUSTOM_IDIOM_LESSON_ID else CUSTOM_WORD_LESSON_ID

    private fun customTrainingId(type: String, setNumber: Int): Int =
        customLessonId(type) - setNumber

    private fun randomCustomTrainingId(type: String): Int =
        customLessonId(type) - 999

    private fun customWordDomainId(type: String, sourceId: Int): Int =
        (if (type == CUSTOM_TYPE_IDIOM) -200_000 else -100_000) - sourceId

    private fun customTitle(type: String): String =
        if (type == CUSTOM_TYPE_IDIOM) "カスタム英熟語" else "カスタム英単語"

    private fun parseCsvRows(input: String): List<List<String>> {
        if (input.isEmpty()) return emptyList()
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentCell = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < input.length) {
            val char = input[index]
            when {
                char == '"' && inQuotes && index + 1 < input.length && input[index + 1] == '"' -> {
                    currentCell.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    currentRow += currentCell.toString()
                    currentCell.clear()
                }
                (char == '\n' || char == '\r') && !inQuotes -> {
                    currentRow += currentCell.toString()
                    currentCell.clear()
                    if (currentRow.any { it.isNotBlank() }) rows += currentRow.toList()
                    currentRow.clear()
                    if (char == '\r' && index + 1 < input.length && input[index + 1] == '\n') {
                        index++
                    }
                }
                else -> currentCell.append(char)
            }
            index++
        }

        currentRow += currentCell.toString()
        if (currentRow.any { it.isNotBlank() }) rows += currentRow.toList()
        return rows
    }

    private data class CustomStudyWord(
        val id: Int,
        val english: String,
        val meaning: String,
        val exampleSentence: String,
        val exampleTranslation: String
    )

    fun observeCustomSentences(): Flow<List<CustomSentenceEntity>> = dao.observeCustomSentences()

    suspend fun deleteAllCustomSentences() { dao.deleteAllCustomSentences() }

    suspend fun addCustomSentence(sentence: String, meaning: String) {
        dao.insertCustomSentence(
            CustomSentenceEntity(
                sentence = sentence.trim(),
                meaning = meaning.trim(),
                addedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteCustomSentence(id: Int) { dao.deleteCustomSentence(id) }

    suspend fun buildSentenceQuiz(setNumber: Int? = null): List<SentenceQuestion> {
        val all = dao.getAllCustomSentences()
        if (all.isEmpty()) return emptyList()
        val targets = if (setNumber != null) {
            all.drop((setNumber - 1).coerceAtLeast(0) * 10).take(10)
        } else {
            all.shuffled().take(minOf(10, all.size))
        }
        return targets.mapNotNull { buildSentenceQuestion(it) }
    }

    private fun buildSentenceQuestion(entity: CustomSentenceEntity): SentenceQuestion? {
        val sentence = entity.sentence.trim()
        val bracketPattern = Regex("\\[([^\\]]+)\\]")
        val matches = bracketPattern.findAll(sentence).toList()
        if (matches.size >= 4) {
            val answers = matches.take(4).map { it.groupValues[1] }
            val markers = listOf("①", "②", "③", "④")
            var template = sentence
            matches.take(4).forEachIndexed { i, m ->
                template = template.replace(m.value, markers[i])
            }
            return SentenceQuestion(
                id = entity.id,
                template = template,
                answers = answers,
                shuffledChoices = answers.shuffled(),
                meaning = entity.meaning
            )
        }
        val rawWords = sentence.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (rawWords.size < 6) return null
        val maxStart = rawWords.size - 4
        val start = if (maxStart > 1) (1..maxStart).random() else 0
        val answerSlice = rawWords.subList(start, start + 4)
        val answers = answerSlice.map { it.trimEnd('.', ',', '!', '?', ';', ':') }
        val markers = listOf("①", "②", "③", "④")
        val templateWords = rawWords.mapIndexed { i, w ->
            if (i in start until start + 4) {
                val marker = markers[i - start]
                val trailing = w.drop(w.trimEnd('.', ',', '!', '?', ';', ':').length)
                if (trailing.isNotEmpty()) "$marker$trailing" else marker
            } else w
        }
        return SentenceQuestion(
            id = entity.id,
            template = templateWords.joinToString(" "),
            answers = answers,
            shuffledChoices = answers.shuffled(),
            meaning = entity.meaning
        )
    }

    suspend fun finishSentenceQuiz(
        startedAt: Long,
        correctCount: Int,
        totalQuestions: Int
    ): SentenceQuizResult {
        val finishedAt = System.currentTimeMillis()
        val total = totalQuestions.coerceAtLeast(1)
        val accuracy = correctCount * 100f / total
        val studySeconds = ((finishedAt - startedAt) / 1000).toInt().coerceAtLeast(1)
        val starCount = when {
            accuracy >= 90f -> 3
            accuracy >= 70f -> 2
            accuracy >= 50f -> 1
            else -> 0
        }
        dao.insertStudyLog(
            StudyLogEntity(
                studiedAt = finishedAt,
                lessonId = CUSTOM_SENTENCE_LESSON_ID,
                trainingId = null,
                studySeconds = studySeconds,
                correctCount = correctCount,
                wrongCount = totalQuestions - correctCount
            )
        )
        return SentenceQuizResult(
            totalQuestions = total,
            correctCount = correctCount,
            wrongCount = total - correctCount,
            accuracy = accuracy,
            studySeconds = studySeconds,
            starCount = starCount
        )
    }

    companion object {
        const val CUSTOM_TYPE_WORD = "word"
        const val CUSTOM_TYPE_IDIOM = "idiom"
        private const val CUSTOM_WORD_LESSON_ID = -10_000
        private const val CUSTOM_IDIOM_LESSON_ID = -20_000
        const val CUSTOM_SENTENCE_LESSON_ID = -30_000
    }
}
