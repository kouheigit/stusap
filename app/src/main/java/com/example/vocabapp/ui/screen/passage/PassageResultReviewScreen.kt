package com.example.vocabapp.ui.screen.passage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.ui.theme.AccentBlue
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.TextDark
import com.example.vocabapp.ui.theme.TextMuted

@Composable
internal fun PassageResultReviewScreen(
    modifier: Modifier = Modifier,
    set: PassageSet,
    state: PassageSessionState,
    hasNextSet: Boolean,
    reviewToken: Int,
    onRetry: () -> Unit,
    onNextSet: () -> Unit,
    onHome: () -> Unit
) {
    val reviews = set.reviews(state)
    var reviewIndex by rememberSaveable(set.id, reviewToken) { mutableIntStateOf(0) }
    var showDocument by rememberSaveable(set.id, reviewToken) { mutableStateOf(false) }
    val currentReview = reviews.getOrNull(reviewIndex) ?: reviews.first()

    LaunchedEffect(reviews.size) {
        reviewIndex = reviewIndex.coerceIn(0, reviews.lastIndex.coerceAtLeast(0))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrightBlue)
    ) {
        ReviewTopBar(
            score = state.score,
            total = set.questions.size,
            currentNumber = reviewIndex + 1,
            onRetry = onRetry
        )
        PassageReviewSelector(
            reviews = reviews,
            currentIndex = reviewIndex,
            onSelect = { reviewIndex = it }
        )
        AnimatedVisibility(visible = showDocument) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                PassageDocumentPanel(documents = set.documents)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryStrip(score = state.score, total = set.questions.size, remaining = state.remainingSec)
            PassageReviewCard(review = currentReview)
            Spacer(modifier = Modifier.height(8.dp))
        }
        ReviewActionBar(
            showDocument = showDocument,
            hasNextSet = hasNextSet,
            isLastReview = reviewIndex == reviews.lastIndex,
            onToggleDocument = { showDocument = !showDocument },
            onNext = {
                if (reviewIndex < reviews.lastIndex) {
                    reviewIndex += 1
                } else if (hasNextSet) {
                    onNextSet()
                } else {
                    onHome()
                }
            },
            onHome = onHome
        )
    }
}

@Composable
private fun ReviewTopBar(
    score: Int,
    total: Int,
    currentNumber: Int,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = RoundedCornerShape(3.dp), color = Color(0xFF8C72E8)) {
                Text(
                    "長文問題",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${currentNumber}",
                        color = BrightBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        "/$total 問",
                        color = TextMuted,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                Text(
                    "正解 $score 問",
                    color = DeepBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("やり直す")
            }
        }
        androidx.compose.material3.HorizontalDivider(color = DeepBlue, thickness = 4.dp)
    }
}

@Composable
private fun SummaryStrip(score: Int, total: Int, remaining: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ReviewSummaryCard("正解", "$score", PassageReviewCorrect, Modifier.weight(1f))
        ReviewSummaryCard("不正解", "${total - score}", PassageReviewWrong, Modifier.weight(1f))
        ReviewSummaryCard("残り", formatClock(remaining), AccentBlue, Modifier.weight(1f))
    }
}

@Composable
private fun ReviewSummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ReviewActionBar(
    showDocument: Boolean,
    hasNextSet: Boolean,
    isLastReview: Boolean,
    onToggleDocument: () -> Unit,
    onNext: () -> Unit,
    onHome: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onToggleDocument,
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (showDocument) "問題文を閉じる" else "問題文を確認", color = AccentBlue, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = if (isLastReview && !hasNextSet) onHome else onNext,
            modifier = Modifier.weight(1f).height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLastReview && !hasNextSet) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ホームへ", fontWeight = FontWeight.Black, fontSize = 18.sp)
            } else {
                Text("次へ", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}

private fun formatClock(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
