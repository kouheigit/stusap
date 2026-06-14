package com.example.vocabapp.ui.screen.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.vocabapp.ui.theme.AccentBlue
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.Gold
import kotlin.random.Random

private data class ConfettiPiece(val xRatio: Float, val color: Color, val size: Float, val phase: Float)

/** Lightweight celebratory confetti. Decorative only; place behind interactive content so taps pass through. */
@Composable
internal fun ConfettiOverlay(modifier: Modifier = Modifier, pieceCount: Int = 26) {
    val palette = listOf(BrightBlue, Gold, AccentBlue, Danger)
    val pieces = remember {
        List(pieceCount) {
            ConfettiPiece(
                xRatio = Random.nextFloat(),
                color = palette[it % palette.size],
                size = 6f + Random.nextFloat() * 8f,
                phase = Random.nextFloat()
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "confettiT"
    )
    Canvas(modifier) {
        pieces.forEach { p ->
            val progress = (t + p.phase) % 1f
            val y = progress * size.height
            val x = p.xRatio * size.width
            drawRect(
                color = p.color.copy(alpha = 1f - progress),
                topLeft = Offset(x, y),
                size = Size(p.size, p.size * 1.6f)
            )
        }
    }
}
