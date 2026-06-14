package com.example.vocabapp

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.SoftBlue

import com.example.vocabapp.ui.screen.common.*

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.data.repository.MAX_CUSTOM_ENGLISH_CHARS
import com.example.vocabapp.data.repository.MAX_CUSTOM_MEANING_CHARS
import com.example.vocabapp.viewmodel.AddWordViewModel


@Composable
internal fun AddWordScreen(navController: NavHostController, viewModel: AddWordViewModel = hiltViewModel()) {
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    var english by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }
    var showPreview by rememberSaveable { mutableStateOf(false) }
    var meaningInput by remember { mutableStateOf<EditText?>(null) }
    LaunchedEffect(saved) {
        if (saved) { viewModel.resetSaved(); navController.popBackStack() }
    }
    BlueScaffold(title = "新規単語登録", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AnimatedMascot(
                mood = MascotMood.Wave,
                size = 78.dp,
                message = stringResource(R.string.add_word_mascot)
            )
            GramCard {
                Column(Modifier.padding(AddWordCardPadding), verticalArrangement = Arrangement.spacedBy(AddWordCardSpacing)) {
                    AddWordField(
                        label = "英語",
                        placeholder = "例: apple, give up",
                        value = english,
                        onValueChange = { english = it.take(MAX_CUSTOM_ENGLISH_CHARS) },
                        imeAction = EditorInfo.IME_ACTION_NEXT,
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
                        autoFocus = true,
                        onImeAction = { meaningInput?.focusAndShowKeyboard() },
                    )
                    AddWordField(
                        label = "日本語",
                        placeholder = "例: りんご、美しい",
                        value = meaning,
                        onValueChange = { meaning = it.take(MAX_CUSTOM_MEANING_CHARS) },
                        imeAction = EditorInfo.IME_ACTION_DONE,
                        onReady = { meaningInput = it },
                    )
                    Text(
                        text = "スペースを含む場合は自動的にカスタム熟語として登録されます",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showPreview && (english.isNotBlank() || meaning.isNotBlank())) {
                        GramCard {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("プレビュー", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(english.ifBlank { "英語" }, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                Text(meaning.ifBlank { "日本語" }, color = TextMuted, fontSize = 15.sp)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        GramSecondaryButton(
                            text = "プレビュー",
                            icon = Icons.Default.Preview,
                            modifier = Modifier.weight(1f),
                            onClick = { showPreview = !showPreview }
                        )
                        GramPrimaryButton(
                            text = "保存",
                            icon = Icons.Default.Save,
                            enabled = english.isNotBlank() && meaning.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.save(english, meaning) }
                        )
                    }
                }
            }
        }
    }
}
