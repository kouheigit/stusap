package com.example.vocabapp

import com.example.vocabapp.ui.theme.Danger

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.SoftBlue

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.theme.DeepBlue

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.repository.MAX_CUSTOM_ENGLISH_CHARS
import com.example.vocabapp.data.repository.MAX_CUSTOM_MEANING_CHARS
import com.example.vocabapp.viewmodel.AddIdiomViewModel
import com.example.vocabapp.viewmodel.CustomIdiomListViewModel


@Composable
internal fun CustomIdiomListScreen(navController: NavHostController, viewModel: CustomIdiomListViewModel = hiltViewModel()) {
    val idioms by viewModel.idioms.collectAsStateWithLifecycle()
    BlueScaffold(title = "登録英熟語一覧 (${idioms.size}語)", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (idioms.isEmpty()) {
                item { EmptyCard("登録された英熟語はありません\n「英熟語登録」から追加してください") }
            } else {
                val chunks = idioms.chunked(10)
                chunks.forEachIndexed { idx, chunk ->
                    val start = idx * 10 + 1
                    val end = start + chunk.size - 1
                    val preview = buildSectionPreview(chunk.map { it.english })
                    item(key = "idiom_header_$idx") { ListSectionHeader(start = start, end = end, preview = preview, showDivider = idx > 0) }
                    items(chunk, key = { it.id }) { idiom ->
                        CustomIdiomRow(idiom = idiom, onDelete = { viewModel.delete(idiom.id) })
                    }
                }
            }
        }
    }
}

@Composable
internal fun CustomIdiomRow(idiom: CustomIdiomEntity, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(idiom.english, color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(idiom.meaning, color = TextMuted, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Danger)
            }
        }
    }
}

@Composable
internal fun AddIdiomScreen(navController: NavHostController, viewModel: AddIdiomViewModel = hiltViewModel()) {
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    var english by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }
    var meaningInput by remember { mutableStateOf<EditText?>(null) }
    LaunchedEffect(saved) {
        if (saved) { viewModel.resetSaved(); navController.popBackStack() }
    }
    BlueScaffold(title = "英熟語登録", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AddWordCardPadding), verticalArrangement = Arrangement.spacedBy(AddWordCardSpacing)) {
                    AddWordField(
                        label = "英熟語",
                        placeholder = "例: as soon as possible, keep in mind",
                        value = english,
                        onValueChange = { english = it.take(MAX_CUSTOM_ENGLISH_CHARS) },
                        imeAction = EditorInfo.IME_ACTION_NEXT,
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
                        autoFocus = true,
                        onImeAction = { meaningInput?.focusAndShowKeyboard() },
                    )
                    AddWordField(
                        label = "日本語の意味",
                        placeholder = "例: できるだけ早く、心に留めておく",
                        value = meaning,
                        onValueChange = { meaning = it.take(MAX_CUSTOM_MEANING_CHARS) },
                        imeAction = EditorInfo.IME_ACTION_DONE,
                        onReady = { meaningInput = it },
                    )
                    Button(
                        onClick = { viewModel.save(english, meaning) },
                        enabled = english.isNotBlank() && meaning.isNotBlank(),
                        modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.65f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue)
                    ) {
                        Text("登録する", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
