package com.example.vocabapp.viewmodel

import com.example.vocabapp.data.repository.CustomPassageRepository
import com.example.vocabapp.domain.model.DocumentKind
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomPassageRegistrationViewModelTest {
    private lateinit var testDispatcher: TestDispatcher

    private fun buildViewModel(
        repository: CustomPassageRepository = mockk(relaxed = true)
    ) = CustomPassageRegistrationViewModel(
        repository = repository
    )

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
    fun changingChoiceCountShowsMatchingChoiceInputs() = runTest(testDispatcher) {
        val vm = buildViewModel()

        vm.updateCurrentChoiceCount(3)

        val state = vm.state.value
        assertEquals(3, state.currentChoiceCount)
        assertEquals(3, state.currentChoices.size)
        assertEquals(0, state.currentAnswerIndex)
    }

    @Test
    fun addManualQuestionStoresQuestionAndRestartsQuestionSetup() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.updateCurrentQuestionStem("What is the purpose of the email?")
        vm.updateCurrentChoice(0, "To cancel a class")
        vm.updateCurrentChoice(1, "To announce a schedule change")
        vm.updateCurrentChoice(2, "To introduce a teacher")
        vm.updateCurrentChoice(3, "To sell tickets")
        vm.updateCurrentAnswerIndex(1)

        vm.addManualQuestion()

        val state = vm.state.value
        assertEquals(1, state.manualQuestions.size)
        assertEquals("Q1", state.manualQuestions.single().number)
        assertEquals("What is the purpose of the email?", state.manualQuestions.single().stem)
        assertEquals(1, state.manualQuestions.single().answerIndex)
        assertEquals("", state.currentQuestionStem)
        assertEquals(listOf("", "", "", ""), state.currentChoices)
        assertFalse(state.manualQuestionSetupCompleted)
    }

    @Test
    fun completingManualQuestionsBuildsPreview() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.updateManualTitle("Email about schedule change")
        vm.updateManualDocumentType("email")
        vm.updateManualTimeLimitSec("420")
        vm.updateManualBody("Dear members,\nThe morning class will move to Room B.")
        vm.updateCurrentQuestionStem("What is the main purpose of this email?")
        vm.updateCurrentChoice(0, "To cancel every class")
        vm.updateCurrentChoice(1, "To announce a schedule change")
        vm.updateCurrentChoice(2, "To introduce a new teacher")
        vm.updateCurrentChoice(3, "To sell tickets")
        vm.updateCurrentAnswerIndex(1)

        vm.completeManualQuestionSetup()

        val preview = vm.state.value.preview
        assertNotNull(preview)
        assertTrue(vm.state.value.manualQuestionSetupCompleted)
        assertEquals(DocumentKind.EMAIL, preview!!.documents.single().kind)
        assertEquals("Email about schedule change", preview.documents.single().title)
        assertEquals(420, preview.timeLimitSec)
        assertEquals(1, preview.questions.size)
    }

    @Test
    fun completingTwoChoiceQuestionBuildsTwoChoicePreview() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.updateManualBody("The shop will close at six.")
        vm.updateCurrentChoiceCount(2)
        vm.updateCurrentQuestionStem("When will the shop close?")
        vm.updateCurrentChoice(0, "At five")
        vm.updateCurrentChoice(1, "At six")
        vm.updateCurrentAnswerIndex(1)

        vm.completeManualQuestionSetup()

        val preview = vm.state.value.preview!!
        assertEquals(listOf("At five", "At six"), preview.questions.single().options)
        assertEquals(1, preview.questions.single().answerIndex)
    }

    @Test
    fun addQuestionThenCompleteBuildsMultipleManualQuestions() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.updateManualBody("The seminar starts at ten and ends at noon.")
        vm.updateCurrentQuestionStem("When does the seminar start?")
        vm.updateCurrentChoice(0, "At nine")
        vm.updateCurrentChoice(1, "At ten")
        vm.updateCurrentChoice(2, "At noon")
        vm.updateCurrentChoice(3, "At three")
        vm.updateCurrentAnswerIndex(1)
        vm.addManualQuestion()
        vm.updateCurrentChoiceCount(3)
        vm.updateCurrentQuestionStem("When does the seminar end?")
        vm.updateCurrentChoice(0, "At ten")
        vm.updateCurrentChoice(1, "At eleven")
        vm.updateCurrentChoice(2, "At noon")
        vm.updateCurrentAnswerIndex(2)

        vm.completeManualQuestionSetup()

        val preview = vm.state.value.preview!!
        assertEquals(2, preview.questions.size)
        assertEquals("Q1", vm.state.value.manualQuestions[0].number)
        assertEquals("Q2", vm.state.value.manualQuestions[1].number)
        assertEquals(3, preview.questions[1].options.size)
    }

    @Test
    fun completingWithoutBodyShowsValidationError() = runTest(testDispatcher) {
        val vm = buildViewModel()
        vm.updateCurrentQuestionStem("What is missing?")
        vm.updateCurrentChoice(0, "Body")
        vm.updateCurrentChoice(1, "Title")
        vm.updateCurrentChoice(2, "Time")
        vm.updateCurrentChoice(3, "Type")

        vm.completeManualQuestionSetup()

        assertEquals("本文を入力してください", vm.state.value.errorMessage)
        assertEquals(null, vm.state.value.preview)
    }

    @Test
    fun saveUsesCompletedManualPreview() = runTest(testDispatcher) {
        val repository = mockk<CustomPassageRepository>()
        coEvery { repository.save(any()) } returns 12
        val vm = buildViewModel(repository)
        vm.updateManualBody("The class will move to Room B.")
        vm.updateCurrentQuestionStem("Where will the class move?")
        vm.updateCurrentChoice(0, "Room A")
        vm.updateCurrentChoice(1, "Room B")
        vm.updateCurrentChoice(2, "Room C")
        vm.updateCurrentChoice(3, "Room D")
        vm.updateCurrentAnswerIndex(1)
        vm.completeManualQuestionSetup()

        vm.save()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.save(match { it.questions.single().stem == "Where will the class move?" }) }
        assertEquals(12, vm.state.value.savedId)
    }
}
