package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.LessonRepository
import com.example.vocabapp.domain.model.HomeSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lessonRepositoryProvider: Provider<LessonRepository>
) : ViewModel() {
    private val _summary = MutableStateFlow(HomeSummary())
    val summary: StateFlow<HomeSummary> = _summary.asStateFlow()

    init {
        viewModelScope.launch {
            val repository = withContext(Dispatchers.IO) { lessonRepositoryProvider.get() }
            repository.observeHomeSummary()
                .collect { _summary.value = it }
        }
    }
}
