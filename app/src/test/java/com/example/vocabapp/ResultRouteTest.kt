package com.example.vocabapp

import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.ui.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Test

class ResultRouteTest {

    @Test
    fun retryRouteFor_wordCustomSetTwoKeepsSetTwo() {
        val result = result(trainingId = ContentType.WORD.trainingId(2))

        assertEquals(
            Route.customTrainingQuiz(ContentType.WORD.routeValue, 2),
            retryRouteFor(result)
        )
    }

    @Test
    fun retryRouteFor_idiomCustomSetElevenKeepsSetEleven() {
        val result = result(trainingId = ContentType.IDIOM.trainingId(11))

        assertEquals(
            Route.customTrainingQuiz(ContentType.IDIOM.routeValue, 11),
            retryRouteFor(result)
        )
    }

    @Test
    fun retryRouteFor_randomCustomKeepsRandomMode() {
        assertEquals(
            Route.randomCustomQuiz(ContentType.WORD.routeValue),
            retryRouteFor(result(trainingId = ContentType.WORD.randomTrainingId))
        )
        assertEquals(
            Route.randomCustomQuiz(ContentType.IDIOM.routeValue),
            retryRouteFor(result(trainingId = ContentType.IDIOM.randomTrainingId))
        )
    }

    @Test
    fun nextRouteFor_wordCustomSetMovesToNextSetWhenAvailable() {
        val result = result(trainingId = ContentType.WORD.trainingId(1))

        assertEquals(
            Route.customTrainingQuiz(ContentType.WORD.routeValue, 2),
            nextRouteFor(result, hasNextCustomSet = true)
        )
    }

    @Test
    fun nextRouteFor_idiomCustomSetMovesToNextSetWhenAvailable() {
        val result = result(trainingId = ContentType.IDIOM.trainingId(1))

        assertEquals(
            Route.customTrainingQuiz(ContentType.IDIOM.routeValue, 2),
            nextRouteFor(result, hasNextCustomSet = true)
        )
    }

    @Test
    fun nextRouteFor_customSetReturnsListWhenNextSetIsUnavailable() {
        val result = result(trainingId = ContentType.WORD.trainingId(1))

        assertEquals(
            Route.customTraining(ContentType.WORD.routeValue),
            nextRouteFor(result, hasNextCustomSet = false)
        )
    }

    @Test
    fun nextRouteFor_randomCustomIgnoresSequentialSetFlag() {
        val result = result(trainingId = ContentType.WORD.randomTrainingId)

        assertEquals(
            Route.customTraining(ContentType.WORD.routeValue),
            nextRouteFor(result, hasNextCustomSet = true)
        )
    }

    private fun result(trainingId: Int): QuizResult =
        QuizResult(
            attemptId = 1L,
            trainingId = trainingId,
            isReview = false,
            totalQuestions = 10,
            correctCount = 0,
            wrongCount = 10,
            accuracy = 0f,
            studySeconds = 0,
            starCount = 0
        )
}
