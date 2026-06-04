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
}
