package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.local.entity.CustomPassageSummary
import com.example.vocabapp.data.repository.CustomPassageRepository
import com.example.vocabapp.domain.model.PassageSet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomPassageListUiState(
    val selectedSet: PassageSet? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CustomPassageListViewModel @Inject constructor(
    private val repository: CustomPassageRepository
) : ViewModel() {
    val summaries: StateFlow<List<CustomPassageSummary>> = repository.observeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(CustomPassageListUiState())
    val state: StateFlow<CustomPassageListUiState> = _state.asStateFlow()

    fun openSet(id: Int) {
        viewModelScope.launch {
            runCatching { repository.getSet(id) }
                .onSuccess { set ->
                    _state.update {
                        if (set == null) {
                            it.copy(errorMessage = "長文問題が見つかりません", selectedSet = null)
                        } else {
                            it.copy(selectedSet = set, errorMessage = null)
                        }
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            selectedSet = null,
                            errorMessage = error.message ?: "長文問題の読み込みに失敗しました"
                        )
                    }
                }
        }
    }

    fun consumeSelectedSet() {
        _state.update { it.copy(selectedSet = null) }
    }
}
