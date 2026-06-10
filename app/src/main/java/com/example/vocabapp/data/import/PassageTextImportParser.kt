package com.example.vocabapp.data.import

import com.example.vocabapp.domain.model.DocumentKind
import com.example.vocabapp.domain.model.PassageConstants
import com.example.vocabapp.domain.model.PassageDocument
import com.example.vocabapp.domain.model.PassageQuestion
import com.example.vocabapp.domain.model.PassageSet

class PassageTextImportParser {
    fun parse(rawText: String): PassageSet {
        val lines = rawText.lines()
        val title = lines.firstHeaderValue("TITLE", "タイトル") ?: "長文問題"
        val kind = (lines.firstHeaderValue("TYPE", "種類") ?: "other").toDocumentKind()
        val timeLimitSec = lines.firstHeaderValue("TIME_LIMIT", "制限時間")?.toIntOrNull()
        val bodyStart = lines.indexOfFirst { it.trim().equals("本文:", ignoreCase = true) }
        require(bodyStart >= 0) { "本文: セクションがありません" }

        val firstQuestionIndex = lines.indexOfFirstQuestion(startIndex = bodyStart + 1)
        require(firstQuestionIndex > bodyStart) { "Q1: から始まる設問がありません" }
        val body = lines.subList(bodyStart + 1, firstQuestionIndex)
            .joinToString("\n")
            .trim()
        require(body.isNotBlank()) { "本文が空です" }

        val questions = parseQuestions(lines.drop(firstQuestionIndex))
        return PassageSet(
            id = DEFAULT_PREVIEW_ID,
            instruction = "Read the passage and choose the best answer to each question.",
            documents = listOf(
                PassageDocument(
                    kind = kind,
                    title = title,
                    body = body
                )
            ),
            questions = questions,
            timeLimitSec = timeLimitSec
        )
    }

    private fun parseQuestions(lines: List<String>): List<PassageQuestion> {
        val questionStarts = lines.mapIndexedNotNull { index, line ->
            if (QUESTION_PATTERN.matches(line.trim())) index else null
        }
        require(questionStarts.isNotEmpty()) { "設問がありません" }
        return questionStarts.mapIndexed { position, start ->
            val end = questionStarts.getOrNull(position + 1) ?: lines.size
            parseQuestion(lines.subList(start, end))
        }
    }

    private fun parseQuestion(lines: List<String>): PassageQuestion {
        val header = lines.first().trim()
        val questionMatch = requireNotNull(QUESTION_PATTERN.matchEntire(header)) {
            "設問の形式が正しくありません"
        }
        val number = "Q${questionMatch.groupValues[1]}"
        val stem = questionMatch.groupValues[2].trim()
        require(stem.isNotBlank()) { "$number の問題文が空です" }

        val choices = mutableMapOf<Char, String>()
        var answerLabel: Char? = null
        var explanation: String? = null
        for (line in lines.drop(1)) {
            val trimmed = line.trim()
            when {
                CHOICE_PATTERN.matches(trimmed) -> {
                    val match = CHOICE_PATTERN.matchEntire(trimmed)!!
                    choices[match.groupValues[1].single()] = match.groupValues[2].trim()
                }
                ANSWER_PATTERN.matches(trimmed) -> {
                    answerLabel = ANSWER_PATTERN.matchEntire(trimmed)!!.groupValues[1].single()
                }
                EXPLANATION_PATTERN.matches(trimmed) -> {
                    explanation = EXPLANATION_PATTERN.matchEntire(trimmed)!!.groupValues[1].trim().ifBlank { null }
                }
            }
        }

        val orderedChoices = ('A'..'D').mapNotNull { choices[it] }
        require(orderedChoices.size in PassageConstants.MIN_OPTIONS..PassageConstants.MAX_OPTIONS) {
            "$number の選択肢は${PassageConstants.MIN_OPTIONS}〜${PassageConstants.MAX_OPTIONS}個必要です"
        }
        require(orderedChoices.none { it.isBlank() }) { "$number の選択肢に空欄があります" }
        val answer = requireNotNull(answerLabel) { "$number の正解がありません" }
        val answerIndex = answer - 'A'
        require(answerIndex in orderedChoices.indices) { "$number の正解は選択肢の範囲内で指定してください" }

        return PassageQuestion(
            number = number,
            stem = stem,
            options = orderedChoices,
            answerIndex = answerIndex,
            explanation = explanation
        )
    }

    private fun List<String>.firstHeaderValue(vararg labels: String): String? {
        val normalizedLabels = labels.map { it.lowercase() }
        return firstNotNullOfOrNull { line ->
            val index = line.indexOf(':')
            if (index < 0) return@firstNotNullOfOrNull null
            val label = line.substring(0, index).trim().lowercase()
            if (label in normalizedLabels) line.substring(index + 1).trim().ifBlank { null } else null
        }
    }

    private fun List<String>.indexOfFirstQuestion(startIndex: Int): Int =
        drop(startIndex).indexOfFirst { QUESTION_PATTERN.matches(it.trim()) }
            .let { if (it < 0) -1 else it + startIndex }

    private fun String.toDocumentKind(): DocumentKind = when (lowercase()) {
        "article" -> DocumentKind.ARTICLE
        "email" -> DocumentKind.EMAIL
        "notice" -> DocumentKind.NOTICE
        else -> DocumentKind.ARTICLE
    }

    companion object {
        private const val DEFAULT_PREVIEW_ID = "custom-import"
        private val QUESTION_PATTERN = Regex("""Q(\d+):\s*(.+)""")
        private val CHOICE_PATTERN = Regex("""([A-D])\.\s*(.+)""")
        private val ANSWER_PATTERN = Regex("""(?:ANSWER|正解):\s*([A-D])""", RegexOption.IGNORE_CASE)
        private val EXPLANATION_PATTERN = Regex("""(?:EXPLANATION|解説):\s*(.*)""", RegexOption.IGNORE_CASE)
    }
}
