package com.example.vocabapp.ui.screen.common

import org.junit.Assert.assertEquals
import org.junit.Test

class CommonAudioTest {

    @Test
    fun buildSpeechText_keepsShortPhrasesAsIs() {
        assertEquals("look up", buildSpeechText(" look  up "))
    }

    @Test
    fun buildSpeechText_separatesLongerIdiomWordsWithPauses() {
        assertEquals("take, care, of", buildSpeechText("take care of"))
    }

    @Test
    fun buildSpeechText_normalizesSymbolsBeforeSpeaking() {
        assertEquals("back, up, and, move, on", buildSpeechText("back-up & move-on"))
    }
}
