package com.example.vocabapp.ui.screen.passage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vocabapp.domain.model.PassageSet
import com.example.vocabapp.ui.screen.common.BlueScaffold
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.Success
import com.example.vocabapp.ui.theme.TextDark
import com.example.vocabapp.ui.theme.TextMuted
import com.example.vocabapp.viewmodel.CustomPassageRegistrationViewModel

@Composable
internal fun CustomPassageRegistrationScreen(
    onBack: () -> Unit,
    onSaved: (Int) -> Unit,
    viewModel: CustomPassageRegistrationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.savedId) {
        state.savedId?.let { id ->
            viewModel.consumeSavedId()
            onSaved(id)
        }
    }

    BlueScaffold(title = "長文問題登録", onBack = onBack) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(BrightBlue),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                RegistrationFormatCard()
            }
            item {
                OutlinedTextField(
                    value = state.rawText,
                    onValueChange = viewModel::onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    label = { Text("問題を貼り付け") },
                    placeholder = { Text(SAMPLE_TEXT) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                ManualPassageBaseFields(
                    title = state.manualTitle,
                    documentType = state.manualDocumentType,
                    timeLimitSec = state.manualTimeLimitSec,
                    body = state.manualBody,
                    onTitleChange = viewModel::updateManualTitle,
                    onDocumentTypeChange = viewModel::updateManualDocumentType,
                    onTimeLimitChange = viewModel::updateManualTimeLimitSec,
                    onBodyChange = viewModel::updateManualBody
                )
            }
            item {
                ManualQuestionSetupCard(
                    stem = state.currentQuestionStem,
                    choiceCount = state.currentChoiceCount,
                    onStemChange = viewModel::updateCurrentQuestionStem,
                    onChoiceCountChange = viewModel::updateCurrentChoiceCount
                )
            }
            state.errorMessage?.let { message ->
                item {
                    StatusCard(message = message, color = Danger)
                }
            }
            state.preview?.let { preview ->
                item {
                    PassagePreviewCard(preview)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = viewModel::preview,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Preview, contentDescription = null)
                        Spacer(Modifier.padding(3.dp))
                        Text("プレビュー")
                    }
                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.padding(3.dp))
                        Text(if (state.isSaving) "保存中" else "保存")
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationFormatCard() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("貼り付け形式", color = DeepBlue, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text("TITLE / TYPE / TIME_LIMIT と、本文・Q・A-D・ANSWER・EXPLANATION をまとめて貼れます。", color = TextDark, fontSize = 13.sp)
            Text(SAMPLE_TEXT, color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun ManualQuestionSetupCard(
    stem: String,
    choiceCount: Int,
    onStemChange: (String) -> Unit,
    onChoiceCountChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("設題の設定", color = DeepBlue, fontWeight = FontWeight.Black, fontSize = 16.sp)
            OutlinedTextField(
                value = stem,
                onValueChange = onStemChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("設題 1題") },
                shape = RoundedCornerShape(8.dp)
            )
            ChoiceCountSelector(
                choiceCount = choiceCount,
                onChoiceCountChange = onChoiceCountChange
            )
        }
    }
}

@Composable
private fun ChoiceCountSelector(
    choiceCount: Int,
    onChoiceCountChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, shape = RoundedCornerShape(8.dp)) {
            Text("${choiceCount}択にする")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (2..4).forEach { count ->
                DropdownMenuItem(
                    text = { Text("${count}択") },
                    onClick = {
                        expanded = false
                        onChoiceCountChange(count)
                    }
                )
            }
        }
    }
}

@Composable
private fun ManualPassageBaseFields(
    title: String,
    documentType: String,
    timeLimitSec: String,
    body: String,
    onTitleChange: (String) -> Unit,
    onDocumentTypeChange: (String) -> Unit,
    onTimeLimitChange: (String) -> Unit,
    onBodyChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("手入力で登録", color = DeepBlue, fontWeight = FontWeight.Black, fontSize = 16.sp)
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("タイトル") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("article", "email", "notice").forEach { type ->
                    OutlinedButton(
                        onClick = { onDocumentTypeChange(type) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = type,
                            color = if (documentType == type) DeepBlue else TextMuted,
                            fontWeight = if (documentType == type) FontWeight.Black else FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            OutlinedTextField(
                value = timeLimitSec,
                onValueChange = onTimeLimitChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("制限時間（秒）") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
                value = body,
                onValueChange = onBodyChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                label = { Text("本文") },
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun PassagePreviewCard(set: PassageSet) {
    val document = set.documents.first()
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(document.title ?: "長文問題", color = DeepBlue, fontWeight = FontWeight.Black, fontSize = 17.sp)
            Text("種類: ${document.kind.name.lowercase()} / 設問: ${set.questions.size}問 / 制限時間: ${set.timeLimitSec ?: 300}秒", color = TextMuted, fontSize = 12.sp)
            Text(document.body, color = TextDark, fontSize = 13.sp, maxLines = 4)
            set.questions.firstOrNull()?.let { question ->
                Text(question.stem, color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("正解: ${('A' + question.answerIndex)}", color = Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatusCard(message: String, color: Color) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp)
        )
    }
}

private val SAMPLE_TEXT = """
TITLE: Email about schedule change
TYPE: email
TIME_LIMIT: 300

本文:
Dear members,

Q1: What is the main purpose of this email?
A. To cancel every class
B. To announce a schedule change
C. To introduce a new teacher
D. To sell tickets
ANSWER: B
EXPLANATION: The email tells members about a schedule change.
""".trimIndent()
