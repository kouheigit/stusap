package com.example.vocabapp

import com.example.vocabapp.ui.theme.Danger

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.SoftBlue

import com.example.vocabapp.ui.theme.BrightBlue

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.viewmodel.SettingsViewModel


@Composable
internal fun SettingsScreen(navController: NavHostController, viewModel: SettingsViewModel = hiltViewModel()) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteCustomWordsDialog by remember { mutableStateOf(false) }
    var showDeleteSentenceDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("学習進捗をリセット") },
            text = {
                Text(
                    "すべての学習進捗・クイズ履歴・復習リストを削除します。\n" +
                        "登録したカスタム単語・熟語・文章も削除されます。\n" +
                        "この操作は元に戻せません。"
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetProgress(); showResetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text("リセット") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) { Text("キャンセル") }
            }
        )
    }

    if (showDeleteCustomWordsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCustomWordsDialog = false },
            title = { Text("カスタム単語・熟語を全削除") },
            text = { Text("登録したすべてのカスタム単語・熟語を削除します。\nこの操作は元に戻せません。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllCustomWordsAndIdioms()
                        showDeleteCustomWordsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text("削除") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteCustomWordsDialog = false }) { Text("キャンセル") }
            }
        )
    }

    if (showDeleteSentenceDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSentenceDialog = false },
            title = { Text("カスタム文章を全削除") },
            text = { Text("登録したすべての文章問題を削除します。\nこの操作は元に戻せません。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAllCustomSentences(); showDeleteSentenceDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text("削除") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteSentenceDialog = false }) { Text("キャンセル") }
            }
        )
    }

    BlueScaffold(title = "設定", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("アプリ情報", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Text("TOEIC向け英単語・英熟語を、10問単位の4択クイズで学習するローカル保存型アプリです。", color = TextMuted)
                    Text("英単語 100語（Lesson 1〜4） / 英熟語 30語（Lesson 1）", color = TextMuted, fontSize = 13.sp)
                    Text("バージョン ${BuildConfig.VERSION_NAME}", color = TextMuted, fontSize = 13.sp)
                }
            }
            Text("データ管理", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(4.dp)) {
                    OutlinedButton(
                        onClick = { showDeleteCustomWordsDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = null
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text("カスタム単語・熟語を全削除", color = Danger, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = TextMuted.copy(alpha = 0.1f))
                    OutlinedButton(
                        onClick = { showDeleteSentenceDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = null
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text("カスタム文章を全削除", color = Danger, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = TextMuted.copy(alpha = 0.1f))
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = null
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text("学習進捗をリセット", color = Danger, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
