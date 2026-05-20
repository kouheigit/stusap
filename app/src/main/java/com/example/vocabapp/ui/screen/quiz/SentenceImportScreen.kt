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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.showLoading()
            scope.launch(Dispatchers.IO) {
                val readResult = runCatching { context.readImportFileAsCsv(uri) }
                readResult.onSuccess { csvText ->
                    viewModel.loadCsv(csvText)
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
                    Text("文章Excel / CSVを選択", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
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
            preview?.let { currentPreview ->
                item {
                    SentenceImportSummaryCard(
                        title = if (result == null) "読み込み結果" else "登録結果",
                        totalRows = result?.totalRows ?: currentPreview.totalRows,
                        newCount = result?.insertedCount ?: currentPreview.newCount,
                        duplicateCount = result?.duplicateCount ?: currentPreview.duplicateCount,
                        errorCount = result?.errorCount ?: currentPreview.errorCount
                    )
                }
                if (result == null) {
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
                            Text("文章を登録する", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
private fun SentenceImportFormatCard() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("文章専用の取り込みです", color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("1行目に sentence と meaning のヘッダーを入れてください。", color = TextDark, fontSize = 14.sp)
            Text("日本語ヘッダーは「文章」「意味」でも読み込めます。", color = TextMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SentenceImportSummaryCard(
    title: String,
    totalRows: Int,
    newCount: Int,
    duplicateCount: Int,
    errorCount: Int
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
        }
    }
}

@Composable
private fun SentenceImportRow(sentence: ImportedSentence) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(sentence.sentence, color = DeepBlue, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(sentence.meaning, color = TextDark, fontSize = 14.sp)
            Text("文章", color = BrightBlue, fontWeight = FontWeight.Bold)
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
