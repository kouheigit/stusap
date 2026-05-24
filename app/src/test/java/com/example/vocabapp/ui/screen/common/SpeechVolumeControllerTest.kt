package com.example.vocabapp.ui.screen.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechVolumeControllerTest {

    @Test
    fun `beginSpeech boosts music volume and finishSpeech restores it`() {
        val gateway = FakeSpeechVolumeGateway(current = 3, maxMusicVolume = 10)
        val controller = SpeechVolumeController(gateway, ::error)

        controller.beginSpeech("first")
        assertEquals(listOf(10), gateway.setVolumes)
        assertEquals(10, gateway.currentMusicVolume)

        assertTrue(controller.finishSpeech("first"))
        assertEquals(listOf(10, 3), gateway.setVolumes)
        assertEquals(3, gateway.currentMusicVolume)
    }

    @Test
    fun `finishSpeech ignores callbacks from older utterances`() {
        val gateway = FakeSpeechVolumeGateway(current = 3, maxMusicVolume = 10)
        val controller = SpeechVolumeController(gateway, ::error)

        controller.beginSpeech("first")
        controller.beginSpeech("second")

        assertFalse(controller.finishSpeech("first"))
        assertEquals(10, gateway.currentMusicVolume)
        assertEquals(listOf(10), gateway.setVolumes)

        assertTrue(controller.finishSpeech("second"))
        assertEquals(3, gateway.currentMusicVolume)
        assertEquals(listOf(10, 3), gateway.setVolumes)
    }

    @Test
    fun `beginSpeech reports boost failure and does not restore an unchanged volume`() {
        val gateway = FakeSpeechVolumeGateway(current = 3, maxMusicVolume = 10).apply {
            failNextSet = true
        }
        val failures = mutableListOf<String>()
        val controller = SpeechVolumeController(gateway) { message, _ -> failures += message }

        controller.beginSpeech("first")
        assertEquals(listOf("Failed to boost media volume for TTS"), failures)
        assertEquals(3, gateway.currentMusicVolume)

        assertTrue(controller.finishSpeech("first"))
        assertEquals(listOf(10), gateway.setVolumes)
    }

    @Test
    fun `cancelSpeech restores boosted volume without an utterance callback`() {
        val gateway = FakeSpeechVolumeGateway(current = 2, maxMusicVolume = 8)
        val controller = SpeechVolumeController(gateway, ::error)

        controller.beginSpeech("first")
        controller.cancelSpeech()

        assertEquals(2, gateway.currentMusicVolume)
        assertEquals(listOf(8, 2), gateway.setVolumes)
    }

    private fun error(message: String, error: Throwable) {
        throw AssertionError("$message: ${error.message}", error)
    }

    private class FakeSpeechVolumeGateway(
        current: Int,
        override val maxMusicVolume: Int
    ) : SpeechVolumeGateway {
        private var currentVolume = current
        val setVolumes = mutableListOf<Int>()
        var failNextSet = false

        override val currentMusicVolume: Int
            get() = currentVolume

        override fun setMusicVolume(volume: Int) {
            setVolumes += volume
            if (failNextSet) {
                failNextSet = false
                throw SecurityException("volume denied")
            }
            currentVolume = volume
        }
    }
}
