package com.example.vocabapp.ui.screen.passage

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
