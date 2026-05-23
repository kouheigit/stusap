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
import androidx.compose.ui.res.stringResource
import com.example.vocabapp.R
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
            title = { Text(stringResource(R.string.settings_reset_title)) },
            text = { Text(stringResource(R.string.settings_reset_confirm)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetProgress(); showResetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text(stringResource(R.string.settings_action_reset)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.settings_action_cancel)) }
            }
        )
    }

    if (showDeleteCustomWordsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCustomWordsDialog = false },
            title = { Text(stringResource(R.string.settings_delete_words_title)) },
            text = { Text(stringResource(R.string.settings_delete_words_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllCustomWordsAndIdioms()
                        showDeleteCustomWordsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text(stringResource(R.string.settings_action_delete)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteCustomWordsDialog = false }) { Text(stringResource(R.string.settings_action_cancel)) }
            }
        )
    }

    if (showDeleteSentenceDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSentenceDialog = false },
            title = { Text(stringResource(R.string.settings_delete_sentence_title)) },
            text = { Text(stringResource(R.string.settings_delete_sentence_confirm)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAllCustomSentences(); showDeleteSentenceDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text(stringResource(R.string.settings_action_delete)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteSentenceDialog = false }) { Text(stringResource(R.string.settings_action_cancel)) }
            }
        )
    }

    BlueScaffold(title = stringResource(R.string.settings_screen_title), onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_app_info_title), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Text(stringResource(R.string.settings_app_info_body), color = TextMuted)
                    Text(stringResource(R.string.settings_app_content_count), color = TextMuted, fontSize = 13.sp)
                    Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME), color = TextMuted, fontSize = 13.sp)
                }
            }
            Text(stringResource(R.string.settings_data_management), color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                        Text(stringResource(R.string.settings_delete_words_title), color = Danger, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = TextMuted.copy(alpha = 0.1f))
                    OutlinedButton(
                        onClick = { showDeleteSentenceDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = null
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_delete_sentence_title), color = Danger, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = TextMuted.copy(alpha = 0.1f))
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = null
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_reset_title), color = Danger, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
