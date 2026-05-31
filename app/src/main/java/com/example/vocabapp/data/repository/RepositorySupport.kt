package com.example.vocabapp.data.repository

import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizConstants

const val MAX_CUSTOM_ENGLISH_CHARS = 200
const val MAX_CUSTOM_MEANING_CHARS = 500
const val MAX_CUSTOM_EXAMPLE_CHARS = 1_000
const val MAX_CUSTOM_SENTENCE_CHARS = 1_000
const val MAX_CUSTOM_CONTENT_ITEMS = 2_000

internal const val IDIOM_LESSON_START_ID = 100  // review_words テーブルの熟語レッスン先頭ID
internal const val CAPACITY_LOW_THRESHOLD = 200
internal const val CUSTOM_SENTENCE_LESSON_ID = -30_000

internal fun customLessonId(type: String): Int =
    ContentType.fromRouteValue(type).lessonId

internal fun customTrainingId(type: String, setNumber: Int): Int =
    ContentType.fromRouteValue(type).trainingId(setNumber)

internal fun customTrainingSetNumber(type: String, trainingId: Int): Int =
    (ContentType.fromRouteValue(type).lessonId - trainingId).coerceAtLeast(1)

internal fun customContentTypeForTrainingId(trainingId: Int): ContentType? =
    when {
        trainingId < ContentType.IDIOM.lessonId -> ContentType.IDIOM
        trainingId < ContentType.WORD.lessonId -> ContentType.WORD
        else -> null
    }

internal fun randomCustomTrainingId(type: String): Int =
    ContentType.fromRouteValue(type).randomTrainingId

internal fun customWordDomainId(type: String, sourceId: Int): Int =
    ContentType.fromRouteValue(type).wordDomainId(sourceId)

internal fun customTitle(type: String): String =
    ContentType.fromRouteValue(type).customTitle

internal fun List<String>.getOrEmpty(index: Int): String =
    if (index >= 0 && index < size) this[index] else ""

internal fun String.normalizeEnglish(): String = trim().lowercase()

private val BRACKET_REGEX = Regex("\\[([^\\]]+)\\]")

internal fun String.isQuizReadySentence(): Boolean {
    val bracketCount = BRACKET_REGEX.findAll(this).count()
    if (bracketCount > 0) return bracketCount == 4
    val wordCount = trim().split("\\s+".toRegex()).count { it.isNotBlank() }
    return wordCount >= 6
}

internal fun String.sentenceType(): String {
    val bracketCount = BRACKET_REGEX.findAll(this).count()
    return if (bracketCount > 0) "B" else "A"
}

internal data class CustomStudyWord(
    val id: Int,
    val english: String,
    val meaning: String,
    val exampleSentence: String,
    val exampleTranslation: String
)

internal fun customTrainingStartIndex(setNumber: Int): Int =
    (setNumber - 1).coerceAtLeast(0) * QuizConstants.QUESTION_COUNT

internal fun List<CustomStudyWord>.customTrainingTargets(setNumber: Int): List<CustomStudyWord> =
    drop(customTrainingStartIndex(setNumber))
        .take(QuizConstants.QUESTION_COUNT)
