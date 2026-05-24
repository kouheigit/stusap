package com.example.vocabapp.ui.screen.common

import android.media.AudioManager

internal interface SpeechVolumeGateway {
    val maxMusicVolume: Int
    val currentMusicVolume: Int
    fun setMusicVolume(volume: Int)
}

internal class AndroidSpeechVolumeGateway(
    private val audioManager: AudioManager
) : SpeechVolumeGateway {
    override val maxMusicVolume: Int
        get() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    override val currentMusicVolume: Int
        get() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    override fun setMusicVolume(volume: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }
}

internal class SpeechVolumeController(
    private val gateway: SpeechVolumeGateway,
    private val reportFailure: (String, Throwable) -> Unit
) {
    private var originalVolume: Int? = null
    private var activeUtteranceId: String? = null

    fun beginSpeech(utteranceId: String) {
        activeUtteranceId = utteranceId
        val currentVolume = gateway.currentMusicVolume
        val maxVolume = gateway.maxMusicVolume
        if (currentVolume >= maxVolume) return
        if (originalVolume == null) {
            originalVolume = currentVolume
        }
        val didBoost = setVolumeOrReport(
            volume = maxVolume,
            action = "Failed to boost media volume for TTS"
        )
        if (!didBoost && originalVolume == currentVolume) {
            originalVolume = null
        }
    }

    fun finishSpeech(utteranceId: String?): Boolean {
        if (utteranceId != activeUtteranceId) return false
        activeUtteranceId = null
        restoreOriginalVolume()
        return true
    }

    fun cancelSpeech() {
        activeUtteranceId = null
        restoreOriginalVolume()
    }

    private fun restoreOriginalVolume() {
        val volumeToRestore = originalVolume ?: return
        originalVolume = null
        setVolumeOrReport(
            volume = volumeToRestore,
            action = "Failed to restore media volume after TTS"
        )
    }

    private fun setVolumeOrReport(volume: Int, action: String): Boolean {
        return try {
            gateway.setMusicVolume(volume)
            true
        } catch (error: RuntimeException) {
            reportFailure(action, error)
            false
        }
    }
}
