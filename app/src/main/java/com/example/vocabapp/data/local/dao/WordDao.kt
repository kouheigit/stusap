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
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(items: List<WordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoices(items: List<WordChoiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(items: List<WordRelationEntity>)
    @Query("SELECT * FROM words WHERE trainingId = :trainingId ORDER BY displayOrder")
    suspend fun getWordsByTraining(trainingId: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWord(wordId: Int): WordEntity?

    @Query("SELECT * FROM words WHERE id = :wordId")
    fun observeWord(wordId: Int): Flow<WordEntity?>

    @Query("UPDATE words SET isFavorite = :isFavorite WHERE id = :wordId")
    suspend fun setWordFavorite(wordId: Int, isFavorite: Boolean)

    @Query("UPDATE words SET isLearned = :isLearned WHERE id = :wordId")
    suspend fun setWordLearned(wordId: Int, isLearned: Boolean)

    @Query("SELECT * FROM word_choices WHERE wordId = :wordId ORDER BY displayOrder")
    suspend fun getChoices(wordId: Int): List<WordChoiceEntity>

    @Query("SELECT * FROM word_relations WHERE wordId = :wordId")
    suspend fun getRelations(wordId: Int): List<WordRelationEntity>

    @Query("SELECT * FROM word_relations WHERE wordId = :wordId")
    fun observeRelations(wordId: Int): Flow<List<WordRelationEntity>>
    @Query("SELECT lower(trim(english)) FROM words")
    suspend fun getNormalizedSeedEnglish(): List<String>
}
