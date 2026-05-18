package com.example.vocabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.domain.model.SentenceQuizState


internal fun buildAnnotatedSentenceTemplate(template: String, nextSlotIndex: Int = -1) = buildAnnotatedString {
    val markerList = listOf("①", "②", "③", "④")
    val words = template.split(" ")
    var markerCount = 0
    words.forEachIndexed { i, word ->
        if (word in markerList) {
            val isNext = markerCount == nextSlotIndex
            val markerColor = if (isNext) Gold else AccentBlue
            withStyle(SpanStyle(
                color = markerColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                background = if (isNext) Gold.copy(alpha = 0.12f) else Color.Transparent
            )) {
                append(word)
            }
            markerCount++
        } else {
            withStyle(SpanStyle(color = DeepBlue, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                append(word)
            }
        }
        if (i < words.lastIndex) append(" ")
    }
}

@Composable
internal fun SentenceQuizContent(
    modifier: Modifier,
    state: SentenceQuizState,
    onSelectChoice: (Int) -> Unit,
    onUndo: () -> Unit,
    onNext: () -> Unit
) {
    val question = state.currentQuestion ?: return
    val selectedIndexSet = state.selectedChoiceIndices.toSet()

    Box(modifier.fillMaxSize().background(SoftBlue)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "問題 ${state.currentIndex + 1} / ${state.questions.size}",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("○ ${state.correctCount}", color = Success, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("✗ ${state.wrongCount}", color = Danger, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1) / state.questions.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = AccentBlue,
                trackColor = Color.White
            )
            Text(
                if (state.isAnswered) "答え合わせ" else "並べ替えて文を完成させよう",
                color = if (state.isAnswered) {
                    if (state.isCorrect == true) Success else Danger
                } else TextDark,
                fontSize = 15.sp,
                fontWeight = if (state.isAnswered) FontWeight.Bold else FontWeight.Normal
            )
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val nextSlot = if (state.isAnswered) -1 else state.selectedWords.size
                    val annotated = buildAnnotatedSentenceTemplate(question.template, nextSlot)
                    Text(
                        annotated,
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    )
                    if (state.isAnswered) {
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
                        val answerText = question.answers.joinToString("  ")
                        val resultLabel = if (state.isCorrect == true) "✓ 正解" else "✗ 不正解"
                        Text(
                            resultLabel,
                            color = if (state.isCorrect == true) Success else Danger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "正解: $answerText",
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(question.meaning, color = TextMuted, fontSize = 14.sp)
                    }
                }
            }
            Text("選択済み", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val markers = listOf("①", "②", "③", "④")
                markers.forEachIndexed { i, marker ->
                    val filledWord = state.selectedWords.getOrNull(i)
                    val isAnsweredCorrect = state.isAnswered && filledWord == question.answers.getOrNull(i)
                    val isAnsweredWrong = state.isAnswered && filledWord != null && !isAnsweredCorrect
                    val bgColor = when {
                        filledWord == null -> Color.White.copy(alpha = 0.5f)
                        isAnsweredCorrect -> Success.copy(alpha = 0.15f)
                        isAnsweredWrong -> Danger.copy(alpha = 0.15f)
                        else -> Color.White
                    }
                    val borderColor = when {
                        isAnsweredCorrect -> Success
                        isAnsweredWrong -> Danger
                        filledWord != null -> AccentBlue.copy(alpha = 0.5f)
                        else -> Color.Transparent
                    }
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                marker,
                                color = if (filledWord != null) AccentBlue else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                filledWord ?: "—",
                                color = if (filledWord != null) DeepBlue else TextMuted,
                                fontSize = 14.sp,
                                fontWeight = if (filledWord != null) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            Text("選択肢", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            val indexedChoices = question.shuffledChoices.mapIndexed { i, w -> i to w }
            indexedChoices.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { (choiceIdx, word) ->
                        val isSelected = selectedIndexSet.contains(choiceIdx)
                        val selectedOrder = state.selectedChoiceIndices.indexOf(choiceIdx)
                        val isCorrectPosition = state.isAnswered && isSelected &&
                            selectedOrder in question.answers.indices &&
                            word == question.answers[selectedOrder]
                        val isWrongPosition = state.isAnswered && isSelected && !isCorrectPosition
                        val containerColor = when {
                            isCorrectPosition -> Success
                            isWrongPosition -> Danger
                            isSelected -> Color.White.copy(alpha = 0.3f)
                            else -> AccentBlue
                        }
                        Button(
                            onClick = { if (!isSelected && !state.isAnswered) onSelectChoice(choiceIdx) },
                            enabled = !isSelected && !state.isAnswered,
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = containerColor,
                                disabledContainerColor = containerColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                word,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (!state.isAnswered) {
                val lastWord = state.selectedWords.lastOrNull()
                OutlinedButton(
                    onClick = onUndo,
                    enabled = lastWord != null,
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (lastWord != null) "「$lastWord」をもどす" else "もどす",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (state.currentIndex >= state.questions.lastIndex) "結果を見る" else "次の問題",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
