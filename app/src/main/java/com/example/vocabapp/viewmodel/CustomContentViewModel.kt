package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.data.repository.QuizRepository
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.QuizState
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * ユーザーが登録したカスタム英単語一覧を管理するViewModel。
 */
class CustomWordListViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    val words: StateFlow<List<CustomWordEntity>> = repository.observeCustomWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * 指定したカスタム英単語を削除する。
     *
     * @param id カスタム英単語ID
     */
    fun delete(id: Int) {
        viewModelScope.launch { repository.deleteCustomWord(id) }
    }

    /**
     * 指定したカスタム英単語のお気に入り状態を更新する。
     *
     * @param id カスタム英単語ID
     * @param isFavorite trueの場合はお気に入りにする
     */
    fun setFavorite(id: Int, isFavorite: Boolean) {
        viewModelScope.launch { repository.setCustomWordFavorite(id, isFavorite) }
    }

    /**
     * 指定したカスタム英単語の学習済み状態を更新する。
     *
     * @param id カスタム英単語ID
     * @param isLearned trueの場合は学習済みにする
     */
    fun setLearned(id: Int, isLearned: Boolean) {
        viewModelScope.launch { repository.setCustomWordLearned(id, isLearned) }
    }
}

@HiltViewModel
/**
 * カスタム英熟語一覧を管理するViewModel。
 */
class CustomIdiomListViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    val idioms: StateFlow<List<CustomIdiomEntity>> = repository.observeCustomIdioms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Int) {
        viewModelScope.launch { repository.deleteCustomIdiom(id) }
    }
}

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

@HiltViewModel
/**
 * カスタム英熟語登録フォームの保存状態を管理するViewModel。
 */
class AddIdiomViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun save(english: String, meaning: String) {
        if (english.isBlank() || meaning.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.addCustomIdiom(english.trim(), meaning.trim())
            }.onSuccess {
                _saved.value = true
            }.onFailureUnlessCancellation {}
        }
    }

    fun resetSaved() { _saved.value = false }
}

@HiltViewModel
/**
 * 10件単位のカスタムトレーニングクイズを管理するViewModel。
 */
class CustomTrainingQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CustomContentRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {
    val contentType: String = checkNotNull(savedStateHandle["type"])
    private val customContentType = ContentType.fromRouteValue(contentType)
    val setNumber: Int = checkNotNull(savedStateHandle["setNumber"])
    private val _state = MutableStateFlow(
        QuizState(
            trainingId = customTrainingId(customContentType, setNumber),
            lessonId = customLessonId(customContentType)
        )
    )
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val _loadState = MutableStateFlow<UiState<QuizState>>(UiState.Loading)
    val loadState: StateFlow<UiState<QuizState>> = _loadState.asStateFlow()
    private val _result = MutableStateFlow<QuizResult?>(null)
    val result: StateFlow<QuizResult?> = _result.asStateFlow()
    private val quizSession = QuizSession(viewModelScope, _state) { current, answers ->
        val quizResult = quizRepository.finishCustomQuiz(
            type = contentType,
            setNumber = setNumber,
            startedAt = current.startedAt,
            answers = answers,
            questions = current.questions
        )
        _result.value = quizResult
        _state.value = current.copy(finishedAttemptId = quizResult.attemptId)
    }

    init {
        viewModelScope.launch {
            _loadState.value = UiState.Loading
            runCatching {
                val startedAt = System.currentTimeMillis()
                val questions = repository.buildCustomTrainingQuiz(contentType, setNumber)
                _state.value = _state.value.copy(questions = questions, startedAt = startedAt)
                quizSession.resetQuestionTimer()
                if (questions.isNotEmpty()) quizSession.startTimer()
                _state.value
            }.onSuccess { quizState ->
                _loadState.value = UiState.Success(quizState)
            }.onFailureUnlessCancellation { throwable ->
                _loadState.value = UiState.Error("カスタムクイズの取得に失敗しました", throwable)
            }
        }
    }

    fun onScreenStarted() = quizSession.resumeTimerIfNeeded()

    fun onScreenStopped() = quizSession.pauseTimer()

    fun submit(choiceId: Int?) = quizSession.submit(choiceId)

    private fun customLessonId(contentType: ContentType): Int =
        if (contentType == ContentType.IDIOM) {
            CUSTOM_IDIOM_LESSON_ID
        } else {
            CUSTOM_WORD_LESSON_ID
        }

    private fun customTrainingId(contentType: ContentType, setNumber: Int): Int =
        customLessonId(contentType) - setNumber
}

@HiltViewModel
/**
 * カスタム単語・熟語からランダムに出題するクイズを管理するViewModel。
 */
class RandomCustomQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CustomContentRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {
    val contentType: String = checkNotNull(savedStateHandle["type"])
    private val customContentType = ContentType.fromRouteValue(contentType)
    private val _state = MutableStateFlow(
        QuizState(
            trainingId = randomTrainingId(customContentType),
            lessonId = customLessonId(customContentType)
        )
    )
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val _loadState = MutableStateFlow<UiState<QuizState>>(UiState.Loading)
    val loadState: StateFlow<UiState<QuizState>> = _loadState.asStateFlow()
    private val _result = MutableStateFlow<QuizResult?>(null)
    val result: StateFlow<QuizResult?> = _result.asStateFlow()
    private val quizSession = QuizSession(viewModelScope, _state) { current, answers ->
        val quizResult = quizRepository.finishRandomCustomQuiz(
            type = contentType,
            startedAt = current.startedAt,
            answers = answers,
            questions = current.questions
        )
        _result.value = quizResult
        _state.value = current.copy(finishedAttemptId = quizResult.attemptId)
    }

    init {
        viewModelScope.launch {
            _loadState.value = UiState.Loading
            runCatching {
                val startedAt = System.currentTimeMillis()
                val questions = repository.buildRandomCustomQuiz(contentType)
                _state.value = _state.value.copy(questions = questions, startedAt = startedAt)
                quizSession.resetQuestionTimer()
                if (questions.isNotEmpty()) quizSession.startTimer()
                _state.value
            }.onSuccess { quizState ->
                _loadState.value = UiState.Success(quizState)
            }.onFailureUnlessCancellation { throwable ->
                _loadState.value = UiState.Error("ランダムクイズの取得に失敗しました", throwable)
            }
        }
    }

    fun onScreenStarted() = quizSession.resumeTimerIfNeeded()

    fun onScreenStopped() = quizSession.pauseTimer()

    fun submit(choiceId: Int?) = quizSession.submit(choiceId)

    private fun customLessonId(contentType: ContentType): Int =
        if (contentType == ContentType.IDIOM) {
            CUSTOM_IDIOM_LESSON_ID
        } else {
            CUSTOM_WORD_LESSON_ID
        }

    private fun randomTrainingId(contentType: ContentType): Int =
        customLessonId(contentType) - RANDOM_TRAINING_OFFSET
}

@HiltViewModel
/**
 * カスタム英熟語全体から出題するクイズを管理するViewModel。
 */
class CustomIdiomQuizViewModel @Inject constructor(
    private val repository: CustomContentRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val quizSession = QuizSession(viewModelScope, _state) { current, answers ->
        val quizResult = quizRepository.finishRandomCustomQuiz(
            type = ContentType.IDIOM.routeValue,
            startedAt = current.startedAt,
            answers = answers,
            questions = current.questions
        )
        _state.value = current.copy(finishedAttemptId = quizResult.attemptId)
    }

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val questions = repository.buildCustomIdiomQuiz()
            _state.value = QuizState(questions = questions, startedAt = startedAt)
            quizSession.resetQuestionTimer()
            if (questions.isNotEmpty()) quizSession.startTimer()
        }
    }

    fun onScreenStarted() = quizSession.resumeTimerIfNeeded()

    fun onScreenStopped() = quizSession.pauseTimer()

    fun submit(choiceId: Int?) = quizSession.submit(choiceId)
}

@HiltViewModel
/**
 * カスタム英単語全体から出題するクイズを管理するViewModel。
 */
class CustomWordQuizViewModel @Inject constructor(
    private val repository: CustomContentRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val quizSession = QuizSession(viewModelScope, _state) { current, answers ->
        val quizResult = quizRepository.finishRandomCustomQuiz(
            type = ContentType.WORD.routeValue,
            startedAt = current.startedAt,
            answers = answers,
            questions = current.questions
        )
        _state.value = current.copy(finishedAttemptId = quizResult.attemptId)
    }

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val questions = repository.buildCustomWordQuiz()
            _state.value = QuizState(questions = questions, startedAt = startedAt)
            quizSession.resetQuestionTimer()
            if (questions.isNotEmpty()) quizSession.startTimer()
        }
    }

    fun onScreenStarted() = quizSession.resumeTimerIfNeeded()

    fun onScreenStopped() = quizSession.pauseTimer()

    fun submit(choiceId: Int?) = quizSession.submit(choiceId)
}

private const val CUSTOM_WORD_LESSON_ID = -10_000
private const val CUSTOM_IDIOM_LESSON_ID = -20_000
private const val RANDOM_TRAINING_OFFSET = 999
