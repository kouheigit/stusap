package com.example.vocabapp

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.vocabapp.ui.audio.SoundPlayer
import com.example.vocabapp.ui.audio.createSoundPlayer
import java.util.Locale

@Composable
internal fun rememberSoundPlayer(): SoundPlayer {
    val player = remember { createSoundPlayer() }
    DisposableEffect(player) {
        onDispose { player.dispose() }
    }
    return player
}

internal data class Speaker(
    val isReady: Boolean,
    val speak: (String) -> Unit
)


@Composable
internal fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isReady by remember { mutableStateOf(false) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val pendingSpeechText = remember { java.util.concurrent.atomic.AtomicReference<String?>(null) }
    val isTtsConfigured = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
    val focusRequest = remember {
        android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setOnAudioFocusChangeListener {}
            .build()
    }
    // 直前に発話した内容と時刻を記録して、短時間の重複リクエストを防ぐ
    val lastSpokenText = remember { java.util.concurrent.atomic.AtomicReference<String>("") }
    val lastSpokenAt = remember { java.util.concurrent.atomic.AtomicLong(0L) }
    fun speakNow(text: String, engine: TextToSpeech) {
        val now = System.currentTimeMillis()
        val isSameTextRecently = lastSpokenText.get() == text && now - lastSpokenAt.get() < 400L
        if (isSameTextRecently) return
        lastSpokenText.set(text)
        lastSpokenAt.set(now)
        engine.stop()
        audioManager.requestAudioFocus(focusRequest)
        val utteranceId = "utt-${System.nanoTime()}"
        val params = android.os.Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun configureTts(engine: TextToSpeech) {
        if (!isTtsConfigured.compareAndSet(false, true)) return

        val langResult = engine.setLanguage(Locale.US)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.language = Locale.ENGLISH
        }
        engine.setSpeechRate(0.9f)
        isReady = true
        pendingSpeechText.getAndSet(null)?.let { text ->
            speakNow(text, engine)
        }
    }

    DisposableEffect(context) {
        var ttsRef: TextToSpeech? = null
        val instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                mainHandler.post {
                    ttsRef?.let(::configureTts)
                }
            }
        }
        ttsRef = instance
        instance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                mainHandler.post { audioManager.abandonAudioFocusRequest(focusRequest) }
            }

            @Deprecated("Deprecated by Android SDK")
            override fun onError(utteranceId: String?) = Unit

            override fun onError(utteranceId: String?, errorCode: Int) = Unit
        })
        tts = instance
        onDispose {
            isReady = false
            isTtsConfigured.set(false)
            pendingSpeechText.set(null)
            instance.stop()
            instance.shutdown()
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }
    val speak: (String) -> Unit = speak@ { text ->
        if (text.isBlank()) return@speak
        val engine = tts
        if (engine != null && isReady) {
            speakNow(text, engine)
        } else {
            pendingSpeechText.set(text)
        }
    }
    return Speaker(isReady = isReady, speak = speak)
}


