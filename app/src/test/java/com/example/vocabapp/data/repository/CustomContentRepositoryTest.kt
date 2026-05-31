package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.AppDatabase
import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.domain.model.ContentType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CustomContentRepositoryTest {

    private val database = mockk<AppDatabase>()
    private val dao = mockk<AppDao>()
    private val repository by lazy { CustomContentRepository(database, QuizRuntime()) }

    init {
        every { database.appDao() } returns dao
    }

    @Test
    fun buildCustomTrainingQuiz_usesIdiomRowsForRequestedSet() = runTest {
        val idioms = customIdioms(70)
        coEvery { dao.getCustomIdiomsInStudyOrder() } returns idioms
        coEvery { dao.getCustomIdiomsForStudyRange(limit = 10, offset = 60) } returns idioms.drop(60).take(10)

        val questions = repository.buildCustomTrainingQuiz(ContentType.IDIOM.routeValue, setNumber = 7)

        assertEquals((61..70).map { "idiom$it" }, questions.map { it.word.english })
    }

    @Test
    fun buildCustomTrainingQuiz_usesWordRowsForRequestedSet() = runTest {
        val words = customWords(50)
        coEvery { dao.getCustomWordsInStudyOrder() } returns words
        coEvery { dao.getCustomWordsForStudyRange(limit = 10, offset = 40) } returns words.drop(40).take(10)

        val questions = repository.buildCustomTrainingQuiz(ContentType.WORD.routeValue, setNumber = 5)

        assertEquals((41..50).map { "word$it" }, questions.map { it.word.english })
    }

    private fun customIdioms(count: Int): List<CustomIdiomEntity> =
        (1..count).map { index ->
            CustomIdiomEntity(
                id = index,
                english = "idiom$index",
                meaning = "meaning$index",
                addedAt = (count - index).toLong()
            )
        }

    private fun customWords(count: Int): List<CustomWordEntity> =
        (1..count).map { index ->
            CustomWordEntity(
                id = index,
                english = "word$index",
                meaning = "meaning$index",
                addedAt = (count - index).toLong()
            )
        }
}
