package com.example.vocabapp.ui.screen.quiz

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.vocabapp.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.ui.theme.AccentBlue
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.Gold
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.Success
import com.example.vocabapp.ui.theme.TextDark
import com.example.vocabapp.ui.theme.TextMuted
import com.example.vocabapp.data.repository.CAPACITY_LOW_THRESHOLD

@Composable
internal fun SentenceFileNameCard(fileName: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DeepBlue.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(18.dp))
            Text(
                fileName,
                color = DeepBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun SentenceCapacityChip(remaining: Int) {
    val isLow = remaining < CAPACITY_LOW_THRESHOLD
    val chipColor = if (isLow) Danger.copy(alpha = 0.12f) else Success.copy(alpha = 0.12f)
    val textColor = if (isLow) Danger else Success
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = chipColor)) {
        Text(
            if (isLow) "残り${remaining}件（上限に近づいています）" else "残り${remaining}件登録可能",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
internal fun SentenceImportFormatCard() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.sentence_import_format_title), color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("ヘッダーなしの場合は、1列目を英文、2列目を意味として読み込みます。", color = TextDark, fontSize = 14.sp)
            CsvFormatExampleCard()
            SentenceFormatHint("A型", AccentBlue, "6語以上でそのまま入力 → 自動で4語が空白に")
            SentenceFormatHint("B型", Gold, "[語句]で4つを囲む → その語句が並べ替え対象")
            HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
            Text("対応ヘッダー名: sentence / english / 英文 / 文章 / 例文 / 英語", color = TextMuted, fontSize = 11.sp)
            Text("意味列: meaning / 日本語の意味 / 意味 / 訳 / 和訳 / 日本語", color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
internal fun SentenceImportLoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(color = BrightBlue)
        Spacer(Modifier.width(12.dp))
        Text(stringResource(R.string.sentence_import_loading_text), color = TextDark, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun SentenceImportSuccessCard(insertedCount: Int, quizReadyCount: Int, onViewList: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Success),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.sentence_import_success_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.sentence_import_success_detail, insertedCount, quizReadyCount), color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
            TextButton(onClick = onViewList, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color.White)) {
                Text(stringResource(R.string.sentence_import_view_list), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
internal fun SentenceImportSummaryCard(
    title: String,
    totalRows: Int,
    newCount: Int,
    duplicateCount: Int,
    errorCount: Int,
    quizReadyCount: Int?,
    quizIncompatibleCount: Int?
) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
            SummaryRow("読み込み", "${totalRows}件", "文章登録", "${newCount}件")
            SummaryRow("重複", "${duplicateCount}件", "エラー", "${errorCount}件")
            if (quizReadyCount != null && quizIncompatibleCount != null && newCount > 0) {
                SummaryRow("クイズ対応", "${quizReadyCount}件 (${quizReadyCount * 100 / newCount}%)", "対応外", "${quizIncompatibleCount}件")
            }
        }
    }
}

@Composable
internal fun SentenceQuizIncompatibleWarning(count: Int) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "${count}件はクイズ非対応（語数不足 or [語句]数が4以外）。登録はできますがクイズには出題されません。",
            color = Gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun CsvFormatExampleCard() {
    Card(shape = RoundedCornerShape(6.dp), colors = CardDefaults.cardColors(containerColor = SoftBlue), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("CSVフォーマット例", color = DeepBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("sentence,meaning", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("I might stay as well as join a tour,ツアーに参加するよりも家にいた方がいい", color = TextDark, fontSize = 11.sp)
            Text("I [might][stay][as][well] as join a tour,ツアーに...", color = TextDark, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SentenceFormatHint(label: String, color: Color, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        SentenceTypeBadge(label, color)
        Text(text, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun SummaryRow(firstLabel: String, firstValue: String, secondLabel: String, secondValue: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        SummaryChip(firstLabel, firstValue, Modifier.weight(1f))
        SummaryChip(secondLabel, secondValue, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = SoftBlue), modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(value, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}
