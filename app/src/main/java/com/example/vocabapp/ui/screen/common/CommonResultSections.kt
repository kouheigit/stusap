package com.example.vocabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.domain.model.QuizResult

@Composable
internal fun ResultContentBody(
    result: QuizResult,
    modifier: Modifier,
    title: String,
    message: String,
    resId: Int,
    isPerfect: Boolean,
    displayedAccuracy: Int,
    medalVisible: Boolean,
    medalScale: Float,
    medalAlpha: Float,
    progress: Float,
    onRetry: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize().background(BrightBlue)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ResultAccuracyCard(
                    result = result,
                    resId = resId,
                    isPerfect = isPerfect,
                    displayedAccuracy = displayedAccuracy,
                    medalVisible = medalVisible,
                    medalScale = medalScale,
                    medalAlpha = medalAlpha,
                    progress = progress
                )
            }
            item { ResultMessageCard(title = title, message = message) }
            item { ResultStudyStatsCard(result = result) }
            if (result.wrongWords.isNotEmpty()) {
                item {
                    Text(
                        "間違えた単語 (${result.wrongWords.size}語)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(result.wrongWords) { word ->
                    ResultWrongWordCard(english = word.english, meaning = word.meaning)
                }
            }
        }
        ResultActionBar(onRetry = onRetry, onNext = onNext)
    }
}

@Composable
private fun ResultMessageCard(title: String, message: String) {
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

@Composable
private fun ResultActionBar(onRetry: () -> Unit, onNext: () -> Unit) {
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
