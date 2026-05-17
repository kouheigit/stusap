package com.example.vocabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ImportSummaryCard(title: String, totalRows: Int, newCount: Int, newIdiomCount: Int, duplicateCount: Int, errorCount: Int) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryChip("読み込み", "${totalRows}件", Modifier.weight(1f))
                SummaryChip("単語登録", "${newCount}件", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryChip("熟語登録", "${newIdiomCount}件", Modifier.weight(1f))
                SummaryChip("重複", "${duplicateCount}件", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryChip("エラー", "${errorCount}件", Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun SummaryChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(SoftBlue, RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun ImportWordRow(english: String, meaning: String, type: String) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(english, color = DeepBlue, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(meaning, color = TextDark, fontSize = 15.sp)
            }
            Text(if (type == "phrase") "熟語" else "単語", color = BrightBlue, fontWeight = FontWeight.Bold)
        }
    }
}
