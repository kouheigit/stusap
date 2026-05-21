package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.AppDatabase
import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizConstants
import com.example.vocabapp.domain.model.QuizQuestion
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.domain.model.WordChoice
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

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
