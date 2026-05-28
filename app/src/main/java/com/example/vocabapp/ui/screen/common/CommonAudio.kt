package com.example.vocabapp.ui.screen.common

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.vocabapp.ui.audio.SoundPlayer
import com.example.vocabapp.ui.audio.createSoundPlayer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.Locale
import kotlinx.coroutines.delay

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
internal fun AutoSpeakEffect(
    text: String?,
    speaker: Speaker,
    triggerKey: Any? = text,
    delayMillis: Long = AUTO_SPEAK_DELAY_MILLIS
) {
    LaunchedEffect(triggerKey, speaker.isReady) {
        if (text.isNullOrBlank()) return@LaunchedEffect
        if (!speaker.isReady) return@LaunchedEffect
        delay(delayMillis)
        speaker.speak(text)
    }
}

@Composable
internal fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isReady by remember { mutableStateOf(false) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val pendingSpeechText = remember { AtomicReference<String?>(null) }
    val isTtsConfigured = remember { AtomicBoolean(false) }
    val activeUtteranceId = remember { AtomicReference<String?>(null) }
    val focusRequest = remember {
        android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setOnAudioFocusChangeListener {}
            .build()
    }
    // 直前に発話した内容と時刻を記録して、短時間の重複リクエストを防ぐ
    val lastSpokenText = remember { AtomicReference("") }
    val lastSpokenAt = remember { AtomicLong(0L) }
    fun finishSpeech(utteranceId: String?) {
        if (utteranceId == null) return
        if (!activeUtteranceId.compareAndSet(utteranceId, null)) return
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    fun speakNow(text: String, engine: TextToSpeech) {
        val now = System.currentTimeMillis()
        val isSameTextRecently = lastSpokenText.get() == text && now - lastSpokenAt.get() < 800L
        if (isSameTextRecently) return
        lastSpokenText.set(text)
        lastSpokenAt.set(now)
        engine.stop()
        val focusResult = audioManager.requestAudioFocus(focusRequest)
        if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
        val utteranceId = "utt-${System.nanoTime()}"
        activeUtteranceId.set(utteranceId)
        val params = android.os.Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, TTS_MAX_VOLUME)
        }
        val speakResult = engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        if (speakResult == TextToSpeech.ERROR) {
            finishSpeech(utteranceId)
        }
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
        val ttsRef = AtomicReference<TextToSpeech?>(null)
        val initSucceeded = AtomicBoolean(false)
        val instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                initSucceeded.set(true)
                mainHandler.post {
                    ttsRef.get()?.let(::configureTts)
                }
            }
        }
        ttsRef.set(instance)
        instance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                mainHandler.post { finishSpeech(utteranceId) }
            }

            @Deprecated("Deprecated by Android SDK")
            override fun onError(utteranceId: String?) {
                mainHandler.post { finishSpeech(utteranceId) }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post { finishSpeech(utteranceId) }
            }
        })
        tts = instance
        // Some TTS engines can invoke the init callback before TextToSpeech()
        // returns; this second path configures the instance after the reference is set.
        if (initSucceeded.get()) {
            mainHandler.post { configureTts(instance) }
        }
        onDispose {
            isReady = false
            isTtsConfigured.set(false)
            pendingSpeechText.set(null)
            instance.stop()
            instance.shutdown()
            activeUtteranceId.set(null)
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

private const val AUTO_SPEAK_DELAY_MILLIS = 150L
private const val TTS_MAX_VOLUME = 1.0f
