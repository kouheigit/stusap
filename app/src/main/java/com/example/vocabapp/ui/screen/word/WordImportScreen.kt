package com.example.vocabapp

import com.example.vocabapp.util.errorImportLog

import com.example.vocabapp.ui.theme.Danger

import com.example.vocabapp.ui.theme.Success

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.TextDark

import com.example.vocabapp.ui.theme.SoftBlue

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.screen.common.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.vocabapp.viewmodel.WordImportViewModel
import kotlinx.coroutines.launch

@Composable
internal fun WordImportScreen(navController: NavHostController, viewModel: WordImportViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val preview by viewModel.preview.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.showLoading()
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val result = runCatching { context.readImportFileAsRows(uri) }
                result.onSuccess { rows ->
                    viewModel.loadRows(rows)
                }.onFailure {
                    errorImportLog("File read error")
                    viewModel.showMessage("ファイルの読み込みに失敗しました。形式とサイズを確認してください。")
                }
            }
        }
    }

    BlueScaffold(title = "単語・熟語インポート", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Button(
                    onClick = {
                        picker.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "application/csv",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Excel / CSVファイルを選択", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
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
                    ImportSummaryCard(
                        title = if (result == null) "読み込み結果" else "登録結果",
                        totalRows = result?.totalRows ?: currentPreview.totalRows,
                        newCount = result?.insertedCount ?: currentPreview.newWords.count { it.type == "word" },
                        newIdiomCount = result?.insertedIdiomCount ?: currentPreview.newWords.count { it.type == "phrase" || it.type == "sentence" },
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
                            Text("登録する", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (currentPreview.newWords.isNotEmpty()) {
                    item { SectionTitle("登録予定 (${currentPreview.newCount}件)") }
                    items(currentPreview.newWords) { word ->
                        ImportWordRow(word.english, word.meaning, word.type)
                    }
                }
                if (currentPreview.duplicateWords.isNotEmpty()) {
                    item { SectionTitle("重複スキップ (${currentPreview.duplicateCount}件)") }
                    items(currentPreview.duplicateWords.take(20)) { word ->
                        ImportWordRow(word.english, word.meaning, word.type)
                    }
                }
                if (currentPreview.errors.isNotEmpty()) {
                    item { SectionTitle("エラー (${currentPreview.errorCount}件)") }
                    items(currentPreview.errors.take(20)) { error ->
                        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("${error.rowNumber}行目", color = Danger, fontWeight = FontWeight.Bold)
                                Text(error.reason, color = TextDark)
                                Text(error.rawValues.toMaskedPreview(), color = TextMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun List<String>.toMaskedPreview(): String =
    take(4).joinToString(", ") { value ->
        val trimmed = value.trim()
        when {
            trimmed.isBlank() -> "(空)"
            trimmed.length <= 8 -> "${trimmed.take(2)}..."
            else -> "${trimmed.take(4)}..."
        }
    }
