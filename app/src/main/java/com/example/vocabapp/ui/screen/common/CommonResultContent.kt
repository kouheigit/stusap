package com.example.vocabapp.ui.screen.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.QuizResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun ResultContent(result: QuizResult, modifier: Modifier, onRetry: () -> Unit, onHome: () -> Unit, onNext: () -> Unit) {
    val isPerfect = result.correctCount == result.totalQuestions
    val resId = medalResId(result.correctCount, result.totalQuestions)
    val title = medalTitle(result.correctCount, result.totalQuestions)
    val message = medalMessage(result.correctCount, result.totalQuestions)
    val soundPlayer = rememberSoundPlayer()
    val mediaSoundPlayer = rememberMediaSoundPlayer()

    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    var medalVisible by remember { mutableStateOf(false) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val animDuration = 1800
        soundPlayer.playSequence(
            listOf(Pair(440f, 150), Pair(523f, 150), Pair(659f, 200), Pair(784f, 250), Pair(1047f, 350)),
            false
        )
        launch {
            animProgress.animateTo(
                targetValue = (result.accuracy / 100f).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = animDuration, easing = LinearEasing)
            )
        }
        val finalAcc = result.accuracy.toInt()
        val startTime = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed >= animDuration) { displayedAccuracy = finalAcc; break }
            displayedAccuracy = ((elapsed.toFloat() / animDuration) * finalAcc).toInt()
            delay(16L)
        }
        delay(200L)
        medalVisible = true
        mediaSoundPlayer.play(R.raw.new_medal_sound)
        if (isPerfect) {
            mediaSoundPlayer.play(R.raw.perfect_native_male, requestFocus = false)
        }
        launch { medalAlpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing)) }
        medalScale.animateTo(1.15f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        medalScale.animateTo(0.95f, animationSpec = tween(120))
        medalScale.animateTo(1f, animationSpec = tween(100))
    }

    ResultContentBody(
        result = result,
        modifier = modifier,
        title = title,
        message = message,
        resId = resId,
        isPerfect = isPerfect,
        displayedAccuracy = displayedAccuracy,
        medalVisible = medalVisible,
        medalScale = medalScale.value,
        medalAlpha = medalAlpha.value,
        progress = animProgress.value,
        onRetry = onRetry,
        onNext = onNext
    )
}
