package com.example.vocabapp.ui.screen.common

import com.example.vocabapp.ui.theme.Teal

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.SoftBlue

import com.example.vocabapp.ui.theme.AccentBlue

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.theme.DeepBlue

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.domain.model.LessonStatus
import com.example.vocabapp.domain.model.Training

@Composable
internal fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(32.dp))
                    Text("${lesson.wordStartNumber}〜${lesson.wordEndNumber}語", color = DeepBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
                Text("学習状態  ${lesson.status.label()}", color = TextMuted, fontSize = 16.sp)
                Text("学習日  ${lesson.lastStudiedAt?.let(::formatDate) ?: "-"}", color = TextMuted, fontSize = 16.sp)
                LinearProgressIndicator(progress = { lesson.progressRate }, modifier = Modifier.fillMaxWidth(0.72f).height(8.dp).clip(RoundedCornerShape(4.dp)), color = Teal, trackColor = SoftBlue)
            }
            MasterBadge(isMaster = lesson.status == LessonStatus.Master)
        }
    }
}

@Composable
internal fun TrainingCard(training: Training, onQuiz: () -> Unit, onDetail: (Int) -> Unit, onFlashcard: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onQuiz),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(training.title, color = TextMuted, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(32.dp))
                    Text("${training.wordStartNumber}〜${training.wordEndNumber}語", color = DeepBlue, fontSize = 28.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                }
                Text("学習回数  ${training.studyCount}", color = TextMuted)
                Text("学習日  ${training.lastStudiedAt?.let(::formatDate) ?: "-"}", color = TextMuted)
                if (training.studyCount > 0) {
                    Text("ベスト  ${training.bestAccuracy.toInt()}%", color = TextMuted)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(3) { index ->
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFDDE5EC), modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("未挑戦", color = TextMuted)
                    }
                }
            }
            if (training.studyCount > 0) {
                val effectiveAccuracy = if (training.lastAccuracy > 0f) training.lastAccuracy else training.bestAccuracy
                val currentMedalResId = lastMedalResId(effectiveAccuracy)
                val isBronze = currentMedalResId == R.drawable.medal_bronze
                val medalMod = if (isBronze)
                    Modifier.size(width = 112.dp, height = 150.dp).clickable { onQuiz() }
                else
                    Modifier.size(112.dp).clickable { onQuiz() }
                Image(
                    painter = painterResource(currentMedalResId),
                    contentDescription = null,
                    modifier = medalMod,
                    contentScale = ContentScale.Fit
                )
            } else {
                Button(onClick = onQuiz, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue), shape = CircleShape, modifier = Modifier.size(86.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text("開始", fontSize = 12.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Text(
                "単語帳で学習",
                modifier = Modifier.weight(1f).clickable { onFlashcard() }.padding(bottom = 14.dp),
                textAlign = TextAlign.Center,
                color = AccentBlue,
                fontWeight = FontWeight.Bold
            )
            Text(
                "先頭単語の詳細を見る",
                modifier = Modifier.weight(1f).clickable { onDetail(training.firstWordId) }.padding(bottom = 14.dp),
                textAlign = TextAlign.Center,
                color = BrightBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

