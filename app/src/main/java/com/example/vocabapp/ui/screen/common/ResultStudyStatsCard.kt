package com.example.vocabapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.domain.model.QuizResult

@Composable
internal fun ResultStudyStatsCard(result: QuizResult) {
    ResultSectionCard(header = "学習状況") {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("正解 / 不正解", color = TextDark, fontWeight = FontWeight.Bold)
                }
                Text(
                    "${result.correctCount} / ${result.wrongCount}",
                    color = DeepBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }
            HorizontalDivider(color = SoftBlue)
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("今回の学習時間", color = TextDark, fontWeight = FontWeight.Bold)
                }
                Text(formatStudyTime(result.studySeconds), color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
internal fun ResultWrongWordCard(english: String, meaning: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(2.dp, Danger.copy(alpha = 0.4f))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Danger, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(english, color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(meaning, color = TextMuted, fontSize = 14.sp)
            }
        }
    }
}

