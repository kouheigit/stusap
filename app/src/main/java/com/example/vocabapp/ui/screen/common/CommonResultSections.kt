package com.example.vocabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

