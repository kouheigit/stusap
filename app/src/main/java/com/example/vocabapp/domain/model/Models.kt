package com.example.vocabapp.domain.model

data class Lesson(
    val id: Int,
    val scoreTarget: Int,
    val title: String,
    val wordStartNumber: Int,
    val wordEndNumber: Int,
    val progressRate: Float,
    val status: LessonStatus,
    val masteredTrainings: Int,
    val lastStudiedAt: Long?
)

enum class LessonStatus { NotStarted, InProgress, Complete, Master }

data class Training(
    val id: Int,
    val lessonId: Int,
    val title: String,
    val wordStartNumber: Int,
    val wordEndNumber: Int,
    val studyCount: Int,
    val bestAccuracy: Float,
    val bestStarCount: Int,
    val lastStudiedAt: Long?,
    val lastAccuracy: Float = 0f,
    val firstWordId: Int = wordStartNumber
)

data class Word(
    val id: Int,
    val trainingId: Int,
    val english: String,
    val meaning: String,
    val phonetic: String,
    val partOfSpeech: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val audioUrl: String?,
    val exampleAudioUrl: String?,
    val displayOrder: Int
)

data class WordChoice(
    val id: Int,
    val wordId: Int,
    val choiceText: String,
    val isCorrect: Boolean,
    val displayOrder: Int
)

data class WordRelation(
    val relatedWord: String,
    val relatedMeaning: String
)

data class QuizQuestion(
    val word: Word,
    val choices: List<WordChoice>
)

data class AnswerRecord(
    val wordId: Int,
    val selectedChoiceId: Int?,
    val isCorrect: Boolean,
    val answeredAt: Long,
    val responseMillis: Int,
    val selectedUnknown: Boolean
)

data class QuizState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedChoiceId: Int? = null,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean? = null,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val remainingMillis: Long = 30000L,
    val startedAt: Long = 0L,
    val finishedAttemptId: Long? = null,
    val isReview: Boolean = false,
    val trainingId: Int? = null,
    val lessonId: Int? = null
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    val isFinished: Boolean
        get() = finishedAttemptId != null
}

data class QuizResult(
    val attemptId: Long,
    val trainingId: Int?,
    val isReview: Boolean,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val accuracy: Float,
    val studySeconds: Int,
    val starCount: Int,
    val wrongWords: List<Word> = emptyList()
)

data class HomeSummary(
    val totalStudySeconds: Int = 0,
    val weekStudySeconds: Int = 0,
    val masteredLessons: Int = 0,
    val totalLessons: Int = 0,
    val reviewCount: Int = 0,
    val streakDays: Int = 0,
    val idiomMasteredLessons: Int = 0,
    val idiomTotalLessons: Int = 0,
    val sentenceCount: Int = 0
)

data class ImportedWord(
    val english: String,
    val meaning: String,
    val exampleSentence: String = "",
    val exampleTranslation: String = "",
    val type: String
)

data class ImportErrorRow(
    val rowNumber: Int,
    val reason: String,
    val rawValues: List<String>
)

data class WordImportPreview(
    val totalRows: Int = 0,
    val newWords: List<ImportedWord> = emptyList(),
    val duplicateWords: List<ImportedWord> = emptyList(),
    val errors: List<ImportErrorRow> = emptyList()
) {
    val newCount: Int get() = newWords.size
    val duplicateCount: Int get() = duplicateWords.size
    val errorCount: Int get() = errors.size
}

data class WordImportResult(
    val totalRows: Int = 0,
    val insertedCount: Int = 0,
    val insertedIdiomCount: Int = 0,
    val duplicateCount: Int = 0,
    val errorCount: Int = 0
)

data class SentenceQuestion(
    val id: Int,
    val template: String,
    val answers: List<String>,
    val shuffledChoices: List<String>,
    val meaning: String
)

data class SentenceQuizState(
    val questions: List<SentenceQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedWords: List<String> = emptyList(),
    val selectedChoiceIndices: List<Int> = emptyList(),
    val isAnswered: Boolean = false,
    val isCorrect: Boolean? = null,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val startedAt: Long = 0L,
    val result: SentenceQuizResult? = null
) {
    val currentQuestion: SentenceQuestion? get() = questions.getOrNull(currentIndex)
    val isFinished: Boolean get() = result != null
}

data class SentenceQuizResult(
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val accuracy: Float,
    val studySeconds: Int,
    val starCount: Int
)
