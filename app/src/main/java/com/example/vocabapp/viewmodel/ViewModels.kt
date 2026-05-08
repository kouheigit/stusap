package com.example.vocabapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.data.repository.VocabRepository
import com.example.vocabapp.domain.model.AnswerRecord
import com.example.vocabapp.domain.model.HomeSummary
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.QuizState
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.domain.model.WordImportPreview
import com.example.vocabapp.domain.model.WordImportResult
import com.example.vocabapp.domain.model.WordRelation
import com.example.vocabapp.domain.usecase.FinishQuizUseCase
import com.example.vocabapp.domain.usecase.GetIdiomLessonsUseCase
import com.example.vocabapp.domain.usecase.GetLessonsUseCase
import com.example.vocabapp.domain.usecase.GetReviewWordsUseCase
import com.example.vocabapp.domain.usecase.GetTrainingsUseCase
import com.example.vocabapp.domain.usecase.StartQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: VocabRepository
) : ViewModel() {
    val summary: StateFlow<HomeSummary> = repository.observeHomeSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeSummary())

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
            repository.seedIdiomsIfNeeded()
        }
    }

    fun resetProgress() {
        viewModelScope.launch { repository.resetLearningData() }
    }

    fun deleteAllCustomWords() {
        viewModelScope.launch { repository.deleteAllCustomWords() }
    }
}

@HiltViewModel
class LessonListViewModel @Inject constructor(
    getLessonsUseCase: GetLessonsUseCase
) : ViewModel() {
    val lessons: StateFlow<List<Lesson>> = getLessonsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@HiltViewModel
class IdiomLessonListViewModel @Inject constructor(
    getIdiomLessonsUseCase: GetIdiomLessonsUseCase
) : ViewModel() {
    val lessons: StateFlow<List<Lesson>> = getIdiomLessonsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@HiltViewModel
class TrainingListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getTrainingsUseCase: GetTrainingsUseCase
) : ViewModel() {
    val lessonId: Int = checkNotNull(savedStateHandle["lessonId"])
    val trainings: StateFlow<List<Training>> = getTrainingsUseCase(lessonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startQuizUseCase: StartQuizUseCase,
    private val finishQuizUseCase: FinishQuizUseCase
) : ViewModel() {
    private val trainingId: Int? = savedStateHandle.get<Int>("trainingId")?.takeIf { it > 0 }
    private val isReview: Boolean = savedStateHandle["isReview"] ?: false
    private val _state = MutableStateFlow(QuizState(isReview = isReview, trainingId = trainingId))
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val answers = mutableListOf<AnswerRecord>()
    private var questionStartedAt = System.currentTimeMillis()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            if (isReview) {
                val questions = startQuizUseCase.review()
                _state.value = _state.value.copy(questions = questions, startedAt = startedAt)
            } else if (trainingId != null) {
                val (lessonId, questions) = startQuizUseCase.training(trainingId)
                _state.value = _state.value.copy(questions = questions, startedAt = startedAt, lessonId = lessonId)
            }
            questionStartedAt = System.currentTimeMillis()
            startTimer()
        }
    }

    fun submit(choiceId: Int?) {
        val current = _state.value
        if (current.isAnswered || current.isFinished) return
        val question = current.currentQuestion ?: return
        val correctChoice = question.choices.firstOrNull { it.isCorrect }
        val isCorrect = choiceId != null && correctChoice?.id == choiceId
        val now = System.currentTimeMillis()
        answers += AnswerRecord(
            wordId = question.word.id,
            selectedChoiceId = choiceId,
            isCorrect = isCorrect,
            answeredAt = now,
            responseMillis = (now - questionStartedAt).toInt(),
            selectedUnknown = choiceId == null
        )
        _state.value = current.copy(
            selectedChoiceId = choiceId,
            isAnswered = true,
            isCorrect = isCorrect,
            correctCount = current.correctCount + if (isCorrect) 1 else 0,
            wrongCount = current.wrongCount + if (isCorrect) 0 else 1
        )
        viewModelScope.launch {
            delay(900)
            nextOrFinish()
        }
    }

    private suspend fun nextOrFinish() {
        val current = _state.value
        if (current.currentIndex >= current.questions.lastIndex) {
            timerJob?.cancel()
            val attemptId = finishQuizUseCase(
                trainingId = current.trainingId,
                lessonId = current.lessonId,
                isReview = current.isReview,
                startedAt = current.startedAt,
                answers = answers.toList()
            )
            _state.value = current.copy(finishedAttemptId = attemptId)
        } else {
            questionStartedAt = System.currentTimeMillis()
            _state.value = current.copy(
                currentIndex = current.currentIndex + 1,
                selectedChoiceId = null,
                isAnswered = false,
                isCorrect = null,
                remainingMillis = 30000L
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value
                if (!current.isAnswered && !current.isFinished) {
                    val next = (current.remainingMillis - 1000L).coerceAtLeast(0L)
                    _state.value = current.copy(remainingMillis = next)
                    if (next == 0L) submit(null)
                }
            }
        }
    }
}

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VocabRepository
) : ViewModel() {
    private val attemptId: Long = checkNotNull(savedStateHandle["attemptId"])
    private val _result = MutableStateFlow<QuizResult?>(null)
    val result: StateFlow<QuizResult?> = _result.asStateFlow()
    private val _trainingLabel = MutableStateFlow<String?>(null)
    val trainingLabel: StateFlow<String?> = _trainingLabel.asStateFlow()

    init {
        viewModelScope.launch {
            val r = repository.getResult(attemptId)
            _result.value = r
            r?.trainingId?.let { tid ->
                _trainingLabel.value = repository.getTrainingRange(tid)
            }
        }
    }
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: VocabRepository,
    getReviewWordsUseCase: GetReviewWordsUseCase
) : ViewModel() {
    val words: StateFlow<List<Word>> = getReviewWordsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(wordId: Int) {
        viewModelScope.launch { repository.removeReviewWord(wordId) }
    }
}

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VocabRepository
) : ViewModel() {
    private val wordId: Int = checkNotNull(savedStateHandle["wordId"])
    private val _word = MutableStateFlow<Word?>(null)
    val word: StateFlow<Word?> = _word.asStateFlow()
    private val _relations = MutableStateFlow<List<WordRelation>>(emptyList())
    val relations: StateFlow<List<WordRelation>> = _relations.asStateFlow()

    init {
        viewModelScope.launch {
            val detail = repository.getWordDetail(wordId)
            _word.value = detail.first
            _relations.value = detail.second
        }
    }

    fun addReview() {
        viewModelScope.launch { repository.addReviewWord(wordId) }
    }
}

@HiltViewModel
class StudyLogViewModel @Inject constructor(
    repository: VocabRepository
) : ViewModel() {
    val logs = repository.observeStudyLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@HiltViewModel
class AddWordViewModel @Inject constructor(
    private val repository: VocabRepository
) : ViewModel() {
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun save(english: String, meaning: String) {
        if (english.isBlank() || meaning.isBlank()) return
        viewModelScope.launch {
            val trimmed = english.trim()
            if (trimmed.contains(Regex("\\s"))) {
                repository.addCustomIdiom(trimmed, meaning.trim())
            } else {
                repository.addCustomWord(trimmed, meaning.trim())
            }
            _saved.value = true
        }
    }

    fun resetSaved() { _saved.value = false }
}

@HiltViewModel
class WordImportViewModel @Inject constructor(
    private val repository: VocabRepository
) : ViewModel() {
    private val _preview = MutableStateFlow<WordImportPreview?>(null)
    val preview: StateFlow<WordImportPreview?> = _preview.asStateFlow()
    private val _result = MutableStateFlow<WordImportResult?>(null)
    val result: StateFlow<WordImportResult?> = _result.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadCsv(csvText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            _result.value = null
            runCatching {
                repository.previewCustomWordCsv(csvText)
            }.onSuccess { loadedPreview ->
                _preview.value = loadedPreview
            }.onFailure { error ->
                _preview.value = null
                _message.value = error.message?.let { "${error.javaClass.simpleName}: $it" }
                    ?: "CSVの読み込みに失敗しました"
            }
            _isLoading.value = false
        }
    }

    fun registerPreview() {
        val currentPreview = _preview.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            runCatching {
                repository.importCustomWords(currentPreview)
            }.onSuccess { importResult ->
                _result.value = importResult
            }.onFailure { error ->
                _message.value = error.message?.let { "${error.javaClass.simpleName}: $it" }
                    ?: "登録に失敗しました"
            }
            _isLoading.value = false
        }
    }

    fun showMessage(message: String) {
        _message.value = message
        _isLoading.value = false
    }

    fun showLoading() {
        _isLoading.value = true
        _message.value = null
        _preview.value = null
        _result.value = null
    }
}

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VocabRepository
) : ViewModel() {
    val trainingId: Int = checkNotNull(savedStateHandle["trainingId"])
    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()
    private val _index = MutableStateFlow(0)
    val index: StateFlow<Int> = _index.asStateFlow()
    private val _revealed = MutableStateFlow(false)
    val revealed: StateFlow<Boolean> = _revealed.asStateFlow()

    init { viewModelScope.launch { _words.value = repository.getWordsForTraining(trainingId) } }

    fun next() {
        if (_index.value < _words.value.lastIndex) {
            _index.value++
            _revealed.value = false
        }
    }

    fun prev() {
        if (_index.value > 0) {
            _index.value--
            _revealed.value = false
        }
    }

    fun toggleReveal() { _revealed.value = !_revealed.value }
}

@HiltViewModel
class CustomWordListViewModel @Inject constructor(
    private val repository: VocabRepository
) : ViewModel() {
    val words: StateFlow<List<CustomWordEntity>> = repository.observeCustomWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Int) {
        viewModelScope.launch { repository.deleteCustomWord(id) }
    }
}

@HiltViewModel
class CustomIdiomListViewModel @Inject constructor(
    private val repository: VocabRepository
) : ViewModel() {
    val idioms: StateFlow<List<CustomIdiomEntity>> = repository.observeCustomIdioms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Int) {
        viewModelScope.launch { repository.deleteCustomIdiom(id) }
    }
}

@HiltViewModel
class CustomTrainingListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: VocabRepository
) : ViewModel() {
    val type: String = checkNotNull(savedStateHandle["type"])
    val trainings: StateFlow<List<Training>> = repository.observeCustomTrainings(type)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@HiltViewModel
class AddIdiomViewModel @Inject constructor(
    private val repository: VocabRepository
) : ViewModel() {
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun save(english: String, meaning: String) {
        if (english.isBlank() || meaning.isBlank()) return
        viewModelScope.launch {
            repository.addCustomIdiom(english.trim(), meaning.trim())
            _saved.value = true
        }
    }

    fun resetSaved() { _saved.value = false }
}

@HiltViewModel
class CustomTrainingQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VocabRepository
) : ViewModel() {
    val type: String = checkNotNull(savedStateHandle["type"])
    val setNumber: Int = checkNotNull(savedStateHandle["setNumber"])
    private val _state = MutableStateFlow(
        QuizState(trainingId = customTrainingId(type, setNumber), lessonId = customLessonId(type))
    )
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val _result = MutableStateFlow<QuizResult?>(null)
    val result: StateFlow<QuizResult?> = _result.asStateFlow()
    private val answers = mutableListOf<AnswerRecord>()
    private var questionStartedAt = System.currentTimeMillis()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val questions = repository.buildCustomTrainingQuiz(type, setNumber)
            _state.value = _state.value.copy(questions = questions, startedAt = startedAt)
            questionStartedAt = System.currentTimeMillis()
            if (questions.isNotEmpty()) startTimer()
        }
    }

    fun submit(choiceId: Int?) {
        val current = _state.value
        if (current.isAnswered || current.isFinished) return
        val question = current.currentQuestion ?: return
        val correctChoice = question.choices.firstOrNull { it.isCorrect }
        val isCorrect = choiceId != null && correctChoice?.id == choiceId
        val now = System.currentTimeMillis()
        answers += AnswerRecord(
            wordId = question.word.id,
            selectedChoiceId = choiceId,
            isCorrect = isCorrect,
            answeredAt = now,
            responseMillis = (now - questionStartedAt).toInt(),
            selectedUnknown = choiceId == null
        )
        _state.value = current.copy(
            selectedChoiceId = choiceId,
            isAnswered = true,
            isCorrect = isCorrect,
            correctCount = current.correctCount + if (isCorrect) 1 else 0,
            wrongCount = current.wrongCount + if (isCorrect) 0 else 1
        )
        viewModelScope.launch {
            delay(900)
            nextOrFinish()
        }
    }

    private suspend fun nextOrFinish() {
        val current = _state.value
        if (current.currentIndex >= current.questions.lastIndex) {
            timerJob?.cancel()
            val quizResult = repository.finishCustomQuiz(
                type = type,
                setNumber = setNumber,
                startedAt = current.startedAt,
                answers = answers.toList(),
                questions = current.questions
            )
            _result.value = quizResult
            _state.value = current.copy(finishedAttemptId = quizResult.attemptId)
        } else {
            questionStartedAt = System.currentTimeMillis()
            _state.value = current.copy(
                currentIndex = current.currentIndex + 1,
                selectedChoiceId = null,
                isAnswered = false,
                isCorrect = null,
                remainingMillis = 30000L
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value
                if (!current.isAnswered && !current.isFinished) {
                    val next = (current.remainingMillis - 1000L).coerceAtLeast(0L)
                    _state.value = current.copy(remainingMillis = next)
                    if (next == 0L) submit(null)
                }
            }
        }
    }

    private fun customLessonId(type: String): Int =
        if (type == "idiom") -20_000 else -10_000

    private fun customTrainingId(type: String, setNumber: Int): Int =
        customLessonId(type) - setNumber
}

@HiltViewModel
class CustomIdiomQuizViewModel @Inject constructor(
    private val repository: VocabRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val answers = mutableListOf<AnswerRecord>()
    private var questionStartedAt = System.currentTimeMillis()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val questions = repository.buildCustomIdiomQuiz()
            _state.value = QuizState(questions = questions, startedAt = startedAt)
            questionStartedAt = System.currentTimeMillis()
            if (questions.isNotEmpty()) startTimer()
        }
    }

    fun submit(choiceId: Int?) {
        val current = _state.value
        if (current.isAnswered || current.isFinished) return
        val question = current.currentQuestion ?: return
        val correctChoice = question.choices.firstOrNull { it.isCorrect }
        val isCorrect = choiceId != null && correctChoice?.id == choiceId
        val now = System.currentTimeMillis()
        answers += AnswerRecord(question.word.id, choiceId, isCorrect, now,
            (now - questionStartedAt).toInt(), choiceId == null)
        _state.value = current.copy(selectedChoiceId = choiceId, isAnswered = true, isCorrect = isCorrect,
            correctCount = current.correctCount + if (isCorrect) 1 else 0,
            wrongCount = current.wrongCount + if (isCorrect) 0 else 1)
        viewModelScope.launch { delay(900); nextOrFinish() }
    }

    private fun nextOrFinish() {
        val current = _state.value
        timerJob?.cancel()
        if (current.currentIndex >= current.questions.lastIndex) {
            _state.value = current.copy(finishedAttemptId = -1L)
        } else {
            questionStartedAt = System.currentTimeMillis()
            _state.value = current.copy(currentIndex = current.currentIndex + 1,
                selectedChoiceId = null, isAnswered = false, isCorrect = null, remainingMillis = 30000L)
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value
                if (!current.isAnswered && !current.isFinished) {
                    val next = (current.remainingMillis - 1000L).coerceAtLeast(0L)
                    _state.value = current.copy(remainingMillis = next)
                    if (next == 0L) submit(null)
                }
            }
        }
    }
}

@HiltViewModel
class CustomWordQuizViewModel @Inject constructor(
    private val repository: VocabRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private val answers = mutableListOf<AnswerRecord>()
    private var questionStartedAt = System.currentTimeMillis()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val questions = repository.buildCustomWordQuiz()
            _state.value = QuizState(questions = questions, startedAt = startedAt)
            questionStartedAt = System.currentTimeMillis()
            if (questions.isNotEmpty()) startTimer()
        }
    }

    fun submit(choiceId: Int?) {
        val current = _state.value
        if (current.isAnswered || current.isFinished) return
        val question = current.currentQuestion ?: return
        val correctChoice = question.choices.firstOrNull { it.isCorrect }
        val isCorrect = choiceId != null && correctChoice?.id == choiceId
        val now = System.currentTimeMillis()
        answers += AnswerRecord(question.word.id, choiceId, isCorrect, now,
            (now - questionStartedAt).toInt(), choiceId == null)
        _state.value = current.copy(selectedChoiceId = choiceId, isAnswered = true, isCorrect = isCorrect,
            correctCount = current.correctCount + if (isCorrect) 1 else 0,
            wrongCount = current.wrongCount + if (isCorrect) 0 else 1)
        viewModelScope.launch { delay(900); nextOrFinish() }
    }

    private fun nextOrFinish() {
        val current = _state.value
        timerJob?.cancel()
        if (current.currentIndex >= current.questions.lastIndex) {
            _state.value = current.copy(finishedAttemptId = -1L)
        } else {
            questionStartedAt = System.currentTimeMillis()
            _state.value = current.copy(currentIndex = current.currentIndex + 1,
                selectedChoiceId = null, isAnswered = false, isCorrect = null, remainingMillis = 30000L)
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value
                if (!current.isAnswered && !current.isFinished) {
                    val next = (current.remainingMillis - 1000L).coerceAtLeast(0L)
                    _state.value = current.copy(remainingMillis = next)
                    if (next == 0L) submit(null)
                }
            }
        }
    }
}
