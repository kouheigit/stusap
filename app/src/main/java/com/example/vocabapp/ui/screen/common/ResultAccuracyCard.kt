package com.example.vocabapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.domain.model.QuizResult

@Composable
internal fun ResultAccuracyCard(
    result: QuizResult,
    resId: Int,
    isPerfect: Boolean,
    displayedAccuracy: Int,
    medalVisible: Boolean,
    medalScale: Float,
    medalAlpha: Float,
    progress: Float
) {
    ResultSectionCard(header = "正解率") {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val medalSize = if (isPerfect) 110.dp else 160.dp
            val resultMedalMod = when {
                isPerfect -> Modifier.size(medalSize)
                resId == R.drawable.medal_bronze -> Modifier.size(width = 107.dp, height = 160.dp)
                else -> Modifier.size(medalSize)
            }
            if (medalVisible) {
                Image(
                    painter = painterResource(resId),
                    contentDescription = if (isPerfect) "パーフェクトメダル" else null,
                    modifier = resultMedalMod.scale(medalScale).alpha(medalAlpha),
                    contentScale = ContentScale.Fit
                )
            } else {
                Spacer(resultMedalMod)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("${result.correctCount}/${result.totalQuestions}正解", color = TextMuted, fontSize = 16.sp)
                Text(
                    "$displayedAccuracy%",
                    color = if (isPerfect) Gold else DeepBlue,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 58.sp
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp)),
            color = Teal,
            trackColor = Color(0xFFDDE5EC)
        )
    }
}

