package com.example.vocabapp

import com.example.vocabapp.ui.theme.Danger

import com.example.vocabapp.ui.theme.Success

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.SoftBlue

import com.example.vocabapp.ui.theme.DeepBlue

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.viewmodel.CustomWordListViewModel


@Composable
internal fun CustomWordListScreen(navController: NavHostController, viewModel: CustomWordListViewModel = hiltViewModel()) {
    val words by viewModel.words.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(CustomWordFilter.All.name) }
    val selectedFilter = CustomWordFilter.valueOf(filter)
    val filteredWords = words.filter { word ->
        val matchesQuery = query.isBlank() ||
            word.english.contains(query, ignoreCase = true) ||
            word.meaning.contains(query, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            CustomWordFilter.All -> true
            CustomWordFilter.Favorite -> word.isFavorite
            CustomWordFilter.Learned -> word.isLearned
            CustomWordFilter.Unlearned -> !word.isLearned
        }
        matchesQuery && matchesFilter
    }
    BlueScaffold(title = "登録単語一覧 (${filteredWords.size}/${words.size}語)", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    label = { Text("単語・意味を検索") }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    CustomWordFilter.entries.forEach { item ->
                        FilterChip(
                            selected = selectedFilter == item,
                            onClick = { filter = item.name },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
            if (words.isEmpty()) {
                item { EmptyCard("登録された単語はありません\n「単語登録」から追加してください") }
            } else if (filteredWords.isEmpty()) {
                item { EmptyCard("条件に一致する単語はありません") }
            } else {
                val chunks = filteredWords.chunked(10)
                chunks.forEachIndexed { idx, chunk ->
                    val start = idx * 10 + 1
                    val end = start + chunk.size - 1
                    val preview = buildSectionPreview(chunk.map { it.english })
                    item(key = "word_header_$idx") { ListSectionHeader(start = start, end = end, preview = preview, showDivider = idx > 0) }
                    items(chunk, key = { it.id }) { word ->
                        CustomWordRow(
                            word = word,
                            onFavorite = { viewModel.setFavorite(word.id, !word.isFavorite) },
                            onLearned = { viewModel.setLearned(word.id, !word.isLearned) },
                            onDelete = { viewModel.delete(word.id) }
                        )
                    }
                }
            }
        }
    }
}

internal enum class CustomWordFilter(val label: String) {
    All("すべて"),
    Favorite("お気に入り"),
    Learned("学習済み"),
    Unlearned("未学習")
}

@Composable
internal fun ListSectionHeader(start: Int, end: Int, preview: String = "", showDivider: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (showDivider) {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = TextMuted.copy(alpha = 0.2f), thickness = 0.5.dp)
            Spacer(Modifier.height(4.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "[$start~$end]",
                color = DeepBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            if (preview.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Text(preview, color = TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun CustomWordRow(
    word: CustomWordEntity,
    onFavorite: () -> Unit,
    onLearned: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(word.english, color = DeepBlue, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(word.meaning, color = TextMuted, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    if (word.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "お気に入り",
                    tint = if (word.isFavorite) Danger else TextMuted
                )
            }
            IconButton(onClick = onLearned) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "学習済み",
                    tint = if (word.isLearned) Success else TextMuted
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Danger)
            }
        }
    }
}
