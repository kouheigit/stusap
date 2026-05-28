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
            return WordImportPreview(errors = listOf(ImportErrorRow(1, "ファイルにデータが見つかりません。英語と意味の2列を入力してください", emptyList())))
        }

        val columns = resolveWordImportColumns(rows.first())
        if (columns == null) {
            val header = CsvHeader(rows.first())
            val englishIndex = header.findIndex(WORD_ENGLISH_HEADER_ALIASES)
            val meaningIndex = header.findIndex(MEANING_HEADER_ALIASES)
            return WordImportPreview(
                totalRows = rows.drop(1).size,
                errors = listOf(missingWordHeaderError(rows.first(), englishIndex, meaningIndex))
            )
        }
        val dataRows = rows.drop(columns.dataStartIndex)
        val existing = (dao.getNormalizedSeedEnglish() + dao.getNormalizedCustomEnglish() +
            dao.getNormalizedCustomIdiomEnglish()).toSet()
        val errorCollector = ImportIssueCollector<ImportedWord>()
        val uniqueCollector = UniqueImportCollector(
            existing = existing,
            availableSlots = remainingCustomContentCapacity(),
            issueCollector = errorCollector
        )

        dataRows.forEachIndexed { index, row ->
            val rowNumber = columns.dataStartIndex + index + 1
            val english = row.getOrEmpty(columns.englishIndex).trim()
            val meaning = row.getOrEmpty(columns.meaningIndex).trim()
            val example = row.getOrEmpty(columns.exampleIndex).trim()
            val exampleTranslation = row.getOrEmpty(columns.exampleTranslationIndex).trim()
            val rawType = row.getOrEmpty(columns.typeIndex).trim().lowercase()

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
            totalRows = dataRows.size,
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
        val wordItems = eligible.filter { it.type == "word" }.mapIndexed { index, word ->
            CustomWordEntity(
                english = word.english.trim().take(MAX_CUSTOM_ENGLISH_CHARS),
                meaning = word.meaning.trim().take(MAX_CUSTOM_MEANING_CHARS),
                addedAt = now + index,
                exampleSentence = word.exampleSentence.trim().take(MAX_CUSTOM_EXAMPLE_CHARS),
                exampleTranslation = word.exampleTranslation.trim().take(MAX_CUSTOM_EXAMPLE_CHARS),
                wordType = word.type
            )
        }
        val idiomItems = eligible.filter { it.type == "phrase" }.mapIndexed { index, word ->
            CustomIdiomEntity(
                english = word.english.trim().take(MAX_CUSTOM_ENGLISH_CHARS),
                meaning = word.meaning.trim().take(MAX_CUSTOM_MEANING_CHARS),
                addedAt = now + index
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
            return SentenceImportPreview(errors = listOf(ImportErrorRow(1, "ファイルにデータが見つかりません。英文と意味の2列を入力してください", emptyList())))
        }

        val columns = resolveSentenceImportColumns(rows.first())
        if (columns == null) {
            val header = CsvHeader(rows.first())
            val sentenceIndex = header.findIndex(SENTENCE_HEADER_ALIASES)
            val meaningIndex = header.findIndex(MEANING_HEADER_ALIASES)
            return SentenceImportPreview(
                totalRows = rows.drop(1).size,
                errors = listOf(missingSentenceHeaderError(rows.first(), sentenceIndex, meaningIndex))
            )
        }
        val dataRows = rows.drop(columns.dataStartIndex)
        if (dataRows.isEmpty()) {
            return SentenceImportPreview(
                totalRows = 0,
                errors = listOf(ImportErrorRow(1, "ヘッダー行のみでデータ行がありません。2行目以降に英文と意味を入力してください", rows.first()))
            )
        }

        val existing = dao.getNormalizedCustomSentences().toSet()
        val errorCollector = ImportIssueCollector<ImportedSentence>()
        val uniqueCollector = UniqueImportCollector(
            existing = existing,
            availableSlots = remainingCustomContentCapacity(),
            issueCollector = errorCollector
        )

        dataRows.forEachIndexed { index, row ->
            val rowNumber = columns.dataStartIndex + index + 1
            val sentence = row.getOrEmpty(columns.sentenceIndex).trim()
            val meaning = row.getOrEmpty(columns.meaningIndex).trim()
            if (!validateSentenceImportRow(rowNumber, row, sentence, meaning, errorCollector::addError)) {
                return@forEachIndexed
            }

            val imported = ImportedSentence(sentence, meaning, isQuizReady = sentence.isQuizReadySentence())
            uniqueCollector.addIfUnique(sentence.normalizeEnglish(), imported, rowNumber, row)
        }

        return SentenceImportPreview(
            totalRows = dataRows.size,
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
