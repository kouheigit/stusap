package com.example.vocabapp.data.repository

import com.example.vocabapp.MAX_IMPORT_COLUMNS
import com.example.vocabapp.domain.model.ImportErrorRow

internal const val IMPORT_INSERT_CHUNK_SIZE = 200
private const val MAX_PREVIEW_DUPLICATES = 50
private const val MAX_PREVIEW_ERRORS = 50
private val CSV_IDIOM_TYPES = setOf("phrase", "idiom", "custom_idiom", "custom_idioms")

internal val SENTENCE_HEADER_ALIASES = setOf("sentence", "english", "英文", "文章", "例文", "英語")
internal val MEANING_HEADER_ALIASES = setOf("meaning", "japanese meaning", "日本語の意味", "意味", "japanese", "訳", "和訳", "日本語")
internal val WORD_ENGLISH_HEADER_ALIASES = setOf("english", "english phrase", "english word", "英語", "英文", "word", "単語", "英単語")
internal val WORD_EXAMPLE_HEADER_ALIASES = setOf("example", "example sentence", "例文", "例文（英語）", "english example")
internal val WORD_EXAMPLE_TRANSLATION_HEADER_ALIASES = setOf("example_translation", "例文（日本語）", "例文訳", "japanese example", "和訳例文")
internal val TYPE_HEADER_ALIASES = setOf("type", "種類", "タイプ")

internal class CsvHeader(header: List<String>) {
    private val lowerValues = header.map { it.trim().lowercase() }

    fun findIndex(aliases: Set<String>): Int = lowerValues.indexOfFirst { it in aliases }
}

internal data class ImportColumns(
    val sentenceIndex: Int,
    val meaningIndex: Int,
    val dataStartIndex: Int
)

internal fun resolveSentenceImportColumns(firstRow: List<String>): ImportColumns? {
    val header = CsvHeader(firstRow)
    val sentenceIndex = header.findIndex(SENTENCE_HEADER_ALIASES)
    val meaningIndex = header.findIndex(MEANING_HEADER_ALIASES)
    return when {
        sentenceIndex != -1 && meaningIndex != -1 -> ImportColumns(sentenceIndex, meaningIndex, dataStartIndex = 1)
        sentenceIndex == -1 && meaningIndex == -1 -> ImportColumns(sentenceIndex = 0, meaningIndex = 1, dataStartIndex = 0)
        else -> null
    }
}

internal class ImportIssueCollector<T> {
    val duplicates = mutableListOf<T>()
    val errors = mutableListOf<ImportErrorRow>()
    var omittedDuplicateCount = 0
        private set
    var omittedErrorCount = 0
        private set

    fun addError(rowNumber: Int, reason: String, row: List<String>) {
        if (errors.size < MAX_PREVIEW_ERRORS) {
            errors += ImportErrorRow(rowNumber, reason, row.take(MAX_IMPORT_COLUMNS))
        } else {
            omittedErrorCount++
        }
    }

    fun addDuplicate(item: T) {
        if (duplicates.size < MAX_PREVIEW_DUPLICATES) {
            duplicates += item
        } else {
            omittedDuplicateCount++
        }
    }
}

internal class UniqueImportCollector<T>(
    private val existing: Set<String>,
    private val availableSlots: Int,
    private val issueCollector: ImportIssueCollector<T>
) {
    private val seenInFile = mutableSetOf<String>()
    private val _newItems = mutableListOf<T>()
    val newItems: List<T> get() = _newItems

    fun addIfUnique(normalizedKey: String, item: T, rowNumber: Int, row: List<String>) {
        when {
            normalizedKey in existing || normalizedKey in seenInFile -> issueCollector.addDuplicate(item)
            _newItems.size >= availableSlots -> issueCollector.addError(rowNumber, "登録上限（${MAX_CUSTOM_CONTENT_ITEMS}件）を超えています", row)
            else -> {
                seenInFile += normalizedKey
                _newItems += item
            }
        }
    }
}

internal fun validateWordImportRow(
    rowNumber: Int,
    row: List<String>,
    english: String,
    meaning: String,
    example: String,
    exampleTranslation: String,
    rawType: String,
    addError: (Int, String, List<String>) -> Unit
): String? {
    if (english.isBlank() || meaning.isBlank()) {
        addError(rowNumber, "english と meaning は必須です", row)
        return null
    }
    if (meaning.length > MAX_CUSTOM_MEANING_CHARS) {
        addError(rowNumber, "meaning は${MAX_CUSTOM_MEANING_CHARS}文字以内にしてください", row)
        return null
    }
    if (example.length > MAX_CUSTOM_EXAMPLE_CHARS || exampleTranslation.length > MAX_CUSTOM_EXAMPLE_CHARS) {
        addError(rowNumber, "例文と例文訳は各${MAX_CUSTOM_EXAMPLE_CHARS}文字以内にしてください", row)
        return null
    }
    val wordType = when {
        rawType.isBlank() -> if (english.contains(Regex("\\s"))) "phrase" else "word"
        rawType == "word" -> rawType
        rawType in CSV_IDIOM_TYPES -> "phrase"
        else -> {
            addError(rowNumber, "type は word / phrase / idiom / custom_idioms のみ指定できます。文章は文章インポートから取り込んでください", row)
            return null
        }
    }
    if (english.length > MAX_CUSTOM_ENGLISH_CHARS) {
        addError(rowNumber, "english は${MAX_CUSTOM_ENGLISH_CHARS}文字以内にしてください", row)
        return null
    }
    return wordType
}

internal fun validateSentenceImportRow(
    rowNumber: Int,
    row: List<String>,
    sentence: String,
    meaning: String,
    addError: (Int, String, List<String>) -> Unit
): Boolean {
    if (sentence.isBlank()) {
        addError(rowNumber, "sentence 列が空です。英文を入力してください", row)
        return false
    }
    if (meaning.isBlank()) {
        addError(rowNumber, "meaning 列が空です。日本語の意味を入力してください", row)
        return false
    }
    if (sentence.length > MAX_CUSTOM_SENTENCE_CHARS) {
        addError(rowNumber, "sentence は${MAX_CUSTOM_SENTENCE_CHARS}文字以内にしてください（現在${sentence.length}文字）", row)
        return false
    }
    if (meaning.length > MAX_CUSTOM_MEANING_CHARS) {
        addError(rowNumber, "meaning は${MAX_CUSTOM_MEANING_CHARS}文字以内にしてください（現在${meaning.length}文字）", row)
        return false
    }
    return true
}

internal fun missingWordHeaderError(header: List<String>, englishIndex: Int, meaningIndex: Int): ImportErrorRow {
    val missing = buildList {
        if (englishIndex == -1) add("英語(例: english, English Phrase, 英語)")
        if (meaningIndex == -1) add("意味(例: meaning, 日本語の意味, 意味)")
    }.joinToString(", ")
    return ImportErrorRow(1, "必須ヘッダーが見つかりません: $missing\n検出されたヘッダー: ${header.joinToString(", ")}", header)
}

internal fun missingSentenceHeaderError(header: List<String>, sentenceIndex: Int, meaningIndex: Int): ImportErrorRow {
    val missing = buildList {
        if (sentenceIndex == -1) add("文章(例: sentence, 英文, 文章)")
        if (meaningIndex == -1) add("意味(例: meaning, 日本語の意味, 意味)")
    }.joinToString(", ")
    return ImportErrorRow(1, "必須ヘッダーが見つかりません: $missing\n検出されたヘッダー: ${header.joinToString(", ")}", header)
}
