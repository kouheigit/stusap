package com.example.vocabapp.ui.audio

import android.content.Context
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

internal class SoundPlayer(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activeSynthTrack = AtomicReference<AudioTrack?>(null)

    fun playCorrect() {
        playSynthBuffer(correctSoundBuffer)
    }

    fun playWrong() {
        playSynthBuffer(wrongSoundBuffer)
    }

    fun playImportSuccess() {
        playSynthBuffer(importSuccessSoundBuffer)
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
        }?.onFailure { error ->
            warnAudioPlayback("Failed to release active synth audio track", error)
        }
    }

    private fun playSynthBuffer(buffer: ShortArray) {
        activeSynthTrack.getAndSet(null)?.runCatching {
            stop()
            flush()
            release()
        }?.onFailure { error ->
            warnAudioPlayback("Failed to release previous synth audio track", error)
        }
        scope.launch {
            var track: AudioTrack? = null
            try {
                track = createAudioTrack(buffer)
                activeSynthTrack.set(track)
                track.play()
                delay(buffer.size * 1_000L / SAMPLE_RATE + 80)
            } catch (error: Exception) {
                warnAudioPlayback("Failed to play synth audio track", error)
            } finally {
                activeSynthTrack.compareAndSet(track, null)
                track?.runCatching {
                    stop()
                    flush()
                    release()
                }?.onFailure { error ->
                    warnAudioPlayback("Failed to release completed synth audio track", error)
                }
            }
        }
    }
}

internal fun createSoundPlayer(context: Context): SoundPlayer = SoundPlayer(context)

private val correctSoundBuffer: ShortArray by lazy {
    buildSynthBuffer(listOf(698f to 140, 880f to 260))
}

private val wrongSoundBuffer: ShortArray by lazy {
    buildSynthBuffer(listOf(280f to 190, 0f to 45, 220f to 230))
}

private val importSuccessSoundBuffer: ShortArray by lazy {
    buildSynthBuffer(importSuccessSoundSegments())
}

internal fun importSuccessSoundSegments(): List<Pair<Float, Int>> =
    listOf(740f to 90, 0f to 28, 988f to 170)

internal fun buildSynthBuffer(segments: List<Pair<Float, Int>>): ShortArray {
    val paddingSamples = SAMPLE_RATE * SILENCE_PADDING_MS / 1_000
    val totalSamples = paddingSamples + segments.sumOf { (_, ms) -> SAMPLE_RATE * ms / 1_000 } + paddingSamples
    val buffer = ShortArray(totalSamples) // leading zeros = silence
    var position = paddingSamples
    var phase = 0.0
    for ((frequency, durationMs) in segments) {
        val sampleCount = SAMPLE_RATE * durationMs / 1_000
        for (index in 0 until sampleCount) {
            if (frequency == 0f) {
                buffer[position++] = 0
                phase = 0.0
                continue
            }
            val envelope = when {
                index < sampleCount * FADE_IN_RATIO -> smoothStep(index / (sampleCount * FADE_IN_RATIO))
                index > sampleCount * FADE_OUT_START_RATIO -> {
                    val fadePosition = (sampleCount - index).toDouble() / (sampleCount * (1.0 - FADE_OUT_START_RATIO))
                    smoothStep(fadePosition)
                }
                else -> 1.0
            }.coerceIn(0.0, 1.0)
            val wave = kotlin.math.sin(phase)
            val sample = (wave * Short.MAX_VALUE * 0.95 * envelope).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[position++] = sample.toShort()
            phase = (phase + 2 * Math.PI * frequency / SAMPLE_RATE) % (2 * Math.PI)
        }
    }
    return buffer
}

internal fun smoothStep(value: Double): Double {
    val x = value.coerceIn(0.0, 1.0)
    return x * x * (3 - 2 * x)
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
private const val FADE_IN_RATIO = 0.10
private const val FADE_OUT_START_RATIO = 0.65
