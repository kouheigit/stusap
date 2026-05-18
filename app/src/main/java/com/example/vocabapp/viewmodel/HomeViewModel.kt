package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.LessonRepository
import com.example.vocabapp.domain.model.HomeSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    lessonRepository: LessonRepository
) : ViewModel() {
    val summary: StateFlow<HomeSummary> = lessonRepository.observeHomeSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeSummary())
}
