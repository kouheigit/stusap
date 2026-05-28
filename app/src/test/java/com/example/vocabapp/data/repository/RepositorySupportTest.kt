package com.example.vocabapp.data.repository

import com.example.vocabapp.domain.model.ContentType
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

    @Test
    fun customTrainingSetNumber_restoresSetNumberFromTrainingId() {
        assertEquals(1, customTrainingSetNumber("word", -10_001))
        assertEquals(2, customTrainingSetNumber("word", -10_002))
        assertEquals(11, customTrainingSetNumber("word", -10_011))
        assertEquals(1, customTrainingSetNumber("idiom", -20_001))
        assertEquals(2, customTrainingSetNumber("idiom", -20_002))
    }

    @Test
    fun customContentTypeForTrainingId_detectsCustomTrainingRanges() {
        assertEquals(ContentType.WORD, customContentTypeForTrainingId(-10_001))
        assertEquals(ContentType.IDIOM, customContentTypeForTrainingId(-20_001))
        assertEquals(null, customContentTypeForTrainingId(1))
    }
}
