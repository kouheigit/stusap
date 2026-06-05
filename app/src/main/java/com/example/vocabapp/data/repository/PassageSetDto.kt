package com.example.vocabapp.data.repository

import com.example.vocabapp.domain.model.DocumentHeader
import com.example.vocabapp.domain.model.DocumentKind
import com.example.vocabapp.domain.model.PassageConstants
import com.example.vocabapp.domain.model.PassageDocument
import com.example.vocabapp.domain.model.PassageQuestion
import com.example.vocabapp.domain.model.PassageSet
import kotlinx.serialization.Serializable

/**
 * `fixtures/sets.json`（および将来の取り込みデータ）の JSON 形状に対応する DTO 群。
 *
 * ドメインモデルへは [toDomain] で変換し、その際に `SPEC.md` の不変条件を検証する。
 * JSON 解析の都合をドメインモデルへ漏らさないために層を分ける。
 */
@Serializable
data class PassageSetDto(
    val id: String,
    val instruction: String,
    val documents: List<DocumentDto>,
    val questions: List<QuestionDto>,
    val timeLimitSec: Int? = null
)

@Serializable
data class DocumentDto(
    val kind: String,
    val header: HeaderDto? = null,
    val title: String? = null,
    val body: String
)

@Serializable
data class HeaderDto(
    val to: String,
    val from: String,
    val date: String,
    val subject: String
)

@Serializable
data class QuestionDto(
    val number: String,
    val stem: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String? = null
)

/**
 * DTO をドメインの [PassageSet] に変換する。
 *
 * @throws IllegalArgumentException 文書・設問が空、選択肢数が 2..4 の範囲外、
 * `answerIndex` が選択肢の範囲外、未知の `kind` のいずれかに該当する場合。
 */
fun PassageSetDto.toDomain(): PassageSet {
    require(documents.isNotEmpty()) { "PassageSet '$id' must contain at least one document" }
    require(questions.isNotEmpty()) { "PassageSet '$id' must contain at least one question" }
    return PassageSet(
        id = id,
        instruction = instruction,
        documents = documents.map { it.toDomain(id) },
        questions = questions.map { it.toDomain(id) },
        timeLimitSec = timeLimitSec
    )
}

private fun DocumentDto.toDomain(setId: String): PassageDocument = PassageDocument(
    kind = kind.toDocumentKind(setId),
    header = header?.let { DocumentHeader(it.to, it.from, it.date, it.subject) },
    title = title,
    body = body
)

private fun QuestionDto.toDomain(setId: String): PassageQuestion {
    require(options.size in PassageConstants.MIN_OPTIONS..PassageConstants.MAX_OPTIONS) {
        "Question '$number' in set '$setId' must have " +
            "${PassageConstants.MIN_OPTIONS}..${PassageConstants.MAX_OPTIONS} options, got ${options.size}"
    }
    require(answerIndex in options.indices) {
        "Question '$number' in set '$setId' has answerIndex $answerIndex out of range ${options.indices}"
    }
    return PassageQuestion(
        number = number,
        stem = stem,
        options = options,
        answerIndex = answerIndex,
        explanation = explanation
    )
}

private fun String.toDocumentKind(setId: String): DocumentKind = when (lowercase()) {
    "article" -> DocumentKind.ARTICLE
    "email" -> DocumentKind.EMAIL
    "notice" -> DocumentKind.NOTICE
    else -> throw IllegalArgumentException("Unknown document kind '$this' in set '$setId'")
}
