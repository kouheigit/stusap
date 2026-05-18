package com.example.vocabapp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.domain.model.SentenceQuizResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun SentenceResultContent(
    result: SentenceQuizResult,
    modifier: Modifier,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    onMenu: () -> Unit
) {
    val isPerfect = result.correctCount == result.totalQuestions
    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }
    var medalVisible by remember { mutableStateOf(false) }
    val soundPlayer = rememberSoundPlayer()

    LaunchedEffect(Unit) {
        val animDuration = 1200
        soundPlayer.playSequence(listOf(Pair(440f, 150), Pair(523f, 150), Pair(659f, 200), Pair(784f, 250)), false)
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
        launch { medalAlpha.animateTo(1f, animationSpec = tween(300)) }
        medalScale.animateTo(1.1f, animationSpec = tween(280))
        medalScale.animateTo(0.95f, animationSpec = tween(100))
        medalScale.animateTo(1f, animationSpec = tween(100))
    }

    Column(modifier.fillMaxSize().background(BrightBlue)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("文章問題 結果", color = TextMuted, fontSize = 15.sp)
                        if (isPerfect && medalVisible) {
                            Text(
                                "🎉 全問正解！",
                                color = Gold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.alpha(medalAlpha.value)
                            )
                        }
                        if (medalVisible) {
                            Text(
                                "$displayedAccuracy%",
                                color = if (isPerfect) Gold else DeepBlue,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.scale(medalScale.value).alpha(medalAlpha.value)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { animProgress.value },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color = Teal,
                            trackColor = Color(0xFFDDE5EC)
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) { idx ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (idx < result.starCount) Gold else TextMuted.copy(alpha = 0.3f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            val rankLabel = when (result.starCount) {
                                3 -> "Excellent!"
                                2 -> "Good!"
                                1 -> "Keep trying!"
                                else -> "Practice more"
                            }
                            Text(rankLabel, color = if (result.starCount >= 2) Success else TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("正解", color = TextMuted, fontSize = 13.sp)
                                Text("${result.correctCount}", color = Success, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("不正解", color = TextMuted, fontSize = 13.sp)
                                Text("${result.wrongCount}", color = Danger, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("問題数", color = TextMuted, fontSize = 13.sp)
                                Text("${result.totalQuestions}", color = TextDark, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
                        val mins = result.studySeconds / 60
                        val secs = result.studySeconds % 60
                        val timeStr = if (mins > 0) "${mins}分${secs}秒" else "${secs}秒"
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("学習時間: $timeStr", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("もう一度", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("文章問題メニュー", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onHome,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("ホーム", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
