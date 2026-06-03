package com.example.vocabapp.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer

/**
 * raw リソースの効果音再生とオーディオフォーカス管理をまとめた専用プレイヤー。
 * 低レベルな [MediaPlayer] / [AudioManager] / [AudioFocusRequest] の制御を UI 層から切り離す。
 */
internal class MediaSoundPlayer(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val activePlayers = mutableListOf<MediaPlayer>()

    private val focusRequest: AudioFocusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build()
            )
            .setOnAudioFocusChangeListener {}
            .build()
    }

    /**
     * [resId] の音源を再生する。[requestFocus] が true のときのみフォーカスを取得し、
     * 再生完了・失敗時に必ず手放す。
     */
    fun play(resId: Int, requestFocus: Boolean = true) {
        if (requestFocus) audioManager.requestAudioFocus(focusRequest)
        try {
            val player = MediaPlayer.create(appContext, resId)
            if (player == null) {
                warnAudioPlayback("MediaPlayer.create returned null for raw resource: $resId")
                if (requestFocus) audioManager.abandonAudioFocusRequest(focusRequest)
                return
            }
            synchronized(activePlayers) { activePlayers.add(player) }
            player.setOnCompletionListener {
                it.release()
                synchronized(activePlayers) { activePlayers.remove(it) }
                if (requestFocus) audioManager.abandonAudioFocusRequest(focusRequest)
            }
            player.start()
        } catch (error: Exception) {
            warnAudioPlayback("Failed to play raw audio resource: $resId", error)
            if (requestFocus) audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }

    fun dispose() {
        synchronized(activePlayers) {
            activePlayers.forEach { player ->
                player.runCatching {
                    if (isPlaying) stop()
                    release()
                }
            }
            activePlayers.clear()
        }
        audioManager.abandonAudioFocusRequest(focusRequest)
    }
}
