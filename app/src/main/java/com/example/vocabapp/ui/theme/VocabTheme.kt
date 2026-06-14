package com.example.vocabapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val DeepBlue = Color(0xFF123F34)
internal val BrightBlue = Color(0xFF58CC02)
internal val AccentBlue = Color(0xFF1CB0F6)
internal val SoftBlue = Color(0xFFEAF8EF)
internal val TextDark = Color(0xFF18312A)
internal val TextMuted = Color(0xFF6F837C)
internal val Success = Color(0xFF58CC02)
internal val Danger = Color(0xFFE5395A)
internal val Gold = Color(0xFFFFC943)
internal val Teal = Color(0xFF00C2A8)
internal val PassagePaperInk = Color(0xFF202020)
internal val PassagePaperBorder = Color(0xFF303030)
internal val PassageEmailChrome = Color(0xFFC7C7C7)
internal val PassageEmailBorder = Color(0xFF555555)
internal val PassageEmailField = Color(0xFFD9D9D9)
internal val PassageEmailFieldBorder = Color(0xFF777777)

@Composable
internal fun VocabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = BrightBlue,
            secondary = Teal,
            background = SoftBlue,
            surface = Color.White,
            error = Danger
        ),
        content = content
    )
}
