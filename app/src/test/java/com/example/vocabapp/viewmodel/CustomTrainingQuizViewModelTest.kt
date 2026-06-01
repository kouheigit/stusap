package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.domain.model.ContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomTrainingQuizViewModelTest {

    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsRequestedWordSetNumberOnInit() = runTest(testDispatcher) {
        val repository = mockk<CustomContentRepository>()
        coEvery { repository.buildCustomTrainingQuiz(any(), any()) } returns emptyList()

        CustomTrainingQuizViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "type" to ContentType.WORD.routeValue,
                    "setNumber" to 5
                )
            ),
            repository = repository,
            quizRepository = mockk<QuizRepository>(relaxed = true)
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.buildCustomTrainingQuiz(ContentType.WORD.routeValue, 5)
        }
        coVerify(exactly = 0) {
            repository.buildCustomTrainingQuiz(ContentType.WORD.routeValue, 0)
        }
    }

    @Test
    fun loadsRequestedIdiomSetNumberOnInit() = runTest(testDispatcher) {
        val repository = mockk<CustomContentRepository>()
        coEvery { repository.buildCustomTrainingQuiz(any(), any()) } returns emptyList()

        CustomTrainingQuizViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "type" to ContentType.IDIOM.routeValue,
                    "setNumber" to 3
                )
            ),
            repository = repository,
            quizRepository = mockk<QuizRepository>(relaxed = true)
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.buildCustomTrainingQuiz(ContentType.IDIOM.routeValue, 3)
        }
        coVerify(exactly = 0) {
            repository.buildCustomTrainingQuiz(ContentType.IDIOM.routeValue, 0)
        }
    }
}
