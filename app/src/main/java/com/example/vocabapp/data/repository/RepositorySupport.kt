package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.entity.QuizAttemptAnswerEntity
import com.example.vocabapp.data.local.entity.WordChoiceEntity
import com.example.vocabapp.data.local.entity.WordEntity
import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.domain.model.WordChoice

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

internal fun WordEntity.toDomain(): Word =
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
        displayOrder = displayOrder,
        isFavorite = isFavorite,
        isLearned = isLearned
    )

internal fun WordChoiceEntity.toDomain(): WordChoice =
    WordChoice(
        id = id,
        wordId = wordId,
        choiceText = choiceText,
        isCorrect = isCorrect,
        displayOrder = displayOrder
    )

internal fun AnswerRecord.toAnswerEntity(attemptId: Long, word: Word?): QuizAttemptAnswerEntity =
    QuizAttemptAnswerEntity(
        quizAttemptId = attemptId,
        wordId = wordId,
        selectedChoiceId = selectedChoiceId,
        isCorrect = isCorrect,
        answeredAt = answeredAt,
        responseMillis = responseMillis,
        selectedUnknown = selectedUnknown,
        wordTrainingId = word?.trainingId ?: 0,
        wordEnglish = word?.english.orEmpty(),
        wordMeaning = word?.meaning.orEmpty(),
        wordPhonetic = word?.phonetic.orEmpty(),
        wordPartOfSpeech = word?.partOfSpeech.orEmpty(),
        wordExampleSentence = word?.exampleSentence.orEmpty(),
        wordExampleTranslation = word?.exampleTranslation.orEmpty(),
        wordAudioUrl = word?.audioUrl,
        wordExampleAudioUrl = word?.exampleAudioUrl,
        wordDisplayOrder = word?.displayOrder ?: 0
    )

internal fun QuizAttemptAnswerEntity.toSnapshotWord(): Word? {
    if (wordEnglish.isBlank() && wordMeaning.isBlank()) return null
    return Word(
        id = wordId,
        trainingId = wordTrainingId,
        english = wordEnglish,
        meaning = wordMeaning,
        phonetic = wordPhonetic,
        partOfSpeech = wordPartOfSpeech,
        exampleSentence = wordExampleSentence,
        exampleTranslation = wordExampleTranslation,
        audioUrl = wordAudioUrl,
        exampleAudioUrl = wordExampleAudioUrl,
        displayOrder = wordDisplayOrder
    )
}

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
