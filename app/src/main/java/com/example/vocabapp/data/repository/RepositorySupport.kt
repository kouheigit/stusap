package com.example.vocabapp.data.repository

const val MAX_CUSTOM_ENGLISH_CHARS = 200
const val MAX_CUSTOM_MEANING_CHARS = 500
const val MAX_CUSTOM_EXAMPLE_CHARS = 1_000
const val MAX_CUSTOM_SENTENCE_CHARS = 1_000
const val MAX_CUSTOM_CONTENT_ITEMS = 2_000

internal const val CUSTOM_TYPE_WORD = "word"
internal const val CUSTOM_TYPE_IDIOM = "idiom"
internal const val CUSTOM_WORD_LESSON_ID = -10_000
internal const val CUSTOM_IDIOM_LESSON_ID = -20_000
internal const val CUSTOM_SENTENCE_LESSON_ID = -30_000

internal fun customLessonId(type: String): Int =
    if (type == CUSTOM_TYPE_IDIOM) CUSTOM_IDIOM_LESSON_ID else CUSTOM_WORD_LESSON_ID

internal fun customTrainingId(type: String, setNumber: Int): Int =
    customLessonId(type) - setNumber

internal fun randomCustomTrainingId(type: String): Int =
    customLessonId(type) - 999

internal fun customWordDomainId(type: String, sourceId: Int): Int =
    (if (type == CUSTOM_TYPE_IDIOM) -200_000 else -100_000) - sourceId

internal fun customTitle(type: String): String =
    if (type == CUSTOM_TYPE_IDIOM) "カスタム英熟語" else "カスタム英単語"

internal fun List<String>.getOrEmpty(index: Int): String =
    if (index >= 0 && index < size) this[index] else ""

internal fun String.normalizeEnglish(): String = trim().lowercase()

internal data class CustomStudyWord(
    val id: Int,
    val english: String,
    val meaning: String,
    val exampleSentence: String,
    val exampleTranslation: String
)
