package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.Training
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
/**
 * カスタム単語・熟語を10件単位のトレーニングへ分割して表示するViewModel。
 */
class CustomTrainingListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: CustomContentRepository
) : ViewModel() {
    val contentType: String = checkNotNull(savedStateHandle["type"])
    private val customContentType = ContentType.fromRouteValue(contentType)
    val trainings: StateFlow<List<Training>> = repository.observeCustomTrainings(contentType)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

