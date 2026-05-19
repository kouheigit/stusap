package com.example.vocabapp.data.repository

import androidx.room.withTransaction
import com.example.vocabapp.MAX_IMPORT_CELL_CHARS
import com.example.vocabapp.MAX_IMPORT_COLUMNS
import com.example.vocabapp.MAX_IMPORT_ROWS
import com.example.vocabapp.parseCsvRows
import com.example.vocabapp.data.local.AppDatabase
import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.ImportErrorRow
import com.example.vocabapp.domain.model.QuizConstants
import com.example.vocabapp.domain.model.ImportedWord
import com.example.vocabapp.domain.model.QuizQuestion
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.domain.model.WordChoice
import com.example.vocabapp.domain.model.WordImportPreview
import com.example.vocabapp.domain.model.WordImportResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private const val MAX_PREVIEW_DUPLICATES = 50
private const val MAX_PREVIEW_ERRORS = 50
private const val IMPORT_INSERT_CHUNK_SIZE = 200
private val CSV_IDIOM_TYPES = setOf("phrase", "idiom", "custom_idiom", "custom_idioms")

@Singleton
class CustomContentRepository @Inject constructor(
    private val database: AppDatabase,
    private val runtime: QuizRuntime
) {
    private val dao: AppDao = database.appDao()

    fun observeCustomWords(): Flow<List<CustomWordEntity>> = dao.observeCustomWords()

    fun observeCustomIdioms(): Flow<List<CustomIdiomEntity>> = dao.observeCustomIdioms()

    fun observeCustomSentences(): Flow<List<CustomSentenceEntity>> = dao.observeCustomSentences()

    fun observeCustomTrainings(type: String): Flow<List<Training>> {
        val lessonId = customLessonId(type)
        return combine(observeCustomStudyWords(type), dao.observeProgress()) { words, progress ->
            words.chunked(QuizConstants.QUESTION_COUNT).mapIndexed { index, chunk ->
                val setNumber = index + 1
                val trainingId = customTrainingId(type, setNumber)
                val item = progress.firstOrNull { it.trainingId == trainingId }
                Training(
                    id = trainingId,
                    lessonId = lessonId,
                    title = customTitle(type),
                    wordStartNumber = index * QuizConstants.QUESTION_COUNT + 1,
                    wordEndNumber = index * QuizConstants.QUESTION_COUNT + chunk.size,
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

    suspend fun previewCustomWordCsv(csvText: String): WordImportPreview {
        val rows = parseCsvRows(csvText)
        if (rows.isEmpty()) {
            return WordImportPreview(errors = listOf(ImportErrorRow(1, "CSVが空です", emptyList())))
        }

        val header = rows.first().map { it.trim() }
        val headerLower = header.map { it.lowercase() }
        fun findIndex(aliases: Set<String>): Int = headerLower.indexOfFirst { it in aliases }

        val englishIndex = findIndex(setOf("english", "english phrase", "english word", "英語", "英文", "word", "単語", "英単語"))
        val meaningIndex = findIndex(setOf("meaning", "japanese meaning", "日本語の意味", "意味", "japanese", "訳", "和訳", "日本語"))
        if (englishIndex == -1 || meaningIndex == -1) {
            val missing = buildList {
                if (englishIndex == -1) add("英語(例: english, English Phrase, 英語)")
                if (meaningIndex == -1) add("意味(例: meaning, 日本語の意味, 意味)")
            }.joinToString(", ")
            return WordImportPreview(
                totalRows = rows.drop(1).size,
                errors = listOf(
                    ImportErrorRow(
                        1,
                        "必須ヘッダーが見つかりません: $missing\n検出されたヘッダー: ${rows.first().joinToString(", ")}",
                        rows.first()
                    )
                )
            )
        }

        val exampleIndex = findIndex(setOf("example", "example sentence", "例文", "例文（英語）", "english example"))
        val exampleTranslationIndex = findIndex(setOf("example_translation", "例文（日本語）", "例文訳", "japanese example", "和訳例文"))
        val typeIndex = findIndex(setOf("type", "種類", "タイプ"))
        val existing = (dao.getNormalizedSeedEnglish() + dao.getNormalizedCustomEnglish() +
            dao.getNormalizedCustomIdiomEnglish()).toMutableSet()
        val seenInCsv = mutableSetOf<String>()
        val newWords = mutableListOf<ImportedWord>()
        val duplicateWords = mutableListOf<ImportedWord>()
        val errors = mutableListOf<ImportErrorRow>()
        var omittedDuplicateCount = 0
        var omittedErrorCount = 0

        fun addError(rowNumber: Int, reason: String, row: List<String>) {
            if (errors.size < MAX_PREVIEW_ERRORS) {
                errors += ImportErrorRow(rowNumber, reason, row.take(MAX_IMPORT_COLUMNS))
            } else {
                omittedErrorCount++
            }
        }

        fun addDuplicate(word: ImportedWord) {
            if (duplicateWords.size < MAX_PREVIEW_DUPLICATES) {
                duplicateWords += word
            } else {
                omittedDuplicateCount++
            }
        }

        val dataRows = rows.drop(1)
        val currentCustomCount = dao.customWordCount() + dao.customIdiomCount() + dao.customSentenceCount()
        val availableSlots = (MAX_CUSTOM_CONTENT_ITEMS - currentCustomCount).coerceAtLeast(0)

        dataRows.forEachIndexed { index, row ->
            val rowNumber = index + 2
            val english = row.getOrEmpty(englishIndex).trim()
            val meaning = row.getOrEmpty(meaningIndex).trim()
            val example = row.getOrEmpty(exampleIndex).trim()
            val exampleTranslation = row.getOrEmpty(exampleTranslationIndex).trim()
            val rawType = row.getOrEmpty(typeIndex).trim().lowercase()

            if (english.isBlank() || meaning.isBlank()) {
                addError(rowNumber, "english と meaning は必須です", row)
                return@forEachIndexed
            }
            if (meaning.length > MAX_CUSTOM_MEANING_CHARS) {
                addError(rowNumber, "meaning は${MAX_CUSTOM_MEANING_CHARS}文字以内にしてください", row)
                return@forEachIndexed
            }
            if (example.length > MAX_CUSTOM_EXAMPLE_CHARS || exampleTranslation.length > MAX_CUSTOM_EXAMPLE_CHARS) {
                addError(rowNumber, "例文と例文訳は各${MAX_CUSTOM_EXAMPLE_CHARS}文字以内にしてください", row)
                return@forEachIndexed
            }

            val wordType = when {
                rawType.isBlank() -> if (english.contains(Regex("\\s"))) "phrase" else "word"
                rawType == "word" || rawType == "sentence" -> rawType
                rawType in CSV_IDIOM_TYPES -> "phrase"
                else -> {
                    addError(rowNumber, "type は word / phrase / idiom / custom_idioms / sentence のみ指定できます", row)
                    return@forEachIndexed
                }
            }
            if (wordType == "sentence" && english.length > MAX_CUSTOM_SENTENCE_CHARS) {
                addError(rowNumber, "sentence は${MAX_CUSTOM_SENTENCE_CHARS}文字以内にしてください", row)
                return@forEachIndexed
            }
            if (wordType != "sentence" && english.length > MAX_CUSTOM_ENGLISH_CHARS) {
                addError(rowNumber, "english は${MAX_CUSTOM_ENGLISH_CHARS}文字以内にしてください", row)
                return@forEachIndexed
            }

            val imported = ImportedWord(english, meaning, example, exampleTranslation, wordType)
            val normalized = english.normalizeEnglish()
            when {
                normalized in existing || normalized in seenInCsv -> addDuplicate(imported)
                newWords.size >= availableSlots -> addError(rowNumber, "登録上限（${MAX_CUSTOM_CONTENT_ITEMS}件）を超えています", row)
                else -> {
                    seenInCsv += normalized
                    newWords += imported
                }
            }
        }

        return WordImportPreview(
            totalRows = dataRows.size,
            newWords = newWords,
            duplicateWords = duplicateWords,
            errors = errors,
            omittedDuplicateCount = omittedDuplicateCount,
            omittedErrorCount = omittedErrorCount
        )
    }

    suspend fun importCustomWords(preview: WordImportPreview): WordImportResult = database.withTransaction {
        val now = runtime.nowMillis()
        ensureCustomContentCapacity(preview.newWords.size)
        val existing = (dao.getNormalizedSeedEnglish() + dao.getNormalizedCustomEnglish() +
            dao.getNormalizedCustomIdiomEnglish()).toSet()
        val seen = mutableSetOf<String>()
        val eligible = preview.newWords.filter { word ->
            val normalized = word.english.normalizeEnglish()
            normalized !in existing && seen.add(normalized)
        }
        val wordItems = eligible.filter { it.type == "word" }.map { word ->
            CustomWordEntity(
                english = word.english.trim().take(MAX_CUSTOM_ENGLISH_CHARS),
                meaning = word.meaning.trim().take(MAX_CUSTOM_MEANING_CHARS),
                addedAt = now,
                exampleSentence = word.exampleSentence.trim().take(MAX_CUSTOM_EXAMPLE_CHARS),
                exampleTranslation = word.exampleTranslation.trim().take(MAX_CUSTOM_EXAMPLE_CHARS),
                wordType = word.type
            )
        }
        val idiomItems = eligible.filter { it.type == "phrase" }.map { word ->
            CustomIdiomEntity(
                english = word.english.trim().take(MAX_CUSTOM_ENGLISH_CHARS),
                meaning = word.meaning.trim().take(MAX_CUSTOM_MEANING_CHARS),
                addedAt = now
            )
        }
        val sentenceItems = eligible.filter { it.type == "sentence" }.map { word ->
            CustomSentenceEntity(
                sentence = word.english.trim().take(MAX_CUSTOM_SENTENCE_CHARS),
                meaning = word.meaning.trim().take(MAX_CUSTOM_MEANING_CHARS),
                addedAt = now
            )
        }
        wordItems.chunked(IMPORT_INSERT_CHUNK_SIZE).forEach { dao.insertCustomWords(it) }
        idiomItems.chunked(IMPORT_INSERT_CHUNK_SIZE).forEach { dao.insertCustomIdioms(it) }
        sentenceItems.chunked(IMPORT_INSERT_CHUNK_SIZE).forEach { dao.insertCustomSentences(it) }
        val lateDuplicates = preview.newWords.size - eligible.size
        WordImportResult(
            totalRows = preview.totalRows,
            insertedCount = wordItems.size,
            insertedIdiomCount = idiomItems.size + sentenceItems.size,
            duplicateCount = preview.duplicateCount + lateDuplicates,
            errorCount = preview.errorCount
        )
    }

    suspend fun addCustomWord(english: String, meaning: String) {
        ensureCustomContentCapacity(1)
        val trimmedEnglish = english.trim().take(MAX_CUSTOM_ENGLISH_CHARS)
        val trimmedMeaning = meaning.trim().take(MAX_CUSTOM_MEANING_CHARS)
        if (trimmedEnglish.isBlank() || trimmedMeaning.isBlank()) return
        dao.insertCustomWord(CustomWordEntity(english = trimmedEnglish, meaning = trimmedMeaning, addedAt = runtime.nowMillis()))
    }

    suspend fun addCustomIdiom(english: String, meaning: String) {
        ensureCustomContentCapacity(1)
        val trimmedEnglish = english.trim().take(MAX_CUSTOM_ENGLISH_CHARS)
        val trimmedMeaning = meaning.trim().take(MAX_CUSTOM_MEANING_CHARS)
        if (trimmedEnglish.isBlank() || trimmedMeaning.isBlank()) return
        dao.insertCustomIdiom(CustomIdiomEntity(english = trimmedEnglish, meaning = trimmedMeaning, addedAt = runtime.nowMillis()))
    }

    suspend fun addCustomSentence(sentence: String, meaning: String) {
        ensureCustomContentCapacity(1)
        val trimmedSentence = sentence.trim().take(MAX_CUSTOM_SENTENCE_CHARS)
        val trimmedMeaning = meaning.trim().take(MAX_CUSTOM_MEANING_CHARS)
        if (trimmedSentence.isBlank() || trimmedMeaning.isBlank()) return
        dao.insertCustomSentence(CustomSentenceEntity(sentence = trimmedSentence, meaning = trimmedMeaning, addedAt = runtime.nowMillis()))
    }

    suspend fun deleteCustomWord(id: Int) = dao.deleteCustomWord(id)
    suspend fun deleteCustomIdiom(id: Int) = dao.deleteCustomIdiom(id)
    suspend fun deleteCustomSentence(id: Int) = dao.deleteCustomSentence(id)
    suspend fun deleteAllCustomWordsAndIdioms() = dao.deleteAllCustomWordsAndIdioms()
    suspend fun deleteAllCustomSentences() = dao.deleteAllCustomSentences()
    suspend fun setCustomWordFavorite(id: Int, isFavorite: Boolean) = dao.setCustomWordFavorite(id, isFavorite)
    suspend fun setCustomWordLearned(id: Int, isLearned: Boolean) = dao.setCustomWordLearned(id, isLearned)

    suspend fun buildCustomIdiomQuiz(): List<QuizQuestion> {
        val all = dao.getAllCustomIdioms()
        if (all.size < QuizConstants.MIN_WORD_COUNT_FOR_QUIZ) return emptyList()
        return runtime.shuffled(all).take(minOf(QuizConstants.QUESTION_COUNT, all.size)).map { idiom ->
            val wrongPool = runtime.shuffled(all.filter { it.id != idiom.id }).take(3)
            val correct = WordChoice(id = idiom.id * -200, wordId = idiom.id, choiceText = idiom.meaning, isCorrect = true, displayOrder = 0)
            val wrongs = wrongPool.mapIndexed { index, wrong ->
                WordChoice(id = wrong.id * -200 - index - 1, wordId = idiom.id, choiceText = wrong.meaning, isCorrect = false, displayOrder = index + 1)
            }
            QuizQuestion(
                word = Word(id = idiom.id, trainingId = -1, english = idiom.english, meaning = idiom.meaning, phonetic = "", partOfSpeech = "英熟語", exampleSentence = "", exampleTranslation = "", audioUrl = null, exampleAudioUrl = null, displayOrder = 0),
                choices = runtime.shuffled(listOf(correct) + wrongs).mapIndexed { index, choice -> choice.copy(displayOrder = index) }
            )
        }
    }

    suspend fun buildCustomTrainingQuiz(type: String, setNumber: Int): List<QuizQuestion> {
        val all = getCustomStudyWords(type)
        if (all.size < QuizConstants.MIN_WORD_COUNT_FOR_QUIZ) return emptyList()
        val startIndex = (setNumber - 1).coerceAtLeast(0) * QuizConstants.QUESTION_COUNT
        val targets = all.drop(startIndex).take(QuizConstants.QUESTION_COUNT)
        if (targets.isEmpty()) return emptyList()
        return buildCustomQuizQuestions(type, customTrainingId(type, setNumber), targets, all, startIndex)
    }

    suspend fun buildRandomCustomQuiz(type: String): List<QuizQuestion> {
        val all = getCustomStudyWords(type)
        if (all.size < QuizConstants.MIN_WORD_COUNT_FOR_QUIZ) return emptyList()
        val targets = runtime.shuffled(all).take(minOf(QuizConstants.QUESTION_COUNT, all.size))
        return buildCustomQuizQuestions(type, randomCustomTrainingId(type), targets, all, 0)
    }

    suspend fun buildCustomWordQuiz(): List<QuizQuestion> {
        val all = dao.getAllCustomWords().filter { it.wordType != "phrase" }
        if (all.size < QuizConstants.MIN_WORD_COUNT_FOR_QUIZ) return emptyList()
        return runtime.shuffled(all).take(minOf(QuizConstants.QUESTION_COUNT, all.size)).map { word ->
            val wrongPool = runtime.shuffled(all.filter { it.id != word.id }).take(3)
            val correct = WordChoice(id = word.id * -100, wordId = word.id, choiceText = word.meaning, isCorrect = true, displayOrder = 0)
            val wrongs = wrongPool.mapIndexed { index, wrong ->
                WordChoice(id = wrong.id * -100 - index - 1, wordId = word.id, choiceText = wrong.meaning, isCorrect = false, displayOrder = index + 1)
            }
            QuizQuestion(
                word = Word(id = word.id, trainingId = -1, english = word.english, meaning = word.meaning, phonetic = "", partOfSpeech = "単語", exampleSentence = word.exampleSentence, exampleTranslation = word.exampleTranslation, audioUrl = null, exampleAudioUrl = null, displayOrder = 0),
                choices = runtime.shuffled(listOf(correct) + wrongs).mapIndexed { index, choice -> choice.copy(displayOrder = index) }
            )
        }
    }

    private fun observeCustomStudyWords(type: String): Flow<List<CustomStudyWord>> {
        val contentType = ContentType.fromRouteValue(type)
        return if (contentType == ContentType.IDIOM) {
            dao.observeCustomIdiomsInStudyOrder().map { items ->
                items.map { CustomStudyWord(it.id, it.english, it.meaning, "", "") }
            }
        } else {
            dao.observeCustomWordsInStudyOrder().map { items ->
                items.filter { it.wordType != "phrase" }
                    .map { CustomStudyWord(it.id, it.english, it.meaning, it.exampleSentence, it.exampleTranslation) }
            }
        }
    }

    private suspend fun getCustomStudyWords(type: String): List<CustomStudyWord> {
        val contentType = ContentType.fromRouteValue(type)
        return if (contentType == ContentType.IDIOM) {
            dao.getCustomIdiomsInStudyOrder().map { CustomStudyWord(it.id, it.english, it.meaning, "", "") }
        } else {
            dao.getCustomWordsInStudyOrder()
                .filter { it.wordType != "phrase" }
                .map { CustomStudyWord(it.id, it.english, it.meaning, it.exampleSentence, it.exampleTranslation) }
        }
    }

    private fun buildCustomQuizQuestions(
        type: String,
        trainingId: Int,
        targets: List<CustomStudyWord>,
        all: List<CustomStudyWord>,
        displayOffset: Int
    ): List<QuizQuestion> {
        val contentType = ContentType.fromRouteValue(type)
        return targets.mapIndexed { questionIndex, target ->
            val wrongPool = runtime.shuffled(all.filter { it.id != target.id }).take(3)
            val domainWordId = contentType.wordDomainId(target.id)
            val correct = WordChoice(id = target.id * contentType.choiceIdMultiplier, wordId = domainWordId, choiceText = target.meaning, isCorrect = true, displayOrder = 0)
            val wrongs = wrongPool.mapIndexed { index, wrong ->
                WordChoice(id = wrong.id * contentType.choiceIdMultiplier - index - 1, wordId = domainWordId, choiceText = wrong.meaning, isCorrect = false, displayOrder = index + 1)
            }
            QuizQuestion(
                word = Word(
                    id = domainWordId,
                    trainingId = trainingId,
                    english = target.english,
                    meaning = target.meaning,
                    phonetic = "",
                    partOfSpeech = contentType.partOfSpeech,
                    exampleSentence = target.exampleSentence,
                    exampleTranslation = target.exampleTranslation,
                    audioUrl = null,
                    exampleAudioUrl = null,
                    displayOrder = displayOffset + questionIndex + 1
                ),
                choices = runtime.shuffled(listOf(correct) + wrongs).mapIndexed { index, choice -> choice.copy(displayOrder = index) }
            )
        }
    }

    private suspend fun ensureCustomContentCapacity(additionalCount: Int) {
        val currentCount = dao.customWordCount() + dao.customIdiomCount() + dao.customSentenceCount()
        if (currentCount + additionalCount > MAX_CUSTOM_CONTENT_ITEMS) {
            throw IllegalArgumentException("登録上限（${MAX_CUSTOM_CONTENT_ITEMS}件）を超えています")
        }
    }

}
