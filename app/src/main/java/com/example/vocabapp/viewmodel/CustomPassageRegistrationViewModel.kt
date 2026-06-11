package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.import.PassageTextImportParser
import com.example.vocabapp.data.repository.CustomPassageRepository
import com.example.vocabapp.domain.model.DocumentKind
import com.example.vocabapp.domain.model.PassageDocument
import com.example.vocabapp.domain.model.PassageQuestion
import com.example.vocabapp.domain.model.PassageSet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomPassageRegistrationUiState(
    val rawText: String = "",
    val manualTitle: String = "",
    val manualDocumentType: String = "article",
    val manualTimeLimitSec: String = "300",
    val manualBody: String = "",
    val currentQuestionStem: String = "",
    val currentChoiceCount: Int = 4,
    val currentChoices: List<String> = List(4) { "" },
    val currentAnswerIndex: Int = 0,
    val currentExplanation: String = "",
    val manualQuestions: List<ManualPassageQuestion> = emptyList(),
    val manualQuestionSetupCompleted: Boolean = false,
    val preview: PassageSet? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val savedId: Int? = null
)

data class ManualPassageQuestion(
    val number: String,
    val stem: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String? = null
)

@HiltViewModel
class CustomPassageRegistrationViewModel @Inject constructor(
    private val repository: CustomPassageRepository
) : ViewModel() {
    private val parser = PassageTextImportParser()
    private val _state = MutableStateFlow(CustomPassageRegistrationUiState())
    val state: StateFlow<CustomPassageRegistrationUiState> = _state.asStateFlow()

    fun onTextChanged(value: String) {
        _state.update {
            it.copy(
                rawText = value,
                preview = null,
                errorMessage = null,
                savedId = null
            )
        }
    }

    fun updateManualTitle(value: String) {
        _state.update { it.copy(manualTitle = value, preview = null, errorMessage = null, savedId = null) }
    }

    fun updateManualDocumentType(value: String) {
        _state.update { it.copy(manualDocumentType = value, preview = null, errorMessage = null, savedId = null) }
    }

    fun updateManualTimeLimitSec(value: String) {
        _state.update { it.copy(manualTimeLimitSec = value, preview = null, errorMessage = null, savedId = null) }
    }

    fun updateManualBody(value: String) {
        _state.update { it.copy(manualBody = value, preview = null, errorMessage = null, savedId = null) }
    }

    fun updateCurrentQuestionStem(value: String) {
        _state.update {
            it.copy(
                currentQuestionStem = value,
                manualQuestionSetupCompleted = false,
                preview = null,
                errorMessage = null,
                savedId = null
            )
        }
    }

    fun updateCurrentChoiceCount(value: Int) {
        val count = value.coerceIn(MIN_CHOICE_COUNT, MAX_CHOICE_COUNT)
        _state.update { state ->
            val adjustedChoices = state.currentChoices
                .take(count)
                .let { choices -> choices + List(count - choices.size) { "" } }
            state.copy(
                currentChoiceCount = count,
                currentChoices = adjustedChoices,
                currentAnswerIndex = state.currentAnswerIndex.coerceAtMost(count - 1),
                manualQuestionSetupCompleted = false,
                preview = null,
                errorMessage = null,
                savedId = null
            )
        }
    }

    fun updateCurrentChoice(index: Int, value: String) {
        _state.update { state ->
            if (index !in state.currentChoices.indices) return@update state
            state.copy(
                currentChoices = state.currentChoices.mapIndexed { choiceIndex, choice ->
                    if (choiceIndex == index) value else choice
                },
                manualQuestionSetupCompleted = false,
                preview = null,
                errorMessage = null,
                savedId = null
            )
        }
    }

    fun updateCurrentAnswerIndex(index: Int) {
        _state.update { state ->
            state.copy(
                currentAnswerIndex = index.coerceIn(0, state.currentChoiceCount - 1),
                manualQuestionSetupCompleted = false,
                preview = null,
                errorMessage = null,
                savedId = null
            )
        }
    }

    fun updateCurrentExplanation(value: String) {
        _state.update {
            it.copy(
                currentExplanation = value,
                manualQuestionSetupCompleted = false,
                preview = null,
                errorMessage = null,
                savedId = null
            )
        }
    }

    fun addManualQuestion() {
        val current = _state.value
        val question = current.buildCurrentQuestionOrNull() ?: return
        _state.update {
            it.copy(
                manualQuestions = it.manualQuestions + question,
                currentQuestionStem = "",
                currentChoiceCount = MAX_CHOICE_COUNT,
                currentChoices = List(MAX_CHOICE_COUNT) { "" },
                currentAnswerIndex = 0,
                currentExplanation = "",
                manualQuestionSetupCompleted = false,
                preview = null,
                errorMessage = null,
                savedId = null
            )
        }
    }

    fun completeManualQuestionSetup() {
        val state = _state.value
        val questions = when {
            state.hasCurrentQuestionInput() -> {
                val current = state.buildCurrentQuestionOrNull() ?: return
                state.manualQuestions + current
            }
            else -> state.manualQuestions
        }
        if (questions.isEmpty()) {
            _state.update { it.copy(errorMessage = "設題を1題以上入力してください") }
            return
        }
        val body = state.manualBody.trim()
        if (body.isBlank()) {
            _state.update { it.copy(errorMessage = "本文を入力してください") }
            return
        }
        _state.update {
            it.copy(
                manualQuestions = questions,
                manualQuestionSetupCompleted = true,
                currentQuestionStem = "",
                currentChoiceCount = MAX_CHOICE_COUNT,
                currentChoices = List(MAX_CHOICE_COUNT) { "" },
                currentAnswerIndex = 0,
                currentExplanation = "",
                preview = questions.toPassageSet(state),
                errorMessage = null,
                savedId = null
            )
        }
    }

    fun preview() {
        val rawText = _state.value.rawText
        runCatching { parser.parse(rawText) }
            .onSuccess { parsed ->
                _state.update { it.copy(preview = parsed, errorMessage = null, savedId = null) }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        preview = null,
                        errorMessage = error.message ?: "長文問題の読み取りに失敗しました",
                        savedId = null
                    )
                }
            }
    }

    fun save() {
        val preview = _state.value.preview ?: run {
            preview()
            _state.value.preview
        } ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null, savedId = null) }
            runCatching { repository.save(preview) }
                .onSuccess { id ->
                    _state.update { it.copy(isSaving = false, savedId = id) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "長文問題の保存に失敗しました"
                        )
                    }
                }
        }
    }

    fun consumeSavedId() {
        _state.update { it.copy(savedId = null) }
    }

    private fun CustomPassageRegistrationUiState.hasCurrentQuestionInput(): Boolean =
        currentQuestionStem.isNotBlank() || currentChoices.any { it.isNotBlank() } || currentExplanation.isNotBlank()

    private fun CustomPassageRegistrationUiState.buildCurrentQuestionOrNull(): ManualPassageQuestion? {
        val stem = currentQuestionStem.trim()
        if (stem.isBlank()) {
            _state.update { it.copy(errorMessage = "設題を入力してください") }
            return null
        }
        val choices = currentChoices.take(currentChoiceCount).map { it.trim() }
        if (choices.any { it.isBlank() }) {
            _state.update { it.copy(errorMessage = "選択肢をすべて入力してください") }
            return null
        }
        val number = "Q${manualQuestions.size + 1}"
        return ManualPassageQuestion(
            number = number,
            stem = stem,
            options = choices,
            answerIndex = currentAnswerIndex.coerceIn(0, choices.lastIndex),
            explanation = currentExplanation.trim().ifBlank { null }
        )
    }

    private fun List<ManualPassageQuestion>.toPassageSet(state: CustomPassageRegistrationUiState): PassageSet =
        PassageSet(
            id = "manual-custom-import",
            instruction = "Read the passage and choose the best answer to each question.",
            documents = listOf(
                PassageDocument(
                    kind = state.manualDocumentType.toDocumentKind(),
                    title = state.manualTitle.trim().ifBlank { "長文問題" },
                    body = state.manualBody.trim()
                )
            ),
            questions = map { question ->
                PassageQuestion(
                    number = question.number,
                    stem = question.stem,
                    options = question.options,
                    answerIndex = question.answerIndex,
                    explanation = question.explanation
                )
            },
            timeLimitSec = state.manualTimeLimitSec.toIntOrNull()
        )

    private fun String.toDocumentKind(): DocumentKind = when (lowercase()) {
        "email" -> DocumentKind.EMAIL
        "notice" -> DocumentKind.NOTICE
        else -> DocumentKind.ARTICLE
    }

    private companion object {
        private const val MIN_CHOICE_COUNT = 2
        private const val MAX_CHOICE_COUNT = 4
    }
}
