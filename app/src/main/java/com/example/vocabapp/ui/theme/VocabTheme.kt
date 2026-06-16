package com.example.vocabapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette extracted pixel-strict from GramCraft comps (see design_progress.md §2).
internal val DeepBlue = Color(0xFF1E3A2C)        // dark heading/body text (was #123F34)
internal val BrightBlue = Color(0xFF21A357)      // primary green (was #58CC02 yellow-green — wrong hue)
internal val HeaderGreen = Color(0xFF1A9A57)     // TopAppBar background green
internal val AccentBlue = Color(0xFF0E9CE6)      // info / accuracy %
internal val SoftBlue = Color(0xFFF4FBF6)        // mint screen background
internal val TextDark = Color(0xFF1E3A2C)
internal val TextMuted = Color(0xFF7A8C84)
internal val Success = Color(0xFF21A357)
internal val Danger = Color(0xFFFF5B71)
internal val Gold = Color(0xFFFFC83D)
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
