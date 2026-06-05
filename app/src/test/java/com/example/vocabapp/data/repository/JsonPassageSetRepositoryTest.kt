package com.example.vocabapp.data.repository

import com.example.vocabapp.domain.model.DocumentKind
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonPassageSetRepositoryTest {

    /** Gradle のユニットテストは作業ディレクトリが app/ のため、リポジトリ直下の fixtures を参照する。 */
    private fun fixtureSource() = PassageSetSource {
        listOf(File("../fixtures/sets.json"), File("fixtures/sets.json"))
            .first { it.exists() }
            .readText()
    }

    private fun repository(source: PassageSetSource) = JsonPassageSetRepository(source)

    @Test
    fun loadSets_parsesBothFixtureSets() = runBlocking {
        val sets = repository(fixtureSource()).loadSets()

        assertEquals(2, sets.size)
        assertEquals(listOf("coffee-article-001", "pool-notice-email-001"), sets.map { it.id })
    }

    @Test
    fun loadSets_coffeeSetHasSingleArticleAndThreeQuestions() = runBlocking {
        val coffee = repository(fixtureSource()).getSet("coffee-article-001")

        assertNotNull(coffee)
        assertEquals(1, coffee!!.documents.size)
        assertEquals(DocumentKind.ARTICLE, coffee.documents.single().kind)
        assertEquals(3, coffee.questions.size)
    }

    @Test
    fun loadSets_poolSetIsMultiDocumentWithNoticeAndEmail() = runBlocking {
        val pool = repository(fixtureSource()).getSet("pool-notice-email-001")!!

        assertEquals(
            listOf(DocumentKind.NOTICE, DocumentKind.EMAIL),
            pool.documents.map { it.kind }
        )
        // email 文書はヘッダーを持つ
        assertNotNull(pool.documents.first { it.kind == DocumentKind.EMAIL }.header)
    }

    @Test
    fun loadSets_poolSetContainsNotQuestionWithCorrectAnswer() = runBlocking {
        val pool = repository(fixtureSource()).getSet("pool-notice-email-001")!!
        val notQuestion = pool.questions.first { it.stem.contains("NOT") }

        assertEquals("2-6", notQuestion.number)
        assertEquals(4, notQuestion.options.size)
        // ゼロ始まりの正解インデックスが範囲内であること
        assertTrue(notQuestion.answerIndex in notQuestion.options.indices)
        assertEquals("Swimming lessons will be free for all visitors.", notQuestion.options[notQuestion.answerIndex])
    }

    @Test
    fun getSet_returnsNullForUnknownId() = runBlocking {
        assertNull(repository(fixtureSource()).getSet("does-not-exist"))
    }

    @Test
    fun loadSets_rejectsAnswerIndexOutOfRange() {
        val badAnswer = PassageSetSource {
            """
            [{"id":"x","instruction":"i","documents":[{"kind":"article","body":"b"}],
              "questions":[{"number":"1-1","stem":"s","options":["a","b"],"answerIndex":5}]}]
            """.trimIndent()
        }

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository(badAnswer).loadSets() }
        }
    }

    @Test
    fun loadSets_rejectsTooFewOptions() {
        val tooFew = PassageSetSource {
            """
            [{"id":"x","instruction":"i","documents":[{"kind":"article","body":"b"}],
              "questions":[{"number":"1-1","stem":"s","options":["a"],"answerIndex":0}]}]
            """.trimIndent()
        }

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository(tooFew).loadSets() }
        }
    }

    @Test
    fun loadSets_rejectsUnknownDocumentKind() {
        val badKind = PassageSetSource {
            """
            [{"id":"x","instruction":"i","documents":[{"kind":"poster","body":"b"}],
              "questions":[{"number":"1-1","stem":"s","options":["a","b"],"answerIndex":0}]}]
            """.trimIndent()
        }

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository(badKind).loadSets() }
        }
    }
}
