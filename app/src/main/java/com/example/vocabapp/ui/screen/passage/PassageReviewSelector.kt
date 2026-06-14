package com.example.vocabapp.ui.screen.passage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.SoftBlue

@Composable
internal fun PassageReviewSelector(
    reviews: List<PassageQuestionReview>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        reviews.forEachIndexed { index, review ->
            val answerColor = if (review.isCorrect) PassageReviewCorrect else PassageReviewWrong
            val chip = if (review.isCorrect) "○" else "×"
            val current = index == currentIndex
            Column(
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    chip,
                    color = answerColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )
                Text(
                    review.question.number,
                    color = if (current) DeepBlue else PassageReviewMuted,
                    fontSize = 14.sp,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(modifier = Modifier.size(6.dp))
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .border(1.dp, SoftBlue, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (review.selectedIndex != null) DeepBlue else Color.Transparent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                if (current) {
                    Spacer(
                        modifier = Modifier
                            .size(width = 56.dp, height = 4.dp)
                            .background(BrightBlue, RoundedCornerShape(2.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.size(width = 56.dp, height = 4.dp))
                }
            }
        }
    }
}
