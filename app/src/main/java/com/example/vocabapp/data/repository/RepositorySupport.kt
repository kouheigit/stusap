package com.example.vocabapp.data.repository

import com.example.vocabapp.domain.model.ContentType

const val MAX_CUSTOM_ENGLISH_CHARS = 200
const val MAX_CUSTOM_MEANING_CHARS = 500
const val MAX_CUSTOM_EXAMPLE_CHARS = 1_000
const val MAX_CUSTOM_SENTENCE_CHARS = 1_000
const val MAX_CUSTOM_CONTENT_ITEMS = 2_000

internal const val IDIOM_LESSON_START_ID = 100  // review_words テーブルの熟語レッスン先頭ID
internal const val CUSTOM_SENTENCE_LESSON_ID = -30_000

internal fun customLessonId(type: String): Int =
    ContentType.fromRouteValue(type).lessonId

internal fun customTrainingId(type: String, setNumber: Int): Int =
    ContentType.fromRouteValue(type).trainingId(setNumber)

internal fun randomCustomTrainingId(type: String): Int =
    ContentType.fromRouteValue(type).randomTrainingId

internal fun customWordDomainId(type: String, sourceId: Int): Int =
    ContentType.fromRouteValue(type).wordDomainId(sourceId)

internal fun customTitle(type: String): String =
    ContentType.fromRouteValue(type).customTitle

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
