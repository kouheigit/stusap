package com.example.vocabapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.data.local.entity.LessonEntity
import com.example.vocabapp.data.local.entity.QuizAttemptAnswerEntity
import com.example.vocabapp.data.local.entity.QuizAttemptEntity
import com.example.vocabapp.data.local.entity.ReviewWordEntity
import com.example.vocabapp.data.local.entity.StudyLogEntity
import com.example.vocabapp.data.local.entity.TrainingEntity
import com.example.vocabapp.data.local.entity.UserProgressEntity
import com.example.vocabapp.data.local.entity.WordChoiceEntity
import com.example.vocabapp.data.local.entity.WordEntity
import com.example.vocabapp.data.local.entity.TrainingFirstWordRow
import com.example.vocabapp.data.local.entity.WordRelationEntity
import com.example.vocabapp.data.repository.IDIOM_LESSON_START_ID
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao :
    LessonDao,
    TrainingDao,
    WordDao,
    QuizDao,
    ReviewDao,
    StudyLogDao,
    UserProgressDao,
    CustomContentDao,
    AppSettingsDao {

    @Transaction
    suspend fun seedIfNeeded(
        lessons: List<LessonEntity>,
        trainings: List<TrainingEntity>,
        words: List<WordEntity>,
        choices: List<WordChoiceEntity>,
        relations: List<WordRelationEntity>
    ) {
        if (lessonCount() == 0) {
            insertLessons(lessons)
            insertTrainings(trainings)
            insertWords(words)
            insertChoices(choices)
            insertRelations(relations)
        }
    }

    @Transaction
    suspend fun seedIdiomsIfNeeded(
        lessons: List<LessonEntity>,
        trainings: List<TrainingEntity>,
        words: List<WordEntity>,
        choices: List<WordChoiceEntity>,
        relations: List<WordRelationEntity>
    ) {
        if (getLesson(IDIOM_LESSON_START_ID) == null) {
            insertLessons(lessons)
            insertTrainings(trainings)
            insertWords(words)
            insertChoices(choices)
            insertRelations(relations)
        }
    }
    @Transaction
    suspend fun deleteAllCustomWordsAndIdioms() {
        deleteAllCustomWords()
        deleteAllCustomIdioms()
    }
    @Transaction
    suspend fun resetLearningData() {
        deleteAttemptAnswers()
        deleteAttempts()
        deleteStudyLogs()
        deleteProgress()
        deleteReviews()
        deleteAllCustomWords()
        deleteAllCustomIdioms()
        deleteAllCustomSentences()
    }
}
