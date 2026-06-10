package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.AppDatabase
import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.CustomPassageQuestionEntity
import com.example.vocabapp.data.local.entity.CustomPassageSetEntity
import com.example.vocabapp.domain.model.DocumentKind
import com.example.vocabapp.domain.model.PassageDocument
import com.example.vocabapp.domain.model.PassageQuestion
import com.example.vocabapp.domain.model.PassageSet
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomPassageRepositoryTest {
    private val database = mockk<AppDatabase>()
    private val dao = mockk<AppDao>()
    private val runtime = mockk<QuizRuntime>()
    private val repository by lazy { CustomPassageRepository(database, runtime) }

    init {
        every { database.appDao() } returns dao
    }

    @Test
    fun saveStoresSetAndQuestions() = runTest {
        val setSlot = slot<CustomPassageSetEntity>()
        val questionSlot = slot<List<CustomPassageQuestionEntity>>()
        every { runtime.nowMillis() } returns 1234L
        coEvery { dao.insertCustomPassageSet(capture(setSlot)) } returns 41L
        coEvery { dao.insertCustomPassageQuestions(capture(questionSlot)) } returns listOf(1L)

        val savedId = repository.save(sampleSet())

        assertEquals(41, savedId)
        assertEquals("Schedule notice", setSlot.captured.title)
        assertEquals("EMAIL", setSlot.captured.documentKind)
        assertEquals("Read and answer.", setSlot.captured.instruction)
        assertEquals("Body text", setSlot.captured.body)
        assertEquals(300, setSlot.captured.timeLimitSec)
        assertEquals(1234L, setSlot.captured.addedAt)
        assertEquals("Q1", questionSlot.captured.single().number)
        assertEquals("A\nB\nC\nD", questionSlot.captured.single().optionsText)
        assertEquals(2, questionSlot.captured.single().answerIndex)
        coVerify { dao.insertCustomPassageQuestions(any()) }
    }

    @Test
    fun getSetMapsStoredRowsToDomain() = runTest {
        coEvery { dao.getCustomPassageSet(41) } returns CustomPassageSetEntity(
            id = 41,
            title = "Schedule notice",
            documentKind = "EMAIL",
            instruction = "Read and answer.",
            body = "Body text",
            timeLimitSec = 300,
            addedAt = 1234L
        )
        coEvery { dao.getCustomPassageQuestions(41) } returns listOf(
            CustomPassageQuestionEntity(
                id = 7,
                setId = 41,
                number = "Q1",
                stem = "What changed?",
                optionsText = "A\nB\nC\nD",
                answerIndex = 2,
                explanation = "Because C is correct.",
                displayOrder = 0
            )
        )

        val set = repository.getSet(41)!!

        assertEquals("custom-passage-41", set.id)
        assertEquals(DocumentKind.EMAIL, set.documents.single().kind)
        assertEquals("Schedule notice", set.documents.single().title)
        assertEquals("Body text", set.documents.single().body)
        assertEquals("Q1", set.questions.single().number)
        assertEquals(listOf("A", "B", "C", "D"), set.questions.single().options)
        assertEquals(2, set.questions.single().answerIndex)
    }

    @Test
    fun getSetReturnsNullForUnknownId() = runTest {
        coEvery { dao.getCustomPassageSet(404) } returns null

        assertNull(repository.getSet(404))
    }

    private fun sampleSet(): PassageSet = PassageSet(
        id = "custom-import",
        instruction = "Read and answer.",
        documents = listOf(
            PassageDocument(
                kind = DocumentKind.EMAIL,
                title = "Schedule notice",
                body = "Body text"
            )
        ),
        questions = listOf(
            PassageQuestion(
                number = "Q1",
                stem = "What changed?",
                options = listOf("A", "B", "C", "D"),
                answerIndex = 2,
                explanation = "Because C is correct."
            )
        ),
        timeLimitSec = 300
    )
}
