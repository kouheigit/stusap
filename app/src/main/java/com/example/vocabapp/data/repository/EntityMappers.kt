package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.entity.QuizAttemptAnswerEntity
import com.example.vocabapp.data.local.entity.WordChoiceEntity
import com.example.vocabapp.data.local.entity.WordEntity
import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.domain.model.WordChoice

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
