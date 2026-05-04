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
    val streakDays: Int = 0
)
