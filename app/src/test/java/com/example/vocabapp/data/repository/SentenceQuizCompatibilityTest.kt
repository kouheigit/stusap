package com.example.vocabapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SentenceQuizCompatibilityTest {

    @Test
    fun isQuizReadySentence_plainSixWords_returnsTrue() {
        assertTrue("I might stay as well here".isQuizReadySentence())
    }

    @Test
    fun isQuizReadySentence_plainFiveWords_returnsFalse() {
        assertFalse("This is too short here".isQuizReadySentence())
    }

    @Test
    fun isQuizReadySentence_bracketFour_returnsTrue() {
        assertTrue("I [might][stay][as][well] as join in a tour".isQuizReadySentence())
    }

    @Test
    fun isQuizReadySentence_bracketThree_returnsFalse() {
        assertFalse("I [might][stay][as] join in a tour".isQuizReadySentence())
    }

    @Test
    fun isQuizReadySentence_bracketFive_returnsFalse() {
        assertFalse("I [might][stay][as][well][soon] join in a tour".isQuizReadySentence())
    }

    @Test
    fun isQuizReadySentence_blankString_returnsFalse() {
        assertFalse("".isQuizReadySentence())
        assertFalse("   ".isQuizReadySentence())
    }

    @Test
    fun isQuizReadySentence_exactlySixWords_returnsTrue() {
        assertTrue("one two three four five six".isQuizReadySentence())
    }

    @Test
    fun sentenceType_noBrackets_returnsA() {
        assertEquals("A", "Hello world this is plain text".sentenceType())
    }

    @Test
    fun sentenceType_withBrackets_returnsB() {
        assertEquals("B", "I [might][stay][as][well] in a tour".sentenceType())
    }
}
