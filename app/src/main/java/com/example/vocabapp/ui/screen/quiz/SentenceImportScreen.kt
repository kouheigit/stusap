package com.example.vocabapp.ui.screen.quiz

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.Gold
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.Success
import com.example.vocabapp.util.errorImportLog
import com.example.vocabapp.queryDisplayName
import com.example.vocabapp.readImportFileAsRows
import com.example.vocabapp.ui.navigation.Route
import com.example.vocabapp.ui.screen.common.BlueScaffold
import com.example.vocabapp.viewmodel.SentenceImportViewModel
import androidx.compose.ui.res.stringResource
import com.example.vocabapp.R
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

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val pickedName = context.queryDisplayName(uri)
            viewModel.showLoading()
            scope.launch(Dispatchers.IO) {
                val readResult = runCatching { context.readImportFileAsRows(uri) }
                readResult.onSuccess { rows ->
                    viewModel.loadRows(rows, pickedName)
                }.onFailure {
                    errorImportLog("Sentence file read error")
                    viewModel.showMessage("文章ファイルの読み込みに失敗しました。形式とサイズを確認してください。")
                }
            }
        }
    }

    BlueScaffold(title = stringResource(R.string.sentence_import_screen_title), onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SentenceFilePickerButton(
                    hasLoadedFile = preview != null || result != null,
                    onClick = {
                        if (preview != null || result != null) viewModel.resetForNewFile()
                        picker.launch(IMPORT_MIME_TYPES)
                    }
                )
            }
            fileName?.let { name -> item { SentenceFileNameCard(name) } }
            remainingCapacity?.let { capacity -> item { SentenceCapacityChip(capacity) } }
            item { SentenceImportFormatCard() }
            if (isLoading) item { SentenceImportLoadingRow() }
            message?.let { text -> item { Text(text, color = Danger, fontWeight = FontWeight.Bold) } }
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
                        item { SentenceQuizIncompatibleWarning(currentPreview.quizIncompatibleCount) }
                    }
                    item {
                        SentenceRegisterButton(
                            count = currentPreview.newCount,
                            enabled = currentPreview.newCount > 0 && !isLoading,
                            onClick = viewModel::registerPreview
                        )
                    }
                }
                if (currentPreview.newSentences.isNotEmpty()) {
                    item { ImportSectionTitle("登録予定 (${currentPreview.newCount}件)", Success) }
                    items(currentPreview.newSentences) { sentence -> SentenceImportRow(sentence) }
                }
                if (currentPreview.duplicateSentences.isNotEmpty()) {
                    item { ImportSectionTitle("重複スキップ (${currentPreview.duplicateCount}件)", Gold) }
                    items(currentPreview.duplicateSentences.take(20)) { sentence -> SentenceImportRow(sentence) }
                    if (currentPreview.duplicateSentences.size > 20) {
                        item { OmittedRowsText("... 他${currentPreview.duplicateSentences.size - 20}件は省略", Gold) }
                    }
                }
                if (currentPreview.errors.isNotEmpty()) {
                    item { ImportSectionTitle("エラー (${currentPreview.errorCount}件)", Danger) }
                    items(currentPreview.errors.take(20)) { error -> SentenceImportErrorRow(error) }
                    if (currentPreview.errors.size > 20) {
                        item { OmittedRowsText("... 他${currentPreview.errors.size - 20}件は省略", Danger) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SentenceFilePickerButton(hasLoadedFile: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(
            if (hasLoadedFile) stringResource(R.string.sentence_import_change_file) else stringResource(R.string.sentence_import_pick_file),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SentenceRegisterButton(count: Int, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Success),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.sentence_import_register_btn, count), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

private val IMPORT_MIME_TYPES = arrayOf(
    "text/*",
    "text/csv",
    "application/csv",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/octet-stream",
    "*/*"
)
