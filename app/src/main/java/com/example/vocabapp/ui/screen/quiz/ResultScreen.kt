package com.example.vocabapp

import com.example.vocabapp.ui.theme.Teal

import com.example.vocabapp.ui.theme.Gold

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.AccentBlue

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.theme.DeepBlue

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.viewmodel.ResultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
internal fun ResultScreen(navController: NavHostController, viewModel: ResultViewModel = hiltViewModel()) {
    val result by viewModel.result.collectAsStateWithLifecycle()
    val trainingLabel by viewModel.trainingLabel.collectAsStateWithLifecycle()
    val title = trainingLabel ?: "クイズ結果"
    BlueScaffold(title = title, onBack = { navController.navigate(Route.Home.path) }) { inner ->
        result?.let {
            ResultContent(
                result = it,
                modifier = Modifier.padding(inner),
                onRetry = { navController.navigate(retryRouteFor(it)) },
                onHome = { navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } },
                onNext = { navController.navigate(nextRouteFor(it)) }
            )
        } ?: Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

internal fun nextRouteFor(result: QuizResult): String {
    val trainingId = result.trainingId ?: return Route.Review.path
    return when {
        trainingId == RANDOM_CUSTOM_WORD_TRAINING_ID -> Route.customTraining(ContentType.WORD.routeValue)
        trainingId == RANDOM_CUSTOM_IDIOM_TRAINING_ID -> Route.customTraining(ContentType.IDIOM.routeValue)
        trainingId < CUSTOM_IDIOM_LESSON_ID -> Route.customTraining(ContentType.IDIOM.routeValue)
        trainingId < CUSTOM_WORD_LESSON_ID -> Route.customTraining(ContentType.WORD.routeValue)
        trainingId >= 100 -> Route.IdiomLessons.path
        else -> Route.Lessons.path
    }
}

internal fun retryRouteFor(result: QuizResult): String {
    val trainingId = result.trainingId ?: return Route.quiz(isReview = result.isReview)
    return when {
        trainingId == RANDOM_CUSTOM_WORD_TRAINING_ID -> Route.randomCustomQuiz(ContentType.WORD.routeValue)
        trainingId == RANDOM_CUSTOM_IDIOM_TRAINING_ID -> Route.randomCustomQuiz(ContentType.IDIOM.routeValue)
        trainingId < CUSTOM_IDIOM_LESSON_ID -> {
            Route.customTrainingQuiz(ContentType.IDIOM.routeValue, CUSTOM_IDIOM_LESSON_ID - trainingId)
        }
        trainingId < CUSTOM_WORD_LESSON_ID -> {
            Route.customTrainingQuiz(ContentType.WORD.routeValue, CUSTOM_WORD_LESSON_ID - trainingId)
        }
        else -> Route.quiz(trainingId, result.isReview)
    }
}

private const val CUSTOM_WORD_LESSON_ID = -10_000
private const val CUSTOM_IDIOM_LESSON_ID = -20_000
private const val RANDOM_TRAINING_OFFSET = 999
private const val RANDOM_CUSTOM_WORD_TRAINING_ID = CUSTOM_WORD_LESSON_ID - RANDOM_TRAINING_OFFSET
private const val RANDOM_CUSTOM_IDIOM_TRAINING_ID = CUSTOM_IDIOM_LESSON_ID - RANDOM_TRAINING_OFFSET

@Composable
internal fun CustomWordQuizResultContent(
    correctCount: Int,
    total: Int,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accuracy = correctCount * 100f / total
    val isPerfect = correctCount == total
    val resId = medalResId(correctCount, total)
    val title = medalTitle(correctCount, total)
    val message = medalMessage(correctCount, total)

    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    var medalVisible by remember { mutableStateOf(false) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }
    val medalPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val context = LocalContext.current
    val soundPlayer = rememberSoundPlayer()

    LaunchedEffect(Unit) {
        val animDuration = 1800
        soundPlayer.playSequence(
            listOf(Pair(330f, 120), Pair(440f, 130), Pair(523f, 140), Pair(659f, 160), Pair(784f, 200), Pair(1047f, 280)),
            false
        )
        launch {
            animProgress.animateTo(
                targetValue = (accuracy / 100f).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = animDuration, easing = LinearEasing)
            )
        }
        val finalAcc = accuracy.toInt()
        val startTime = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed >= animDuration) { displayedAccuracy = finalAcc; break }
            displayedAccuracy = ((elapsed.toFloat() / animDuration) * finalAcc).toInt()
            delay(16L)
        }
        delay(200L)
        medalVisible = true
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val req = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build())
                .setOnAudioFocusChangeListener {}.build()
            am.requestAudioFocus(req)
            val mp = MediaPlayer.create(context, R.raw.new_medal_sound)
            mp?.setVolume(1f, 1f)
            mp?.setOnCompletionListener {
                it.release()
                medalPlayer.value = null
                am.abandonAudioFocusRequest(req)
            }
            mp?.start()
            medalPlayer.value = mp
        } catch (_: Exception) {}
        launch { medalAlpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing)) }
        medalScale.animateTo(1.15f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        medalScale.animateTo(0.95f, animationSpec = tween(120))
        medalScale.animateTo(1f, animationSpec = tween(100))
    }

    DisposableEffect(Unit) {
        onDispose {
            medalPlayer.value?.let { mp ->
                if (mp.isPlaying) mp.stop()
                mp.release()
                medalPlayer.value = null
            }
        }
    }

    Column(
        modifier = modifier.background(BrightBlue).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ResultSectionCard(header = "正解率") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val medalMod = when {
                    isPerfect -> Modifier.size(110.dp)
                    resId == R.drawable.medal_bronze -> Modifier.size(width = 107.dp, height = 160.dp)
                    else -> Modifier.size(160.dp)
                }
                if (medalVisible) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        modifier = medalMod.scale(medalScale.value).alpha(medalAlpha.value),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Spacer(medalMod)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.result_correct_summary, correctCount, total),
                        color = TextMuted,
                        fontSize = 16.sp
                    )
                    Text(
                        stringResource(R.string.result_accuracy, displayedAccuracy),
                        color = if (isPerfect) Gold else DeepBlue,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 58.sp
                    )
                }
            }
            LinearProgressIndicator(
                progress = { animProgress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp)),
                color = Teal,
                trackColor = Color(0xFFDDE5EC)
            )
        }
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(title, color = DeepBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(message, color = TextMuted, textAlign = TextAlign.Center, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.result_retry), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text(stringResource(R.string.result_home))
        }
    }
}
