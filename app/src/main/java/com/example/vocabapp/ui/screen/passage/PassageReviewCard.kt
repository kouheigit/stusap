package com.example.vocabapp.ui.screen.passage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.ui.screen.common.GramCard
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.TextDark
import com.example.vocabapp.ui.theme.TextMuted

@Composable
internal fun PassageReviewCard(
    review: PassageQuestionReview,
    modifier: Modifier = Modifier
) {
    GramCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            ReviewHeader(review)
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    review.question.stem,
                    color = TextDark,
                    fontSize = 19.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Black
                )
                ReviewAnswerSection(
                    title = "あなたの解答",
                    text = review.selectedText ?: "未解答",
                    chipLabel = review.selectedIndex?.let { passageChoiceLabel(it) },
                    chipColor = if (review.isCorrect) PassageReviewCorrect else PassageReviewWrong,
                    backgroundColor = if (review.isCorrect) PassageReviewSectionFill else SoftBlue,
                    textColor = if (review.isCorrect) PassageReviewCorrect else PassageReviewWrong
                )
                ReviewAnswerSection(
                    title = "正解",
                    text = review.correctText,
                    chipLabel = passageChoiceLabel(review.question.answerIndex),
                    chipColor = PassageReviewCorrect,
                    backgroundColor = PassageReviewSectionFill,
                    textColor = PassageReviewCorrect
                )
                ReviewExplanationSection(review = review)
            }
        }
    }
}

@Composable
private fun ReviewHeader(review: PassageQuestionReview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBlue)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = DeepBlue,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
        ) {
            Text(
                "Q ${review.question.number}",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun ReviewAnswerSection(
    title: String,
    text: String,
    chipLabel: String?,
    chipColor: Color,
    backgroundColor: Color,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        SectionTitle(title)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (chipLabel != null) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = chipColor
                ) {
                    Text(
                        chipLabel,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text,
                color = textColor,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReviewExplanationSection(review: PassageQuestionReview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PassageReviewSectionLine)
    ) {
        SectionTitle("解説")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                review.question.explanation ?: "解説はまだありません。",
                color = TextDark,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            HorizontalDivider(color = PassageReviewSectionLine)
            review.question.options.forEachIndexed { index, option ->
                val letter = passageChoiceLabel(index)
                val optionColor = when {
                    index == review.question.answerIndex -> PassageReviewCorrect
                    index == review.selectedIndex && !review.isCorrect -> PassageReviewWrong
                    else -> TextDark
                }
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        "$letter :",
                        color = optionColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            option,
                            color = optionColor,
                            fontSize = 16.sp,
                            lineHeight = 23.sp,
                            fontWeight = if (index == review.question.answerIndex || index == review.selectedIndex) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PassageReviewSectionFill)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            title,
            color = TextDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Start
        )
    }
}
