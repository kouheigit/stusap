package com.example.vocabapp.ui.audio

import android.util.Log

private const val AUDIO_PLAYBACK_TAG = "AudioPlayback"
// JVM unit tests run outside Dalvik, where android.util.Log is not backed by the framework.
private val canUseAndroidLog: Boolean =
    System.getProperty("java.vm.name").orEmpty().contains("Dalvik", ignoreCase = true)

internal fun warnAudioPlayback(message: String, throwable: Throwable? = null) {
    if (!canUseAndroidLog) return
    if (throwable == null) {
        Log.w(AUDIO_PLAYBACK_TAG, message)
    } else {
        Log.w(AUDIO_PLAYBACK_TAG, message, throwable)
    }
}
