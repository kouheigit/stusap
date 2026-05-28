package com.example.vocabapp

import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.Training
import org.junit.Assert.assertEquals
import org.junit.Test

class CustomTrainingListScreenTest {

    @Test
    fun customSetNumber_usesTrainingIdForWordTraining() {
        val training = training(
            id = ContentType.WORD.trainingId(2),
            lessonId = ContentType.WORD.lessonId,
            wordStartNumber = 1
        )

        assertEquals(2, customSetNumber(training))
    }

    @Test
    fun customSetNumber_usesTrainingIdForIdiomTraining() {
        val training = training(
            id = ContentType.IDIOM.trainingId(3),
            lessonId = ContentType.IDIOM.lessonId,
            wordStartNumber = 1
        )

        assertEquals(3, customSetNumber(training))
    }

    private fun training(id: Int, lessonId: Int, wordStartNumber: Int): Training =
        Training(
            id = id,
            lessonId = lessonId,
            title = "custom",
            wordStartNumber = wordStartNumber,
            wordEndNumber = wordStartNumber + 9,
            studyCount = 0,
            bestAccuracy = 0f,
            bestStarCount = 0,
            lastStudiedAt = null
        )
}
