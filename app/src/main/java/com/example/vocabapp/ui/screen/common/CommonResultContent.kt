package com.example.vocabapp

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.QuizResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun ResultContent(result: QuizResult, modifier: Modifier, onRetry: () -> Unit, onHome: () -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    val isPerfect = result.correctCount == result.totalQuestions
    val resId = medalResId(result.correctCount, result.totalQuestions)
    val title = medalTitle(result.correctCount, result.totalQuestions)
    val message = medalMessage(result.correctCount, result.totalQuestions)
    val soundPlayer = rememberSoundPlayer()

    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    var medalVisible by remember { mutableStateOf(false) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }
    val medalPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val perfectPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

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
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val medalFocusReq = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build())
            .setOnAudioFocusChangeListener {}.build()
        try {
            am.requestAudioFocus(medalFocusReq)
            val mp = MediaPlayer.create(context, R.raw.new_medal_sound)
            mp?.setVolume(1f, 1f)
            mp?.setOnCompletionListener {
                it.release()
                medalPlayer.value = null
                am.abandonAudioFocusRequest(medalFocusReq)
            }
            mp?.start()
            medalPlayer.value = mp
        } catch (_: Exception) { am.abandonAudioFocusRequest(medalFocusReq) }
        if (isPerfect) {
            try {
                val mp = MediaPlayer.create(context, R.raw.perfect_native_male)
                mp?.setOnCompletionListener { it.release(); perfectPlayer.value = null }
                mp?.start()
                perfectPlayer.value = mp
            } catch (_: Exception) {}
        }
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
            perfectPlayer.value?.let { mp ->
                if (mp.isPlaying) mp.stop()
                mp.release()
                perfectPlayer.value = null
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(BrightBlue)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ResultSectionCard(header = "正解率") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val medalSize = if (isPerfect) 110.dp else 160.dp
                        val resultMedalMod = when {
                            isPerfect -> Modifier.size(medalSize)
                            resId == R.drawable.medal_bronze -> Modifier.size(width = 107.dp, height = 160.dp)
                            else -> Modifier.size(medalSize)
                        }
                        if (medalVisible) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = if (isPerfect) "パーフェクトメダル" else null,
                                modifier = resultMedalMod.scale(medalScale.value).alpha(medalAlpha.value),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Spacer(resultMedalMod)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${result.correctCount}/${result.totalQuestions}正解", color = TextMuted, fontSize = 16.sp)
                            Text(
                                "$displayedAccuracy%",
                                color = if (isPerfect) Gold else DeepBlue,
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 58.sp
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { animProgress.value },
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp).height(14.dp).clip(RoundedCornerShape(7.dp)),
                        color = Teal,
                        trackColor = Color(0xFFDDE5EC)
                    )
                }
            }

            item {
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
            }

            item {
                ResultSectionCard(header = "学習状況") {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("正解 / 不正解", color = TextDark, fontWeight = FontWeight.Bold)
                            }
                            Text("${result.correctCount} / ${result.wrongCount}", color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                        androidx.compose.material3.HorizontalDivider(color = SoftBlue)
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("今回の学習時間", color = TextDark, fontWeight = FontWeight.Bold)
                            }
                            Text(formatStudyTime(result.studySeconds), color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            if (result.wrongWords.isNotEmpty()) {
                item {
                    Text("間違えた単語 (${result.wrongWords.size}語)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                }
                items(result.wrongWords) { word ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Danger.copy(alpha = 0.4f))
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Danger, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(word.english, color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Text(word.meaning, color = TextMuted, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f).height(54.dp)) {
                Text("再チャレンジ", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("次へ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

