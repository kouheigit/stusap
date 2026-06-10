package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.AppDatabase
import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.local.entity.CustomPassageQuestionEntity
import com.example.vocabapp.data.local.entity.CustomPassageSetEntity
import com.example.vocabapp.data.local.entity.CustomPassageSummary
import com.example.vocabapp.domain.model.DocumentKind
import com.example.vocabapp.domain.model.PassageDocument
import com.example.vocabapp.domain.model.PassageQuestion
import com.example.vocabapp.domain.model.PassageSet
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CustomPassageRepository @Inject constructor(
    database: AppDatabase,
    private val runtime: QuizRuntime
) {
    private val dao: AppDao = database.appDao()

    fun observeSummaries(): Flow<List<CustomPassageSummary>> = dao.observeCustomPassageSummaries()

    suspend fun save(set: PassageSet): Int {
        val document = set.documents.first()
        val setId = dao.insertCustomPassageSet(
            CustomPassageSetEntity(
                title = document.title?.takeIf { it.isNotBlank() } ?: "長文問題",
                documentKind = document.kind.name,
                instruction = set.instruction,
                body = document.body,
                timeLimitSec = set.timeLimitSec,
                addedAt = runtime.nowMillis()
            )
        ).toInt()
        dao.insertCustomPassageQuestions(
            set.questions.mapIndexed { index, question ->
                CustomPassageQuestionEntity(
                    setId = setId,
                    number = question.number,
                    stem = question.stem,
                    optionsText = question.options.joinToString(OPTION_SEPARATOR),
                    answerIndex = question.answerIndex,
                    explanation = question.explanation,
                    displayOrder = index
                )
            }
        )
        return setId
    }

    suspend fun getSet(id: Int): PassageSet? {
        val set = dao.getCustomPassageSet(id) ?: return null
        val questions = dao.getCustomPassageQuestions(id)
        return PassageSet(
            id = "custom-passage-$id",
            instruction = set.instruction,
            documents = listOf(
                PassageDocument(
                    kind = set.documentKind.toDocumentKind(),
                    title = set.title,
                    body = set.body
                )
            ),
            questions = questions.map { it.toDomain() },
            timeLimitSec = set.timeLimitSec
        )
    }

    suspend fun delete(id: Int) = dao.deleteCustomPassageSet(id)

    private fun CustomPassageQuestionEntity.toDomain(): PassageQuestion = PassageQuestion(
        number = number,
        stem = stem,
        options = optionsText.split(OPTION_SEPARATOR),
        answerIndex = answerIndex,
        explanation = explanation
    )

    private fun String.toDocumentKind(): DocumentKind = when (this) {
        DocumentKind.EMAIL.name -> DocumentKind.EMAIL
        DocumentKind.NOTICE.name -> DocumentKind.NOTICE
        else -> DocumentKind.ARTICLE
    }

    private companion object {
        private const val OPTION_SEPARATOR = "\n"
    }
}
