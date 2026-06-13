package com.example.vocabapp.ui.screen.common

import androidx.annotation.DrawableRes
import com.example.vocabapp.R

/** Robot mascot behaviors. One PNG today; [mascotDrawable] is the single swap point for future pose images. */
enum class MascotMood { Idle, Wave, Thinking, Cheer, Point }

/** Per-mood pose image. All point at robota_mascot until per-pose art exists — change here only. */
@DrawableRes
fun mascotDrawable(mood: MascotMood): Int = when (mood) {
    MascotMood.Idle -> R.drawable.robota_mascot
    MascotMood.Wave -> R.drawable.robota_mascot
    MascotMood.Thinking -> R.drawable.robota_mascot
    MascotMood.Cheer -> R.drawable.robota_mascot
    MascotMood.Point -> R.drawable.robota_mascot
}

/** Transform parameters for the infinite mascot animation. */
data class MascotMotion(
    val bobFrom: Float,
    val bobTo: Float,
    val bobDurationMillis: Int,
    val rotateFrom: Float,
    val rotateTo: Float,
    val rotateDurationMillis: Int,
    val minScale: Float,
    val maxScale: Float,
    val scaleDurationMillis: Int,
    val showConfetti: Boolean,
)

fun mascotMotionFor(mood: MascotMood): MascotMotion = when (mood) {
    MascotMood.Idle -> MascotMotion(-4f, 6f, 1300, -3f, 3f, 1500, 0.96f, 1.02f, 1200, false)
    MascotMood.Wave -> MascotMotion(-4f, 6f, 1300, -6f, 9f, 640, 0.97f, 1.03f, 1200, false)
    MascotMood.Thinking -> MascotMotion(-3f, 4f, 1700, -2f, 4f, 1800, 0.97f, 1.02f, 1500, false)
    MascotMood.Cheer -> MascotMotion(-8f, 4f, 520, -6f, 6f, 520, 0.94f, 1.10f, 520, true)
    MascotMood.Point -> MascotMotion(-3f, 3f, 1100, 2f, 8f, 900, 0.98f, 1.02f, 1100, false)
}
