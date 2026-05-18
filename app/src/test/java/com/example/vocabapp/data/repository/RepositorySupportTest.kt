package com.example.vocabapp.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class RepositorySupportTest {
    @Test
    fun customIds_keepWordAndIdiomRangesSeparated() {
        assertEquals(-10_001, customTrainingId("word", 1))
        assertEquals(-20_001, customTrainingId("idiom", 1))
        assertEquals(-10_999, randomCustomTrainingId("word"))
        assertEquals(-20_999, randomCustomTrainingId("idiom"))
    }

    @Test
    fun customWordDomainId_keepsCustomContentAwayFromSeedWordIds() {
        assertEquals(-100_123, customWordDomainId("word", 123))
        assertEquals(-200_123, customWordDomainId("idiom", 123))
    }
}
