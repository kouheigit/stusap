package com.example.vocabapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val DeepBlue = Color(0xFF0D47A1)
internal val BrightBlue = Color(0xFF1E88E5)
internal val AccentBlue = Color(0xFF03A9E6)
internal val SoftBlue = Color(0xFFE3F2FD)
internal val TextDark = Color(0xFF17203C)
internal val TextMuted = Color(0xFF7B8A95)
internal val Success = Color(0xFF22A852)
internal val Danger = Color(0xFFE5395A)
internal val Gold = Color(0xFFFFC943)
internal val Teal = Color(0xFF41C7BE)

@Composable
internal fun VocabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = BrightBlue,
            secondary = Teal,
            background = Color.White,
            surface = Color.White,
            error = Danger
        ),
        content = content
    )
}
