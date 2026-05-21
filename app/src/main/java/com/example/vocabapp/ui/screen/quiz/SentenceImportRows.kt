package com.example.vocabapp.ui.screen.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.ui.theme.AccentBlue
import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.Gold
import com.example.vocabapp.ui.theme.Success
import com.example.vocabapp.ui.theme.TextDark
import com.example.vocabapp.ui.theme.TextMuted
import com.example.vocabapp.data.repository.isQuizReadySentence
import com.example.vocabapp.data.repository.sentenceType
import com.example.vocabapp.domain.model.ImportErrorRow
import com.example.vocabapp.domain.model.ImportedSentence

@Composable
internal fun SentenceImportRow(sentence: ImportedSentence) {
    val quizReady = sentence.sentence.isQuizReadySentence()
    val type = sentence.sentence.sentenceType()
    val badgeColor = if (type == "B") Gold else AccentBlue
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(sentence.sentence, color = DeepBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(sentence.meaning, color = TextDark, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                SentenceTypeBadge("${type}型", badgeColor)
                SentenceTypeBadge(if (quizReady) "クイズ対応" else "語数不足", if (quizReady) Success else Danger)
            }
        }
    }
}

@Composable
internal fun SentenceImportErrorRow(error: ImportErrorRow) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Danger.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                SentenceTypeBadge("${error.rowNumber}行", Danger)
                Text("エラー", color = Danger, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(error.reason, color = TextDark, fontSize = 13.sp)
            if (error.rawValues.isNotEmpty()) {
                Text(error.rawValues.toSentenceMaskedPreview(), color = TextMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun ImportSectionTitle(text: String, color: Color) {
    Text(text, color = color, fontSize = 24.sp, fontWeight = FontWeight.Black)
}

@Composable
internal fun OmittedRowsText(text: String, color: Color) {
    Text(text, color = color.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
}

@Composable
internal fun SentenceTypeBadge(label: String, color: Color) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Text(
            label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun List<String>.toSentenceMaskedPreview(): String =
    take(4).joinToString(", ") { value ->
        val trimmed = value.trim()
        when {
            trimmed.isBlank() -> "(空)"
            trimmed.length <= 8 -> "${trimmed.take(2)}..."
            else -> "${trimmed.take(4)}..."
        }
    }
