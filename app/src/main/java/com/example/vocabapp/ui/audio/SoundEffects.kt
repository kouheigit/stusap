package com.example.vocabapp.ui.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class SoundPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeSynthTrack = AtomicReference<AudioTrack?>(null)

    fun playCorrect() {
        playSynthBuffer(correctSoundBuffer)
    }

    fun playWrong() {
        playSynthBuffer(wrongSoundBuffer)
    }

    fun playSequence(segments: List<Pair<Float, Int>>, interrupt: Boolean = true) {
        val buffer = buildSynthBuffer(segments)
        if (interrupt) {
            playSynthBuffer(buffer)
        } else {
            scope.launch {
                delay(40)
                playSynthBuffer(buffer)
            }
        }
    }

    fun dispose() {
        scope.cancel()
        activeSynthTrack.getAndSet(null)?.runCatching {
            stop()
            flush()
            release()
        }
    }

    private fun playSynthBuffer(buffer: ShortArray) {
        activeSynthTrack.getAndSet(null)?.runCatching {
            stop()
            flush()
            release()
        }
        scope.launch {
            var track: AudioTrack? = null
            try {
                track = createAudioTrack(buffer)
                activeSynthTrack.set(track)
                track.play()
                delay(buffer.size * 1_000L / SAMPLE_RATE + 80)
            } catch (_: Exception) {
            } finally {
                activeSynthTrack.compareAndSet(track, null)
                track?.runCatching {
                    stop()
                    flush()
                    release()
                }
            }
        }
    }
}

internal fun createSoundPlayer(): SoundPlayer = SoundPlayer()

private val correctSoundBuffer: ShortArray by lazy {
    buildSynthBuffer(listOf(698f to 140, 880f to 260))
}

private val wrongSoundBuffer: ShortArray by lazy {
    buildSynthBuffer(listOf(280f to 190, 0f to 45, 220f to 230))
}

private fun buildSynthBuffer(segments: List<Pair<Float, Int>>): ShortArray {
    val paddingSamples = SAMPLE_RATE * SILENCE_PADDING_MS / 1_000
    val totalSamples = paddingSamples + segments.sumOf { (_, ms) -> SAMPLE_RATE * ms / 1_000 }
    val buffer = ShortArray(totalSamples) // leading zeros = silence
    var position = paddingSamples
    for ((frequency, durationMs) in segments) {
        val sampleCount = SAMPLE_RATE * durationMs / 1_000
        for (index in 0 until sampleCount) {
            if (frequency == 0f) {
                buffer[position++] = 0
                continue
            }
            val envelope = when {
                index < sampleCount * 0.10 -> index / (sampleCount * 0.10)
                index > sampleCount * 0.65 -> (sampleCount - index).toDouble() / (sampleCount * 0.35)
                else -> 1.0
            }.coerceIn(0.0, 1.0)
            val wave = kotlin.math.sin(2 * Math.PI * frequency * index / SAMPLE_RATE)
            val sample = (wave * Short.MAX_VALUE * 0.95 * envelope).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[position++] = sample.toShort()
        }
    }
    return buffer
}

private fun createAudioTrack(buffer: ShortArray): AudioTrack =
    AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()
        .also { track ->
            track.write(buffer, 0, buffer.size)
            track.setVolume(AudioTrack.getMaxVolume())
        }

private const val SAMPLE_RATE = 44_100
private const val SILENCE_PADDING_MS = 5
