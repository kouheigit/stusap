package com.example.vocabapp.viewmodel

import com.example.vocabapp.data.repository.CustomPassageRepository
import com.example.vocabapp.domain.model.DocumentKind
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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

    private fun buildViewModel() = CustomPassageRegistrationViewModel(
        repository = mockk<CustomPassageRepository>(relaxed = true)
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
}
