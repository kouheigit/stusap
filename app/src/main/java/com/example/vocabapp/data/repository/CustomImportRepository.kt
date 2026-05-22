package com.example.vocabapp.data.repository

import androidx.room.withTransaction
import com.example.vocabapp.data.local.AppDatabase
import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.domain.model.ImportErrorRow
import com.example.vocabapp.domain.model.ImportedSentence
import com.example.vocabapp.domain.model.ImportedWord
import com.example.vocabapp.domain.model.SentenceImportPreview
import com.example.vocabapp.domain.model.SentenceImportResult
import com.example.vocabapp.domain.model.WordImportPreview
import com.example.vocabapp.domain.model.WordImportResult
import com.example.vocabapp.parseCsvRows
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomImportRepository @Inject constructor(
    private val database: AppDatabase,
    private val runtime: QuizRuntime
) {
    private val dao: AppDao = database.appDao()

    suspend fun previewCustomWordCsv(csvText: String): WordImportPreview {
        return previewCustomWordRows(parseCsvRows(csvText))
    }

    suspend fun previewCustomWordRows(rows: List<List<String>>): WordImportPreview {
        if (rows.isEmpty()) {
            return WordImportPreview(errors = listOf(ImportErrorRow(1, "CSVが空です", emptyList())))
        }

        val header = CsvHeader(rows.first())
        val englishIndex = header.findIndex(WORD_ENGLISH_HEADER_ALIASES)
        val meaningIndex = header.findIndex(MEANING_HEADER_ALIASES)
        if (englishIndex == -1 || meaningIndex == -1) {
            return WordImportPreview(
                totalRows = rows.drop(1).size,
                errors = listOf(missingWordHeaderError(rows.first(), englishIndex, meaningIndex))
            )
        }

        val exampleIndex = header.findIndex(WORD_EXAMPLE_HEADER_ALIASES)
        val exampleTranslationIndex = header.findIndex(WORD_EXAMPLE_TRANSLATION_HEADER_ALIASES)
        val typeIndex = header.findIndex(TYPE_HEADER_ALIASES)
        val existing = (dao.getNormalizedSeedEnglish() + dao.getNormalizedCustomEnglish() +
            dao.getNormalizedCustomIdiomEnglish()).toSet()
        val errorCollector = ImportIssueCollector<ImportedWord>()
        val uniqueCollector = UniqueImportCollector(
            existing = existing,
            availableSlots = remainingCustomContentCapacity(),
            issueCollector = errorCollector
        )

        rows.drop(1).forEachIndexed { index, row ->
            val rowNumber = index + 2
            val english = row.getOrEmpty(englishIndex).trim()
            val meaning = row.getOrEmpty(meaningIndex).trim()
            val example = row.getOrEmpty(exampleIndex).trim()
            val exampleTranslation = row.getOrEmpty(exampleTranslationIndex).trim()
            val rawType = row.getOrEmpty(typeIndex).trim().lowercase()

            val wordType = validateWordImportRow(
                rowNumber = rowNumber,
                row = row,
                english = english,
                meaning = meaning,
                example = example,
                exampleTranslation = exampleTranslation,
                rawType = rawType,
                addError = errorCollector::addError
            ) ?: return@forEachIndexed

            val imported = ImportedWord(english, meaning, example, exampleTranslation, wordType)
            uniqueCollector.addIfUnique(english.normalizeEnglish(), imported, rowNumber, row)
        }

        return WordImportPreview(
            totalRows = rows.drop(1).size,
            newWords = uniqueCollector.newItems,
            duplicateWords = errorCollector.duplicates,
            errors = errorCollector.errors,
            omittedDuplicateCount = errorCollector.omittedDuplicateCount,
            omittedErrorCount = errorCollector.omittedErrorCount
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
        wordItems.chunked(IMPORT_INSERT_CHUNK_SIZE).forEach { dao.insertCustomWords(it) }
        idiomItems.chunked(IMPORT_INSERT_CHUNK_SIZE).forEach { dao.insertCustomIdioms(it) }
        val lateDuplicates = preview.newWords.size - eligible.size
        WordImportResult(
            totalRows = preview.totalRows,
            insertedCount = wordItems.size,
            insertedIdiomCount = idiomItems.size,
            duplicateCount = preview.duplicateCount + lateDuplicates,
            errorCount = preview.errorCount
        )
    }

    suspend fun previewCustomSentenceCsv(csvText: String): SentenceImportPreview {
        return previewCustomSentenceRows(parseCsvRows(csvText))
    }

    suspend fun previewCustomSentenceRows(rows: List<List<String>>): SentenceImportPreview {
        if (rows.isEmpty()) {
            return SentenceImportPreview(errors = listOf(ImportErrorRow(1, "ファイルにデータが見つかりません。1行目にヘッダー（sentence, meaning）を入れてください", emptyList())))
        }
        if (rows.size == 1) {
            return SentenceImportPreview(
                totalRows = 0,
                errors = listOf(ImportErrorRow(1, "ヘッダー行のみでデータ行がありません。2行目以降に英文と意味を入力してください", rows.first()))
            )
        }

        val header = CsvHeader(rows.first())
        val sentenceIndex = header.findIndex(SENTENCE_HEADER_ALIASES)
        val meaningIndex = header.findIndex(MEANING_HEADER_ALIASES)
        if (sentenceIndex == -1 || meaningIndex == -1) {
            return SentenceImportPreview(
                totalRows = rows.drop(1).size,
                errors = listOf(missingSentenceHeaderError(rows.first(), sentenceIndex, meaningIndex))
            )
        }

        val existing = dao.getNormalizedCustomSentences().toSet()
        val errorCollector = ImportIssueCollector<ImportedSentence>()
        val uniqueCollector = UniqueImportCollector(
            existing = existing,
            availableSlots = remainingCustomContentCapacity(),
            issueCollector = errorCollector
        )

        rows.drop(1).forEachIndexed { index, row ->
            val rowNumber = index + 2
            val sentence = row.getOrEmpty(sentenceIndex).trim()
            val meaning = row.getOrEmpty(meaningIndex).trim()
            if (!validateSentenceImportRow(rowNumber, row, sentence, meaning, errorCollector::addError)) {
                return@forEachIndexed
            }

            val imported = ImportedSentence(sentence, meaning, isQuizReady = sentence.isQuizReadySentence())
            uniqueCollector.addIfUnique(sentence.normalizeEnglish(), imported, rowNumber, row)
        }

        return SentenceImportPreview(
            totalRows = rows.drop(1).size,
            newSentences = uniqueCollector.newItems,
            duplicateSentences = errorCollector.duplicates,
            errors = errorCollector.errors,
            omittedDuplicateCount = errorCollector.omittedDuplicateCount,
            omittedErrorCount = errorCollector.omittedErrorCount
        )
    }

    suspend fun importCustomSentences(preview: SentenceImportPreview): SentenceImportResult = database.withTransaction {
        val now = runtime.nowMillis()
        ensureCustomContentCapacity(preview.newSentences.size)
        val existing = dao.getNormalizedCustomSentences().toSet()
        val seen = mutableSetOf<String>()
        val eligible = preview.newSentences.filter { sentence ->
            val normalized = sentence.sentence.normalizeEnglish()
            normalized !in existing && seen.add(normalized)
        }
        val sentenceItems = eligible.map { sentence ->
            CustomSentenceEntity(
                sentence = sentence.sentence.trim().take(MAX_CUSTOM_SENTENCE_CHARS),
                meaning = sentence.meaning.trim().take(MAX_CUSTOM_MEANING_CHARS),
                addedAt = now,
                importedFromFile = preview.sourceFileName?.take(128)
            )
        }
        sentenceItems.chunked(IMPORT_INSERT_CHUNK_SIZE).forEach { dao.insertCustomSentences(it) }
        val lateDuplicates = preview.newSentences.size - eligible.size
        SentenceImportResult(
            totalRows = preview.totalRows,
            insertedCount = sentenceItems.size,
            quizReadyInsertedCount = eligible.count { it.isQuizReady },
            duplicateCount = preview.duplicateCount + lateDuplicates,
            errorCount = preview.errorCount
        )
    }

    suspend fun remainingCustomContentCapacity(): Int {
        val currentCount = dao.customWordCount() + dao.customIdiomCount() + dao.customSentenceCount()
        return (MAX_CUSTOM_CONTENT_ITEMS - currentCount).coerceAtLeast(0)
    }

    private suspend fun ensureCustomContentCapacity(additionalCount: Int) {
        val currentCount = dao.customWordCount() + dao.customIdiomCount() + dao.customSentenceCount()
        if (currentCount + additionalCount > MAX_CUSTOM_CONTENT_ITEMS) {
            throw IllegalArgumentException("登録上限（${MAX_CUSTOM_CONTENT_ITEMS}件）を超えています")
        }
    }
}
