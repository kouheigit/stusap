package com.example.vocabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.LessonStatus
import com.example.vocabapp.domain.model.Word
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun WordRow(word: Word, action: @Composable () -> Unit, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(word.english, color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("${word.meaning}  ${word.phonetic}", color = TextMuted)
                Text(word.partOfSpeech, color = TextMuted)
            }
            action()
        }
    }
}

@Composable
internal fun MasterBadge(isMaster: Boolean) {
    Box(
        modifier = Modifier.size(92.dp).clip(CircleShape).background(if (isMaster) Gold else Color(0xFFEAF1F7)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            Text(if (isMaster) "Master" else "Start", color = if (isMaster) Color.White else TextMuted, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
internal fun ResultSectionCard(header: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(SoftBlue).padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(header, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            content()
        }
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(text, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
}

@Composable
internal fun EmptyCard(text: String) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Text(text, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(28.dp))
    }
}

@Composable
internal fun EmptyMessage(modifier: Modifier, title: String, button: String, onClick: () -> Unit) {
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        EmptyCard(title)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onClick) { Text(button) }
    }
}

internal fun LessonStatus.label(): String = when (this) {
    LessonStatus.NotStarted -> "未学習"
    LessonStatus.InProgress -> "学習中"
    LessonStatus.Complete -> "完了"
    LessonStatus.Master -> "Master"
}

internal fun medalResId(correctCount: Int, totalQuestions: Int): Int {
    if (totalQuestions == 0) return R.drawable.medal_bronze
    val accuracy = correctCount * 100f / totalQuestions
    return when {
        correctCount == totalQuestions -> R.drawable.medal_perfect
        accuracy >= 80f -> R.drawable.medal_gold
        accuracy >= 50f -> R.drawable.medal_silver
        else -> R.drawable.medal_bronze
    }
}

internal fun lastMedalResId(lastAccuracy: Float): Int = when {
    lastAccuracy >= 100f -> R.drawable.medal_perfect
    lastAccuracy >= 80f -> R.drawable.medal_gold
    lastAccuracy >= 50f -> R.drawable.medal_silver
    else -> R.drawable.medal_bronze
}

internal fun medalTitle(correctCount: Int, totalQuestions: Int): String = when {
    correctCount == totalQuestions -> "Perfect！"
    correctCount >= 8 -> "Excellent！"
    correctCount >= 5 -> "Good！"
    else -> "Keep trying！"
}

internal fun medalMessage(correctCount: Int, totalQuestions: Int): String = when {
    correctCount == totalQuestions -> "頑張りましたね！おめでとうございます。\nトレーニングをマスターしました！"
    correctCount >= 8 -> "もう少しでパーフェクト！\nこの調子で頑張りましょう！"
    correctCount >= 5 -> "いい調子です！\n焦らず、コツコツ続けることが大事です！努力は嘘をつかないです！"
    else -> "諦めない事が肝心です！\nただひたすらに頑張れば、結果はおのずとついてきます！"
}

internal fun formatDate(millis: Long): String =
    SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN).format(Date(millis))

internal fun formatSeconds(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val hours = minutes / 60
    val remainMinutes = minutes % 60
    return if (hours > 0) "${hours}時間${remainMinutes}分" else "${minutes}分"
}

internal fun formatStudyTime(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    return if (safeSeconds < 60) "${safeSeconds}秒" else formatSeconds(safeSeconds)
}

internal val AddWordCardPadding = 24.dp
internal val AddWordCardSpacing = 20.dp

