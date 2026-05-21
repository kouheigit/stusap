package com.example.vocabapp

import com.example.vocabapp.ui.screen.common.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.data.repository.isQuizReadySentence
import com.example.vocabapp.data.repository.sentenceType
import com.example.vocabapp.domain.model.ImportErrorRow
import com.example.vocabapp.domain.model.ImportedSentence
import com.example.vocabapp.viewmodel.SentenceImportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal fun SentenceImportScreen(
    navController: NavHostController,
    viewModel: SentenceImportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val preview by viewModel.preview.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val fileName by viewModel.fileName.collectAsStateWithLifecycle()
    val remainingCapacity by viewModel.remainingCapacity.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadRemainingCapacity()
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val pickedName = context.queryDisplayName(uri)
            viewModel.showLoading()
            scope.launch(Dispatchers.IO) {
                val readResult = runCatching { context.readImportFileAsCsv(uri) }
                readResult.onSuccess { csvText ->
                    viewModel.loadCsv(csvText, pickedName)
                }.onFailure {
                    errorImportLog("Sentence file read error")
                    viewModel.showMessage("文章ファイルの読み込みに失敗しました。形式とサイズを確認してください。")
                }
            }
        }
    }

    BlueScaffold(title = "文章インポート", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Button(
                    onClick = {
                        if (preview != null || result != null) {
                            viewModel.resetForNewFile()
                        }
                        picker.launch(
                            arrayOf(
                                "text/*",
                                "text/csv",
                                "application/csv",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (preview != null || result != null) "別のファイルを選択" else "文章Excel / CSVを選択",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            fileName?.let { name ->
                item { SentenceFileNameCard(name) }
            }

            remainingCapacity?.let { capacity ->
                item { SentenceCapacityChip(capacity) }
            }

            item {
                SentenceImportFormatCard()
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = BrightBlue)
                        Spacer(Modifier.width(12.dp))
                        Text("処理中...", color = TextDark, fontWeight = FontWeight.Bold)
                    }
                }
            }

            message?.let { text ->
                item { Text(text, color = Danger, fontWeight = FontWeight.Bold) }
            }

            result?.let { importResult ->
                item {
                    SentenceImportSuccessCard(
                        insertedCount = importResult.insertedCount,
                        quizReadyCount = importResult.quizReadyInsertedCount,
                        onViewList = { navController.navigate(Route.CustomSentenceList.path) }
                    )
                }
            }

            preview?.let { currentPreview ->
                item {
                    SentenceImportSummaryCard(
                        title = if (result == null) "読み込み結果" else "登録結果",
                        totalRows = result?.totalRows ?: currentPreview.totalRows,
                        newCount = result?.insertedCount ?: currentPreview.newCount,
                        duplicateCount = result?.duplicateCount ?: currentPreview.duplicateCount,
                        errorCount = result?.errorCount ?: currentPreview.errorCount,
                        quizReadyCount = if (result == null) currentPreview.quizReadyCount else null,
                        quizIncompatibleCount = if (result == null) currentPreview.quizIncompatibleCount else null
                    )
                }

                if (result == null) {
                    if (currentPreview.quizIncompatibleCount > 0) {
                        item {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.12f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "⚠️ ${currentPreview.quizIncompatibleCount}件はクイズ非対応（語数不足 or [語句]数が4以外）。登録はできますがクイズには出題されません。",
                                    color = Gold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = { viewModel.registerPreview() },
                            enabled = currentPreview.newCount > 0 && !isLoading,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Success),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("文章を登録する（${currentPreview.newCount}件）", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (currentPreview.newSentences.isNotEmpty()) {
                    item { SectionTitle("登録予定 (${currentPreview.newCount}件)") }
                    items(currentPreview.newSentences) { sentence ->
                        SentenceImportRow(sentence)
                    }
                }

                if (currentPreview.duplicateSentences.isNotEmpty()) {
                    item { SectionTitle("重複スキップ (${currentPreview.duplicateCount}件)") }
                    items(currentPreview.duplicateSentences.take(20)) { sentence ->
                        SentenceImportRow(sentence)
                    }
                }

                if (currentPreview.errors.isNotEmpty()) {
                    item { SectionTitle("エラー (${currentPreview.errorCount}件)") }
                    items(currentPreview.errors.take(20)) { error ->
                        SentenceImportErrorRow(error)
                    }
                }
            }
        }
    }
}

@Composable
private fun SentenceFileNameCard(fileName: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DeepBlue.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = null,
                tint = DeepBlue,
                modifier = Modifier.size(18.dp)
            )
            Text(
                fileName,
                color = DeepBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SentenceCapacityChip(remaining: Int) {
    val isLow = remaining < 200
    val chipColor = if (isLow) Danger.copy(alpha = 0.12f) else Success.copy(alpha = 0.12f)
    val textColor = if (isLow) Danger else Success
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = chipColor),
        modifier = Modifier
    ) {
        Text(
            if (isLow) "残り${remaining}件（上限に近づいています）" else "残り${remaining}件登録可能",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SentenceImportFormatCard() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("文章専用インポート", color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("1行目にヘッダー行を入れてください。", color = TextDark, fontSize = 14.sp)
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = SoftBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CSVフォーマット例", color = DeepBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("sentence,meaning", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("I might stay as well as join a tour,ツアーに参加するよりも家にいた方がいい", color = TextDark, fontSize = 11.sp)
                    Text("I [might][stay][as][well] as join a tour,ツアーに...", color = TextDark, fontSize = 11.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                SentenceTypeBadge("A型", AccentBlue)
                Text("6語以上でそのまま入力 → 自動で4語が空白に", color = TextMuted, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                SentenceTypeBadge("B型", Gold)
                Text("[語句]で4つを囲む → その語句が並べ替え対象", color = TextMuted, fontSize = 12.sp)
            }
            androidx.compose.material3.HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
            Text(
                "対応ヘッダー名: sentence / english / 英文 / 文章 / 例文 / 英語",
                color = TextMuted,
                fontSize = 11.sp
            )
            Text(
                "意味列: meaning / 日本語の意味 / 意味 / 訳 / 和訳 / 日本語",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SentenceImportSuccessCard(insertedCount: Int, quizReadyCount: Int, onViewList: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Success),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("登録完了！", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text("${insertedCount}件登録（クイズ対応: ${quizReadyCount}件）", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
            TextButton(
                onClick = onViewList,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text("一覧へ", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SentenceImportSummaryCard(
    title: String,
    totalRows: Int,
    newCount: Int,
    duplicateCount: Int,
    errorCount: Int,
    quizReadyCount: Int?,
    quizIncompatibleCount: Int?
) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryChip("読み込み", "${totalRows}件", Modifier.weight(1f))
                SummaryChip("文章登録", "${newCount}件", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryChip("重複", "${duplicateCount}件", Modifier.weight(1f))
                SummaryChip("エラー", "${errorCount}件", Modifier.weight(1f))
            }
            if (quizReadyCount != null && quizIncompatibleCount != null && newCount > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryChip("クイズ対応", "${quizReadyCount}件", Modifier.weight(1f))
                    SummaryChip("対応外", "${quizIncompatibleCount}件", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SentenceTypeBadge(label: String, color: Color) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Text(
            label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun SentenceImportRow(sentence: ImportedSentence) {
    val quizReady = sentence.sentence.isQuizReadySentence()
    val type = sentence.sentence.sentenceType()
    val badgeColor = if (type == "B") Gold else AccentBlue
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(sentence.sentence, color = DeepBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(sentence.meaning, color = TextDark, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                SentenceTypeBadge("${type}型", badgeColor)
                if (quizReady) {
                    SentenceTypeBadge("クイズ対応", Success)
                } else {
                    SentenceTypeBadge("語数不足", Danger)
                }
            }
        }
    }
}

@Composable
private fun SentenceImportErrorRow(error: ImportErrorRow) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${error.rowNumber}行目", color = Danger, fontWeight = FontWeight.Bold)
            Text(error.reason, color = TextDark)
            Text(error.rawValues.toSentenceMaskedPreview(), color = TextMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun List<String>.toSentenceMaskedPreview(): String =
    take(4).joinToString(", ") { value ->
        val trimmed = value.trim()
        when {
            trimmed.isBlank() -> "(空)"
            trimmed.length <= 8 -> "${trimmed.take(2)}..."
            else -> "${trimmed.take(4)}..."
        }
    }
