package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.LessonRepository
import com.example.vocabapp.data.repository.SeedRepository
import com.example.vocabapp.data.repository.SettingsRepository
import com.example.vocabapp.domain.model.HomeSummary
import com.example.vocabapp.domain.model.SentenceQuizState
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.QuizState
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.domain.model.WordImportPreview
import com.example.vocabapp.domain.model.WordImportResult
import com.example.vocabapp.domain.model.WordRelation
import com.example.vocabapp.domain.usecase.quiz.FinishQuizUseCase
import com.example.vocabapp.domain.usecase.lesson.GetIdiomLessonsUseCase
import com.example.vocabapp.domain.usecase.lesson.GetLessonsUseCase
import com.example.vocabapp.domain.usecase.review.GetReviewWordsUseCase
import com.example.vocabapp.domain.usecase.lesson.GetTrainingsUseCase
import com.example.vocabapp.domain.usecase.quiz.StartQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val seedRepository: SeedRepository,
    private val settingsRepository: SettingsRepository,
    private val customContentRepository: CustomContentRepository
) : ViewModel() {
    val summary: StateFlow<HomeSummary> = lessonRepository.observeHomeSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeSummary())

    init {
        viewModelScope.launch {
            seedRepository.seedIfNeeded()
            seedRepository.seedIdiomsIfNeeded()
        }
    }

    fun resetProgress() {
        viewModelScope.launch { settingsRepository.resetLearningData() }
    }

    fun deleteAllCustomWordsAndIdioms() {
        viewModelScope.launch {
            customContentRepository.deleteAllCustomWordsAndIdioms()
        }
    }

    fun deleteAllCustomSentences() {
        viewModelScope.launch { customContentRepository.deleteAllCustomSentences() }
    }
}
