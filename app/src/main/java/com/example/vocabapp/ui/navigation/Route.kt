package com.example.vocabapp.ui.navigation

internal sealed class Route(val path: String) {
    object Home : Route("home")
    object Lessons : Route("lessons")
    object IdiomLessons : Route("idiom-lessons")
    object Review : Route("review")
    object StudyLog : Route("study-log")
    object Settings : Route("settings")
    object AddWord : Route("add-word")
    object WordImport : Route("word-import?defaultType=word")
    object IdiomImport : Route("word-import?defaultType=phrase")
    object CustomWordList : Route("custom-word-list")
    object AddIdiom : Route("add-idiom")
    object CustomIdiomList : Route("custom-idiom-list")
    object RandomCustomMenu : Route("random-custom-menu")
    object SentenceMenu : Route("sentence-menu")
    object AddSentence : Route("add-sentence")
    object SentenceImport : Route("sentence-import")
    object CustomSentenceList : Route("custom-sentence-list")
    data class SentenceTrainingBlock(val blockNumber: String) : Route("sentence-block/$blockNumber") {
        companion object {
            const val PATTERN = "sentence-block/{blockNumber}"
        }
    }

    data class SentenceQuiz(val setNumber: String) : Route("sentence-quiz/$setNumber") {
        companion object {
            const val PATTERN = "sentence-quiz/{setNumber}"
        }
    }

    data class Training(val lessonId: String) : Route("training/$lessonId") {
        companion object {
            const val PATTERN = "training/{lessonId}"
        }
    }

    data class Quiz(val trainingId: String?, val isReview: Boolean) :
        Route("quiz?trainingId=${trainingId ?: 0}&isReview=$isReview") {
        companion object {
            const val PATTERN = "quiz?trainingId={trainingId}&isReview={isReview}"
        }
    }

    data class Result(val attemptId: String) : Route("result/$attemptId") {
        companion object {
            const val PATTERN = "result/{attemptId}"
        }
    }

    data class WordDetail(val wordId: String) : Route("word/$wordId") {
        companion object {
            const val PATTERN = "word/{wordId}"
        }
    }

    data class CustomTraining(val type: String) : Route("custom-training/$type") {
        companion object {
            const val PATTERN = "custom-training/{type}"
        }
    }

    data class CustomTrainingBlock(val type: String, val blockNumber: String) :
        Route("custom-training/$type/block/$blockNumber") {
        companion object {
            const val PATTERN = "custom-training/{type}/block/{blockNumber}"
        }
    }

    data class CustomTrainingQuiz(val type: String, val setNumber: String) :
        Route("custom-training-quiz/$type/$setNumber") {
        companion object {
            const val PATTERN = "custom-training-quiz/{type}/{setNumber}"
        }
    }

    data class RandomCustomQuiz(val type: String) : Route("random-custom-quiz/$type") {
        companion object {
            const val PATTERN = "random-custom-quiz/{type}"
        }
    }

    data class Flashcard(val trainingId: String) : Route("flashcard/$trainingId") {
        companion object {
            const val PATTERN = "flashcard/{trainingId}"
        }
    }

    companion object {
        const val WORD_IMPORT_PATTERN = "word-import?defaultType={defaultType}"
        fun flashcard(trainingId: Int) = Flashcard(trainingId.toString()).path
        fun customTraining(type: String) = CustomTraining(type).path
        fun customTrainingBlock(type: String, blockNumber: Int) =
            CustomTrainingBlock(type, blockNumber.toString()).path
        fun customTrainingQuiz(type: String, setNumber: Int) =
            CustomTrainingQuiz(type, setNumber.toString()).path
        fun randomCustomQuiz(type: String) = RandomCustomQuiz(type).path
        fun sentenceTrainingBlock(blockNumber: Int) = SentenceTrainingBlock(blockNumber.toString()).path
        fun sentenceQuiz(setNumber: Int) = SentenceQuiz(setNumber.toString()).path
        fun training(lessonId: Int) = Training(lessonId.toString()).path
        fun quiz(trainingId: Int? = null, isReview: Boolean = false) =
            Quiz(trainingId?.toString(), isReview).path
        fun result(attemptId: Long) = Result(attemptId.toString()).path
        fun word(wordId: Int) = WordDetail(wordId.toString()).path
    }
}
