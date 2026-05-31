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
interface CustomContentDao {
    @Insert
    suspend fun insertCustomWord(item: CustomWordEntity): Long

    @Insert
    suspend fun insertCustomWords(items: List<CustomWordEntity>): List<Long>

    @Query("SELECT lower(trim(english)) FROM custom_words")
    suspend fun getNormalizedCustomEnglish(): List<String>

    @Query("SELECT * FROM custom_words ORDER BY addedAt DESC")
    fun observeCustomWords(): Flow<List<CustomWordEntity>>

    @Query("UPDATE custom_words SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setCustomWordFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE custom_words SET isLearned = :isLearned WHERE id = :id")
    suspend fun setCustomWordLearned(id: Int, isLearned: Boolean)

    @Query("SELECT * FROM custom_words ORDER BY RANDOM()")
    suspend fun getAllCustomWords(): List<CustomWordEntity>

    @Query("SELECT * FROM custom_words ORDER BY addedAt ASC, id ASC")
    fun observeCustomWordsInStudyOrder(): Flow<List<CustomWordEntity>>

    @Query("SELECT * FROM custom_words ORDER BY addedAt ASC, id ASC")
    suspend fun getCustomWordsInStudyOrder(): List<CustomWordEntity>

    @Query("SELECT * FROM custom_words WHERE wordType != 'phrase' ORDER BY addedAt ASC, id ASC LIMIT :limit OFFSET :offset")
    suspend fun getCustomWordsForStudyRange(limit: Int, offset: Int): List<CustomWordEntity>

    @Query("SELECT COUNT(*) FROM custom_words")
    suspend fun customWordCount(): Int

    @Query("DELETE FROM custom_words WHERE id = :id")
    suspend fun deleteCustomWord(id: Int)

    @Query("DELETE FROM custom_words")
    suspend fun deleteAllCustomWords()

    @Insert
    suspend fun insertCustomIdiom(item: CustomIdiomEntity): Long

    @Insert
    suspend fun insertCustomIdioms(items: List<CustomIdiomEntity>): List<Long>

    @Query("SELECT lower(trim(english)) FROM custom_idioms")
    suspend fun getNormalizedCustomIdiomEnglish(): List<String>

    @Query("SELECT * FROM custom_idioms ORDER BY addedAt DESC")
    fun observeCustomIdioms(): Flow<List<CustomIdiomEntity>>

    @Query("SELECT * FROM custom_idioms ORDER BY RANDOM()")
    suspend fun getAllCustomIdioms(): List<CustomIdiomEntity>

    @Query("SELECT * FROM custom_idioms ORDER BY addedAt ASC, id ASC")
    fun observeCustomIdiomsInStudyOrder(): Flow<List<CustomIdiomEntity>>

    @Query("SELECT * FROM custom_idioms ORDER BY addedAt ASC, id ASC")
    suspend fun getCustomIdiomsInStudyOrder(): List<CustomIdiomEntity>

    @Query("SELECT * FROM custom_idioms ORDER BY addedAt ASC, id ASC LIMIT :limit OFFSET :offset")
    suspend fun getCustomIdiomsForStudyRange(limit: Int, offset: Int): List<CustomIdiomEntity>

    @Query("SELECT COUNT(*) FROM custom_idioms")
    suspend fun customIdiomCount(): Int

    @Query("DELETE FROM custom_idioms WHERE id = :id")
    suspend fun deleteCustomIdiom(id: Int)

    @Query("DELETE FROM custom_idioms")
    suspend fun deleteAllCustomIdioms()

    @Insert
    suspend fun insertCustomSentence(item: CustomSentenceEntity): Long

    @Insert
    suspend fun insertCustomSentences(items: List<CustomSentenceEntity>): List<Long>

    @Query("SELECT lower(trim(sentence)) FROM custom_sentences")
    suspend fun getNormalizedCustomSentences(): List<String>

    @Query("SELECT * FROM custom_sentences ORDER BY addedAt DESC")
    fun observeCustomSentences(): Flow<List<CustomSentenceEntity>>

    @Query("SELECT * FROM custom_sentences ORDER BY addedAt ASC, id ASC")
    suspend fun getAllCustomSentences(): List<CustomSentenceEntity>

    @Query("SELECT COUNT(*) FROM custom_sentences")
    suspend fun customSentenceCount(): Int

    @Query("DELETE FROM custom_sentences WHERE id = :id")
    suspend fun deleteCustomSentence(id: Int)

    @Query("DELETE FROM custom_sentences")
    suspend fun deleteAllCustomSentences()
}
