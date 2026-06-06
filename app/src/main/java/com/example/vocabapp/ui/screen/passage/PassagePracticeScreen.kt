package com.example.vocabapp.ui.screen.passage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.vocabapp.ui.navigation.Route
import com.example.vocabapp.ui.theme.AccentBlue
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.Success
import com.example.vocabapp.ui.theme.Teal
import com.example.vocabapp.ui.theme.TextDark
import com.example.vocabapp.ui.theme.TextMuted
import kotlinx.coroutines.delay

private val PassageBlue = Color(0xFF168BEF)
private val RuleGray = Color(0xFFD4DEE5)
private val ChoiceGray = Color(0xFF8797A1)

@Composable
internal fun PassagePracticeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sets: List<PassageSet> = PassagePracticeFixtures.sets
) {
    var setIndex by rememberSaveable { mutableIntStateOf(0) }
    var currentIndex by rememberSaveable(setIndex) { mutableIntStateOf(0) }
    var remainingSec by rememberSaveable(setIndex) {
        mutableIntStateOf(sets.getOrNull(setIndex)?.timeLimitSec ?: 0)
    }
    var finished by rememberSaveable(setIndex) { mutableStateOf(false) }
    var documentExpanded by rememberSaveable(setIndex) { mutableStateOf(true) }
    val set = sets.getOrNull(setIndex) ?: return
    val selections = remember(setIndex) {
        mutableStateListOf<Int?>().also { list ->
            repeat(set.questions.size) { list.add(null) }
        }
    }
    val submitted = remember(setIndex) {
        mutableStateListOf<Boolean>().also { list ->
            repeat(set.questions.size) { list.add(false) }
        }
    }
    val score = set.questions.indices.count { index ->
        submitted.getOrNull(index) == true &&
            selections.getOrNull(index) == set.questions[index].answerIndex
    }
    val state = PassageSessionState(
        setId = set.id,
        currentIndex = currentIndex,
        selections = selections.toList(),
        remainingSec = remainingSec,
        finished = finished,
        score = score
    )

    LaunchedEffect(setIndex, finished) {
        while (!finished && remainingSec > 0) {
            delay(1000L)
            remainingSec -= 1
        }
        if (remainingSec == 0) finished = true
    }

    if (finished) {
        PassageResultScreen(
            modifier = modifier,
            set = set,
            state = state,
            hasNextSet = setIndex < sets.lastIndex,
            onRetry = {
                currentIndex = 0
                remainingSec = set.timeLimitSec ?: 0
                finished = false
                documentExpanded = true
                selections.indices.forEach { selections[it] = null }
                submitted.indices.forEach { submitted[it] = false }
            },
            onNextSet = {
                setIndex += 1
            },
            onHome = {
                navController.navigate(Route.Home.path) {
                    popUpTo(Route.Home.path) { inclusive = true }
                }
            }
        )
        return
    }

    PassagePracticeContent(
        modifier = modifier,
        set = set,
        state = state,
        setPosition = setIndex + 1,
        setCount = sets.size,
        isDocumentExpanded = documentExpanded,
        isSubmitted = submitted.getOrElse(currentIndex) { false },
        onToggleDocument = { documentExpanded = !documentExpanded },
        onClose = { navController.popBackStack() },
        onSelect = { optionIndex ->
            if (!submitted[currentIndex]) selections[currentIndex] = optionIndex
        },
        onSubmit = {
            if (selections[currentIndex] != null) submitted[currentIndex] = true
        },
        onNext = {
            if (currentIndex >= set.questions.lastIndex) {
                finished = true
            } else {
                currentIndex += 1
            }
        }
    )
}

@Composable
private fun PassagePracticeContent(
    modifier: Modifier,
    set: PassageSet,
    state: PassageSessionState,
    setPosition: Int,
    setCount: Int,
    isDocumentExpanded: Boolean,
    isSubmitted: Boolean,
    onToggleDocument: () -> Unit,
    onClose: () -> Unit,
    onSelect: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val question = set.questions[state.currentIndex]
    val progress = (state.currentIndex + 1) / set.questions.size.toFloat()

    Column(modifier.fillMaxSize().background(Color.White)) {
        PassagePracticeTopBar(
            currentNumber = state.currentIndex + 1,
            totalCount = set.questions.size,
            setPosition = setPosition,
            setCount = setCount,
            onClose = onClose
        )
        PassageTimerBar(
            remainingSec = state.remainingSec,
            progress = if ((set.timeLimitSec ?: 0) == 0) 0f else state.remainingSec / set.timeLimitSec!!.toFloat()
        )
        // 本文・設問・選択肢をひとつの縦スクロール領域にまとめ、長文が途中で
        // 見切れないようにする。固定するのは上部バーと下部の操作バーのみ。
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            PassageInstruction(set.instruction)
            AnimatedVisibility(visible = isDocumentExpanded) {
                PassageDocumentPanel(documents = set.documents)
            }
            AnimatedVisibility(visible = !isDocumentExpanded) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(PassageBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "本文を閉じています",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            PassageDocumentToggle(
                expanded = isDocumentExpanded,
                onToggle = onToggleDocument
            )
            PassageQuestionBody(
                question = question,
                selectedIndex = state.selectedIndex,
                isSubmitted = isSubmitted,
                onSelect = onSelect
            )
        }
        BottomPracticeBar(
            progress = progress,
            totalQuestions = set.questions.size,
            currentIndex = state.currentIndex,
            canSubmit = state.selectedIndex != null && !isSubmitted,
            isSubmitted = isSubmitted,
            isLastQuestion = state.currentIndex == set.questions.lastIndex,
            onSubmit = onSubmit,
            onNext = onNext
        )
    }
}

@Composable
private fun PassagePracticeTopBar(
    currentNumber: Int,
    totalCount: Int,
    setPosition: Int,
    setCount: Int,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(86.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = Color(0xFF8C72E8)
            ) {
                Text(
                    "長文問題",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Surface(shape = RoundedCornerShape(100.dp), color = SoftBlue) {
                Text(
                    "セット $setPosition/$setCount",
                    color = DeepBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    currentNumber.toString(),
                    color = BrightBlue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    "/$totalCount 問",
                    color = TextMuted,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "閉じる",
                    tint = DeepBlue,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
        HorizontalDivider(color = DeepBlue, thickness = 4.dp)
    }
}

@Composable
private fun PassageTimerBar(remainingSec: Int, progress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth().height(42.dp).background(Color.White).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Timer, contentDescription = null, tint = ChoiceGray, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(8.dp)),
            color = Teal,
            trackColor = Color(0xFFEAF0F2)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            formatClock(remainingSec),
            color = ChoiceGray,
            fontSize = 15.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 54.dp)
        )
    }
}

@Composable
private fun PassageInstruction(instruction: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(PassageBlue).padding(start = 16.dp, end = 16.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFE8F4FF), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .border(1.dp, RuleGray, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp)).background(DeepBlue),
                contentAlignment = Alignment.Center
            ) {
                Text("Q", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                instruction,
                color = TextDark,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun PassageDocumentPanel(documents: List<PassageDocument>, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().background(PassageBlue).padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 28.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            documents.forEach { document ->
                when (document.kind) {
                    PassageDocumentKind.Article -> ArticleDocument(document)
                    PassageDocumentKind.Email -> EmailDocument(document)
                    PassageDocumentKind.Notice -> NoticeDocument(document)
                }
            }
        }
    }
}

@Composable
private fun ArticleDocument(document: PassageDocument) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(
            document.title ?: "Article",
            color = Color(0xFF202020),
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontFamily = FontFamily.Serif
        )
        HorizontalDivider(color = Color.Black, thickness = 4.dp)
        PassageBodyText(document.body)
    }
}

@Composable
private fun NoticeDocument(document: PassageDocument) {
    Column(
        modifier = Modifier.border(2.dp, Color(0xFF303030)).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            document.title ?: "Notice",
            color = Color(0xFF202020),
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        HorizontalDivider(color = Color(0xFF303030), thickness = 2.dp)
        // 親が縦スクロールのため高さ制約が無限になる。IntrinsicSize.Min で
        // セルの fillMaxHeight を最も高いセルに揃え、スクロール内でも安全に描画する。
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            NoticeCell("Class", Modifier.weight(0.9f).fillMaxHeight(), header = true)
            NoticeCell("Time", Modifier.weight(1.15f).fillMaxHeight(), header = true)
            NoticeCell(document.body, Modifier.weight(2.6f).fillMaxHeight(), header = false)
        }
    }
}

@Composable
private fun NoticeCell(text: String, modifier: Modifier, header: Boolean) {
    Box(
        modifier = modifier.border(1.dp, Color(0xFF303030)).padding(12.dp),
        contentAlignment = if (header) Alignment.TopCenter else Alignment.TopStart
    ) {
        Text(
            text,
            color = Color(0xFF202020),
            fontSize = if (header) 21.sp else 22.sp,
            lineHeight = if (header) 25.sp else 31.sp,
            fontWeight = if (header) FontWeight.Black else FontWeight.Normal,
            textAlign = if (header) TextAlign.Center else TextAlign.Start
        )
    }
}

@Composable
private fun EmailDocument(document: PassageDocument) {
    val header = document.header
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFC7C7C7)).border(2.dp, Color(0xFF555555)).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (header != null) {
            EmailHeaderRow("To:", header.to)
            EmailHeaderRow("From:", header.from)
            EmailHeaderRow("Date:", header.date)
            EmailHeaderRow("Subject:", header.subject)
        }
        Column(
            modifier = Modifier.fillMaxWidth().background(Color.White).border(1.dp, Color(0xFF777777)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            document.body.split("\n\n").forEach { paragraph ->
                Text(
                    paragraph,
                    color = Color(0xFF202020),
                    fontSize = 23.sp,
                    lineHeight = 33.sp,
                    fontFamily = FontFamily.Serif
                )
            }
        }
    }
}

@Composable
private fun EmailHeaderRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            color = Color(0xFF202020),
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(82.dp).background(Color(0xFFD9D9D9)).border(1.dp, Color(0xFF777777)).padding(4.dp),
            maxLines = 1
        )
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            color = Color(0xFF202020),
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.weight(1f).background(Color.White).border(1.dp, Color(0xFF777777)).padding(horizontal = 8.dp, vertical = 3.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PassageBodyText(body: String) {
    // 段落（空行区切り）ごとに自然な本文として表示する。文末を機械的に分割すると
    // 不自然な改行や略語の誤分割が起きるため、原文の段落構成をそのまま尊重する。
    val paragraphs = body.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        paragraphs.forEach { paragraph ->
            Text(
                paragraph,
                color = Color(0xFF202020),
                fontSize = 21.sp,
                lineHeight = 34.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
private fun PassageDocumentToggle(expanded: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(PassageBlue).clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = null,
                    tint = BrightBlue,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                if (expanded) "問題を閉じる" else "問題を開く",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun PassageQuestionBody(
    question: PassageQuestion,
    selectedIndex: Int?,
    isSubmitted: Boolean,
    onSelect: (Int) -> Unit
) {
    Column(Modifier.fillMaxWidth().background(Color.White)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Surface(shape = RoundedCornerShape(4.dp), color = DeepBlue) {
                Text(
                    "Q ${question.number}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                question.stem,
                color = TextDark,
                fontSize = 19.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Black
            )
            AnimatedVisibility(visible = isSubmitted) {
                FeedbackBlock(question, selectedIndex)
            }
        }
        HorizontalDivider(color = RuleGray)
        question.options.forEachIndexed { index, option ->
            ChoiceRow(
                label = ('A' + index).toString(),
                text = option,
                isSelected = selectedIndex == index,
                isCorrect = isSubmitted && index == question.answerIndex,
                isWrong = isSubmitted && selectedIndex == index && index != question.answerIndex,
                enabled = !isSubmitted,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun FeedbackBlock(question: PassageQuestion, selectedIndex: Int?) {
    val correct = selectedIndex == question.answerIndex
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (correct) Success.copy(alpha = 0.12f) else Danger.copy(alpha = 0.10f)
        ),
        border = BorderStroke(1.dp, if (correct) Success else Danger)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (correct) Success else Danger,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (correct) "正解" else "不正解",
                    color = if (correct) Success else Danger,
                    fontWeight = FontWeight.Black
                )
                question.explanation?.let {
                    Text(it, color = TextDark, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    label: String,
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        isCorrect -> Success.copy(alpha = 0.16f)
        isWrong -> Danger.copy(alpha = 0.14f)
        isSelected -> SoftBlue
        else -> Color.White
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(4.dp)).background(
                when {
                    isCorrect -> Success
                    isWrong -> Danger
                    isSelected -> BrightBlue
                    else -> ChoiceGray
                }
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text,
            color = TextDark,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
    HorizontalDivider(color = RuleGray)
}

@Composable
private fun BottomPracticeBar(
    progress: Float,
    totalQuestions: Int,
    currentIndex: Int,
    canSubmit: Boolean,
    isSubmitted: Boolean,
    isLastQuestion: Boolean,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(72.dp).background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(start = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalQuestions) { index ->
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(
                        if (index == currentIndex) AccentBlue else Color(0xFFEAF0F2)
                    )
                )
            }
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.width(1.dp).height(1.dp),
            color = Color.Transparent,
            trackColor = Color.Transparent
        )
        Button(
            onClick = if (isSubmitted) onNext else onSubmit,
            enabled = isSubmitted || canSubmit,
            modifier = Modifier.width(190.dp).fillMaxHeight(),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSubmitted) DeepBlue else AccentBlue,
                disabledContainerColor = Color(0xFFCAD7DD),
                contentColor = Color.White,
                disabledContentColor = Color.White
            )
        ) {
            Text(
                when {
                    isSubmitted && isLastQuestion -> "結果を見る"
                    isSubmitted -> "次へ"
                    else -> "解答する"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun PassageResultScreen(
    modifier: Modifier,
    set: PassageSet,
    state: PassageSessionState,
    hasNextSet: Boolean,
    onRetry: () -> Unit,
    onNextSet: () -> Unit,
    onHome: () -> Unit
) {
    val total = set.questions.size
    val wrong = total - state.score
    Column(
        modifier = modifier.fillMaxSize().background(BrightBlue).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(22.dp))
        Text("結果", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text(set.id, color = SoftBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("${state.score * 100 / total}%", color = DeepBlue, fontSize = 54.sp, fontWeight = FontWeight.Black)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultMiniStat("正解", "${state.score}", Success, Modifier.weight(1f))
                    ResultMiniStat("不正解", "$wrong", Danger, Modifier.weight(1f))
                    ResultMiniStat("合計", "$total", AccentBlue, Modifier.weight(1f))
                }
            }
        }
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("リトライ", fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
        if (hasNextSet) {
            Button(
                onClick = onNextSet,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("次のセット", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
        OutlinedButton(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("ホームへ戻る", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultMiniStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Black)
    }
}

private fun formatClock(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
