package com.example.vocabapp

import com.example.vocabapp.ui.screen.common.*

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.viewmodel.ReviewViewModel
import com.example.vocabapp.viewmodel.StudyLogViewModel


@Composable
internal fun ReviewScreen(navController: NavHostController, viewModel: ReviewViewModel = hiltViewModel()) {
    val words by viewModel.words.collectAsStateWithLifecycle()
    BlueScaffold(title = "復習", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = { navController.navigate(Route.quiz(isReview = true)) },
                    enabled = words.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("復習クイズを開始", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (words.isEmpty()) {
                item { EmptyCard("復習対象の単語はまだありません") }
            } else {
                items(words) { word ->
                    WordRow(
                        word = word,
                        action = {
                            IconButton(onClick = { viewModel.remove(word.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Danger)
                            }
                        },
                        onClick = { navController.navigate(Route.word(word.id)) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun StudyLogScreen(navController: NavHostController, viewModel: StudyLogViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    BlueScaffold(title = "学習ログ", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (logs.isEmpty()) {
                item { EmptyCard("学習ログはまだありません") }
            } else {
                items(logs) { log ->
                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(formatDate(log.studiedAt), fontWeight = FontWeight.Bold, color = TextDark)
                                Text("正解 ${log.correctCount} / 不正解 ${log.wrongCount}", color = TextMuted)
                            }
                            Text(formatSeconds(log.studySeconds), color = DeepBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
