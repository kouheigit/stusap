package com.example.vocabapp.ui.screen.common

/** Maps a 0-100 score to a TEPPEN-style letter grade for the result screen badge. */
fun gradeLabel(scoreOutOf100: Int): String = when {
    scoreOutOf100 >= 100 -> "S"
    scoreOutOf100 >= 80 -> "A"
    scoreOutOf100 >= 60 -> "B"
    scoreOutOf100 >= 40 -> "C"
    else -> "D"
}
