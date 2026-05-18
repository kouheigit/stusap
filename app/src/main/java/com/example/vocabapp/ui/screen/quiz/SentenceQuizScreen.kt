package com.example.vocabapp

import android.view.inputmethod.EditorInfo
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.repository.MAX_CUSTOM_MEANING_CHARS
import com.example.vocabapp.data.repository.MAX_CUSTOM_SENTENCE_CHARS
import com.example.vocabapp.domain.model.SentenceQuizResult
import com.example.vocabapp.domain.model.SentenceQuizState
import com.example.vocabapp.viewmodel.AddSentenceViewModel
import com.example.vocabapp.viewmodel.CustomSentenceListViewModel
import com.example.vocabapp.viewmodel.SentenceQuizViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
internal fun SentenceMenuScreen(
    navController: NavHostController,
    viewModel: CustomSentenceListViewModel = hiltViewModel()
) {
    val sentences by viewModel.sentences.collectAsStateWithLifecycle()
    BlueScaffold(title = "文章問題", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { navController.navigate(Route.AddSentence.path) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("文章登録", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { navController.navigate(Route.CustomSentenceList.path) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("登録一覧", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Button(
                    onClick = { navController.navigate(Route.SentenceQuiz.path) },
                    enabled = sentences.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (sentences.isEmpty()) "文章を登録してから開始できます"
                        else "文章問題を開始（${sentences.size}文登録済み）",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (sentences.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("登録文章", color = TextMuted, fontSize = 12.sp)
                                Text("${sentences.size}", color = DeepBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                Text("文", color = TextMuted, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("出題可能", color = TextMuted, fontSize = 12.sp)
                                Text("${sentences.size}", color = AccentBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                Text("問", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            item {
                SectionTitle("文章問題について")
            }
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("登録した英文から並べ替え問題を自動生成します", color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text("A", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("英文をそのまま入力（6語以上）", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("→ 4語が自動で空白になります", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.15f)),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text("B", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("[語句]で4つを囲んで入力", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("→ その語句が並べ替え対象になります", color = TextMuted, fontSize = 12.sp)
                                Text("例: I [might][stay][as][well] as join in a tour", color = AccentBlue, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AddSentenceScreen(
    navController: NavHostController,
    viewModel: AddSentenceViewModel = hiltViewModel()
) {
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    var sentence by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(saved) {
        if (saved) {
            viewModel.resetSaved()
            sentence = ""
            meaning = ""
        }
    }

    BlueScaffold(title = "文章登録", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(SoftBlue)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("英文", fontWeight = FontWeight.Bold, color = TextMuted)
                            val wordCount = sentence.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                            val bracketCount = "\\[([^\\]]+)\\]".toRegex().findAll(sentence).count()
                            val countLabel = when {
                                bracketCount > 0 -> "[語句] $bracketCount / 4"
                                sentence.isBlank() -> ""
                                else -> "${wordCount}語"
                            }
                            val countColor = when {
                                bracketCount > 0 && bracketCount < 4 -> Danger
                                bracketCount == 4 -> Success
                                wordCount in 1..5 && sentence.isNotBlank() -> Danger
                                wordCount >= 6 -> Success
                                else -> TextMuted
                            }
                            if (countLabel.isNotEmpty()) {
                                Text(countLabel, fontSize = 12.sp, color = countColor, fontWeight = FontWeight.Bold)
                            }
                        }
                        AddWordField(
                            label = "",
                            placeholder = "例: I [might][stay][as][well] as join in a tour",
                            value = sentence,
                            onValueChange = { sentence = it.take(MAX_CUSTOM_SENTENCE_CHARS) },
                            imeAction = EditorInfo.IME_ACTION_NEXT,
                            autoFocus = true
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("日本語の意味", fontWeight = FontWeight.Bold, color = TextMuted)
                        AddWordField(
                            label = "",
                            placeholder = "例: パッケージツアーに参加するよりも家にいた方がいい",
                            value = meaning,
                            onValueChange = { meaning = it.take(MAX_CUSTOM_MEANING_CHARS) },
                            imeAction = EditorInfo.IME_ACTION_DONE,
                            onImeAction = { viewModel.save(sentence, meaning) }
                        )
                    }
                    val wordCount2 = sentence.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                    val bracketCount2 = "\\[([^\\]]+)\\]".toRegex().findAll(sentence).count()
                    val isValidSentence = sentence.isNotBlank() && (
                        (bracketCount2 == 0 && wordCount2 >= 6) ||
                        bracketCount2 == 4
                    )
                    val validationMsg = when {
                        sentence.isBlank() -> null
                        bracketCount2 > 0 && bracketCount2 < 4 -> "[語句]は4つ必要です（現在${bracketCount2}つ）"
                        bracketCount2 > 4 -> "[語句]は4つまでにしてください"
                        bracketCount2 == 0 && wordCount2 < 6 -> "6語以上の英文が必要です（現在${wordCount2}語）"
                        else -> null
                    }
                    if (validationMsg != null) {
                        Text(validationMsg, color = Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.save(sentence, meaning) },
                        enabled = isValidSentence && meaning.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("登録する", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (saved) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Success),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("登録しました", color = Color.White, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = { navController.navigate(Route.CustomSentenceList.path) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            Text("一覧へ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("入力形式", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("① そのまま英文を入力（6語以上必要）", color = TextMuted, fontSize = 12.sp)
                    Text("   → 4語をランダムに自動で空白に変換", color = TextMuted, fontSize = 12.sp)
                    Text("② [語句]で語句を囲む（4つ必要）", color = TextMuted, fontSize = 12.sp)
                    Text("   → その語句が並べ替え対象になる", color = TextMuted, fontSize = 12.sp)
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))
                    Text("例② : I [might][stay][as][well] as join in a tour", color = AccentBlue, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
internal fun CustomSentenceListScreen(
    navController: NavHostController,
    viewModel: CustomSentenceListViewModel = hiltViewModel()
) {
    val sentences by viewModel.sentences.collectAsStateWithLifecycle()
    BlueScaffold(title = "登録文章一覧", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Button(
                    onClick = { navController.navigate(Route.AddSentence.path) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                    Spacer(Modifier.width(4.dp))
                    Text("文章を追加", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (sentences.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentBlue.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text("登録済みの文章はまだありません", color = TextMuted, fontSize = 15.sp)
                            Text("上のボタンから英文を追加してください", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                itemsIndexed(sentences) { idx, s ->
                    SentenceRow(index = idx + 1, sentence = s, onDelete = { viewModel.delete(s.id) })
                }
            }
        }
    }
}

@Composable
internal fun SentenceRow(index: Int, sentence: CustomSentenceEntity, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("文章を削除") },
            text = { Text("この文章を削除しますか？") },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text("削除", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("キャンセル") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$index",
                color = AccentBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    sentence.sentence,
                    color = DeepBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    sentence.meaning,
                    color = TextMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dateStr = remember(sentence.addedAt) {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = sentence.addedAt }
                    "${cal.get(java.util.Calendar.YEAR)}/${cal.get(java.util.Calendar.MONTH) + 1}/${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
                }
                Text(dateStr, color = TextMuted.copy(alpha = 0.6f), fontSize = 11.sp)
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Danger)
            }
        }
    }
}

@Composable
internal fun SentenceQuizScreen(
    navController: NavHostController,
    viewModel: SentenceQuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val result = state.result
    val soundPlayer = rememberSoundPlayer()

    LaunchedEffect(state.isAnswered) {
        if (state.isAnswered) {
            delay(30L)
            if (state.isCorrect == true) soundPlayer.playCorrect() else soundPlayer.playWrong()
        }
    }

    if (state.isFinished && result != null) {
        BlueScaffold(title = "文章問題") { inner ->
            SentenceResultContent(
                result = result,
                modifier = Modifier.padding(inner),
                onRetry = {
                    navController.navigate(Route.SentenceQuiz.path) {
                        popUpTo(Route.SentenceQuiz.path) { inclusive = true }
                    }
                },
                onHome = { navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } },
                onMenu = { navController.navigate(Route.SentenceMenu.path) { popUpTo(Route.SentenceMenu.path) { inclusive = true } } }
            )
        }
        return
    }

    BlueScaffold(title = "文章問題", onBack = { navController.popBackStack() }) { inner ->
        when {
            state.questions.isEmpty() && state.startedAt == 0L ->
                Box(Modifier.fillMaxSize().padding(inner).background(SoftBlue), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                        Text("問題を準備中...", color = TextMuted, fontSize = 14.sp)
                    }
                }
            state.questions.isEmpty() ->
                EmptyMessage(
                    modifier = Modifier.padding(inner).background(BrightBlue),
                    title = "出題できる文章がありません\n\n6語以上の英文、または\n[語句]形式で4つの語句を\n含む英文を登録してください",
                    button = "戻る",
                    onClick = { navController.popBackStack() }
                )
            else ->
                SentenceQuizContent(
                    modifier = Modifier.padding(inner),
                    state = state,
                    onSelectChoice = viewModel::selectWord,
                    onUndo = viewModel::undoLastWord,
                    onNext = viewModel::nextQuestion
                )
        }
    }
}

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

@Composable
internal fun SentenceResultContent(
    result: SentenceQuizResult,
    modifier: Modifier,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    onMenu: () -> Unit
) {
    val isPerfect = result.correctCount == result.totalQuestions
    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }
    var medalVisible by remember { mutableStateOf(false) }
    val soundPlayer = rememberSoundPlayer()

    LaunchedEffect(Unit) {
        val animDuration = 1200
        soundPlayer.playSequence(listOf(Pair(440f, 150), Pair(523f, 150), Pair(659f, 200), Pair(784f, 250)), false)
        launch {
            animProgress.animateTo(
                targetValue = (result.accuracy / 100f).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = animDuration, easing = LinearEasing)
            )
        }
        val finalAcc = result.accuracy.toInt()
        val startTime = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed >= animDuration) { displayedAccuracy = finalAcc; break }
            displayedAccuracy = ((elapsed.toFloat() / animDuration) * finalAcc).toInt()
            delay(16L)
        }
        delay(200L)
        medalVisible = true
        launch { medalAlpha.animateTo(1f, animationSpec = tween(300)) }
        medalScale.animateTo(1.1f, animationSpec = tween(280))
        medalScale.animateTo(0.95f, animationSpec = tween(100))
        medalScale.animateTo(1f, animationSpec = tween(100))
    }

    Column(modifier.fillMaxSize().background(BrightBlue)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("文章問題 結果", color = TextMuted, fontSize = 15.sp)
                        if (isPerfect && medalVisible) {
                            Text(
                                "🎉 全問正解！",
                                color = Gold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.alpha(medalAlpha.value)
                            )
                        }
                        if (medalVisible) {
                            Text(
                                "$displayedAccuracy%",
                                color = if (isPerfect) Gold else DeepBlue,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.scale(medalScale.value).alpha(medalAlpha.value)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { animProgress.value },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color = Teal,
                            trackColor = Color(0xFFDDE5EC)
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) { idx ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (idx < result.starCount) Gold else TextMuted.copy(alpha = 0.3f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            val rankLabel = when (result.starCount) {
                                3 -> "Excellent!"
                                2 -> "Good!"
                                1 -> "Keep trying!"
                                else -> "Practice more"
                            }
                            Text(rankLabel, color = if (result.starCount >= 2) Success else TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("正解", color = TextMuted, fontSize = 13.sp)
                                Text("${result.correctCount}", color = Success, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("不正解", color = TextMuted, fontSize = 13.sp)
                                Text("${result.wrongCount}", color = Danger, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("問題数", color = TextMuted, fontSize = 13.sp)
                                Text("${result.totalQuestions}", color = TextDark, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
                        val mins = result.studySeconds / 60
                        val secs = result.studySeconds % 60
                        val timeStr = if (mins > 0) "${mins}分${secs}秒" else "${secs}秒"
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("学習時間: $timeStr", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("もう一度", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("文章問題メニュー", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onHome,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("ホーム", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
