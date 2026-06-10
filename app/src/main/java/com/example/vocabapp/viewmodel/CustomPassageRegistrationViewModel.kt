package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.import.PassageTextImportParser
import com.example.vocabapp.data.repository.CustomPassageRepository
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
    val preview: PassageSet? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val savedId: Int? = null
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
}
