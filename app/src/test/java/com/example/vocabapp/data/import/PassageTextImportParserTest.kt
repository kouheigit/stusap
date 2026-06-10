package com.example.vocabapp.data.import

import com.example.vocabapp.domain.model.DocumentKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PassageTextImportParserTest {
    private val parser = PassageTextImportParser()

    @Test
    fun parse_acceptsEnglishLabels() {
        val set = parser.parse(
            """
            TITLE: Email about schedule change
            TYPE: email
            TIME_LIMIT: 300

            本文:
            Dear members,
            The morning class will move to Room B.

            Q1: What is the main purpose of this email?
            A. To cancel every class
            B. To announce a room change
            C. To introduce a new teacher
            D. To sell tickets
            ANSWER: B
            EXPLANATION: The email says the class will move to Room B.
            """.trimIndent()
        )

        assertEquals("custom-import", set.id)
        assertEquals(300, set.timeLimitSec)
        assertEquals(DocumentKind.EMAIL, set.documents.single().kind)
        assertEquals("Email about schedule change", set.documents.single().title)
        assertTrue(set.documents.single().body.contains("Room B"))
        assertEquals("Q1", set.questions.single().number)
        assertEquals(1, set.questions.single().answerIndex)
        assertEquals("To announce a room change", set.questions.single().options[1])
    }

    @Test
    fun parse_acceptsJapaneseLabels() {
        val set = parser.parse(
            """
            タイトル: 図書館のお知らせ
            種類: notice
            制限時間: 420

            本文:
            The library will close early on Friday.

            Q1: What will happen on Friday?
            A. The library will close early
            B. The library will open late
            C. The library will move
            D. The library will sell books
            正解: A
            解説: 本文に close early とあります。
            """.trimIndent()
        )

        assertEquals(420, set.timeLimitSec)
        assertEquals(DocumentKind.NOTICE, set.documents.single().kind)
        assertEquals("図書館のお知らせ", set.documents.single().title)
        assertEquals(0, set.questions.single().answerIndex)
        assertEquals("本文に close early とあります。", set.questions.single().explanation)
    }

    @Test
    fun parse_rejectsMissingBody() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            parser.parse(
                """
                TITLE: Broken

                Q1: What is missing?
                A. Body
                B. Title
                C. Answer
                D. Options
                ANSWER: A
                """.trimIndent()
            )
        }

        assertTrue(error.message!!.contains("本文"))
    }

    @Test
    fun parse_rejectsInvalidAnswerLabel() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            parser.parse(
                """
                TITLE: Broken

                本文:
                This passage has an invalid answer label.

                Q1: Which answer is invalid?
                A. A
                B. B
                C. C
                D. D
                ANSWER: E
                """.trimIndent()
            )
        }

        assertTrue(error.message!!.contains("正解"))
    }

    @Test
    fun parse_rejectsQuestionWithTooFewChoices() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            parser.parse(
                """
                TITLE: Broken

                本文:
                This passage has too few choices.

                Q1: What is wrong?
                A. Only one choice
                ANSWER: A
                """.trimIndent()
            )
        }

        assertTrue(error.message!!.contains("選択肢"))
    }
}
