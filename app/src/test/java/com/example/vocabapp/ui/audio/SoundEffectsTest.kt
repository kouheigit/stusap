package com.example.vocabapp.ui.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundEffectsTest {

    @Test
    fun buildSynthBuffer_addsSilencePaddingAroundSegments() {
        val paddingSamples = 220
        val segmentSamples = 4_410
        val buffer = buildSynthBuffer(listOf(440f to 100))

        assertEquals(paddingSamples + segmentSamples + paddingSamples, buffer.size)
        assertEquals(0, buffer.take(paddingSamples).sumOf { kotlin.math.abs(it.toInt()) })
        assertEquals(0, buffer.takeLast(paddingSamples).sumOf { kotlin.math.abs(it.toInt()) })
    }

    @Test
    fun buildSynthBuffer_keepsSamplesWithinConfiguredPcmHeadroom() {
        val maxExpectedAmplitude = (Short.MAX_VALUE * 0.95).toInt()
        val buffer = buildSynthBuffer(listOf(440f to 100))
        val audibleSamples = buffer.filter { it != 0.toShort() }

        assertTrue(audibleSamples.isNotEmpty())
        assertTrue(audibleSamples.any { it > 0 })
        assertTrue(audibleSamples.any { it < 0 })
        assertTrue(audibleSamples.all { kotlin.math.abs(it.toInt()) <= maxExpectedAmplitude })
    }

    @Test
    fun importSuccessSoundSegments_usesTwoBrightTonesWithShortGap() {
        val segments = importSuccessSoundSegments()

        assertEquals(listOf(740f to 90, 0f to 28, 988f to 170), segments)
    }

    @Test
    fun smoothStep_clampsBoundaryInputs() {
        val delta = 0.000_001

        assertEquals(0.0, smoothStep(-0.25), delta)
        assertEquals(0.0, smoothStep(0.0), delta)
        assertEquals(0.5, smoothStep(0.5), delta)
        assertEquals(1.0, smoothStep(1.0), delta)
        assertEquals(1.0, smoothStep(1.25), delta)
    }
}
