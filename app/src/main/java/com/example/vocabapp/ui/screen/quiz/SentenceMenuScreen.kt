package com.example.vocabapp

import com.example.vocabapp.ui.theme.Gold

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.TextDark

import com.example.vocabapp.ui.theme.AccentBlue

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.theme.SoftBlue

import com.example.vocabapp.ui.theme.DeepBlue

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.vocabapp.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.viewmodel.CustomSentenceListViewModel


@Composable
internal fun SentenceMenuScreen(
    navController: NavHostController,
    viewModel: CustomSentenceListViewModel = hiltViewModel()
) {
    val sentences by viewModel.sentences.collectAsStateWithLifecycle()
    BlueScaffold(title = stringResource(R.string.home_sentence_title), onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AnimatedMascot(
                    mood = MascotMood.Idle,
                    size = 92.dp,
                    message = "文章問題で読解力をアップしましょう！"
                )
            }
            item {
                GramCard {
                    SentenceMenuActionRow(
                        onClick = { navController.navigate(Route.AddSentence.path) },
                        icon = Icons.Default.Add,
                        title = stringResource(R.string.sentence_menu_add_button),
                        subtitle = "新しい文章問題を登録する"
                    )
                }
            }
            item {
                GramCard {
                    SentenceMenuActionRow(
                        onClick = { navController.navigate(Route.CustomSentenceList.path) },
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        title = stringResource(R.string.sentence_menu_list_button),
                        subtitle = "登録済みの文章問題を確認する"
                    )
                }
            }
            item {
                GramCard {
                    SentenceMenuActionRow(
                        onClick = { navController.navigate(Route.SentenceImport.path) },
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        title = stringResource(R.string.home_sentence_import_title),
                        subtitle = stringResource(R.string.sentence_menu_import_hint)
                    )
                }
            }
            item {
                Text("100問ごとのまとまり", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            if (sentences.isEmpty()) {
                item { EmptyCard(stringResource(R.string.sentence_menu_start_empty)) }
            } else {
                val blocks = sentences.chunked(SENTENCE_BLOCK_SIZE)
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        blocks.take(3).forEachIndexed { blockIndex, block ->
                            val start = blockIndex * SENTENCE_BLOCK_SIZE + 1
                            val label = "${start}~${start + block.size - 1}問"
                            GramCard(modifier = Modifier.weight(1f)) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    GramCircularProgress(
                                        progress = (block.size / SENTENCE_BLOCK_SIZE.toFloat()).coerceIn(0f, 1f),
                                        label = "${(block.size * 100 / SENTENCE_BLOCK_SIZE)}%"
                                    )
                                    Text(label, color = DeepBlue, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    Text(if (block.size >= SENTENCE_BLOCK_SIZE) "Master" else "学習中", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                items(blocks.size) { blockIndex ->
                    val blockNumber = blockIndex + 1
                    val start = blockIndex * SENTENCE_BLOCK_SIZE + 1
                    val end = start + blocks[blockIndex].size - 1
                    CardButton(
                        title = "第${start}〜${end}問",
                        subtitle = "10問セット ${blocks[blockIndex].chunked(SENTENCE_SET_SIZE).size}個",
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        onClick = { navController.navigate(Route.sentenceTrainingBlock(blockNumber)) }
                    )
                }
                item {
                    SentenceStatsCard(sentences.size)
                }
            }
            item {
                Text(stringResource(R.string.sentence_menu_about_section), color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.sentence_menu_about_desc), color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text("A", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("英文をそのまま入力（6語以上）", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("→ 4語が自動で空白になります", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.15f)),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text("B", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("[語句]で4つを囲んで入力", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("→ その語句が並べ替え対象になります", color = TextMuted, fontSize = 12.sp)
                                Text("例: I [might][stay][as][well] as join in a tour", color = AccentBlue, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SentenceMenuActionRow(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(BrightBlue.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BrightBlue, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = TextMuted, fontSize = 13.sp)
        }
        Text("›", color = BrightBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun SentenceTrainingBlockScreen(
    navController: NavHostController,
    blockNumber: Int,
    viewModel: CustomSentenceListViewModel = hiltViewModel()
) {
    val sentences by viewModel.sentences.collectAsStateWithLifecycle()
    val startQuestion = (blockNumber - 1).coerceAtLeast(0) * SENTENCE_BLOCK_SIZE + 1
    val blockSentences = sentences.drop(startQuestion - 1).take(SENTENCE_BLOCK_SIZE)
    val titleEndQuestion = if (blockSentences.isEmpty()) {
        blockNumber * SENTENCE_BLOCK_SIZE
    } else {
        startQuestion + blockSentences.size - 1
    }
    BlueScaffold(title = "文章問題 ${startQuestion}〜${titleEndQuestion}", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionTitle("10問ごとのセット") }
            if (blockSentences.isEmpty()) {
                item { EmptyCard("この範囲の文章はありません") }
            } else {
                val sets = blockSentences.chunked(SENTENCE_SET_SIZE)
                items(sets.size) { setIndexInBlock ->
                    val setNumber = (blockNumber - 1).coerceAtLeast(0) * SENTENCE_SETS_PER_BLOCK + setIndexInBlock + 1
                    val setStart = (setNumber - 1) * SENTENCE_SET_SIZE + 1
                    val setEnd = setStart + sets[setIndexInBlock].size - 1
                    CardButton(
                        title = "第${setStart}〜${setEnd}問",
                        subtitle = "10問出題",
                        icon = Icons.Default.PlayArrow,
                        onClick = { navController.navigate(Route.sentenceQuiz(setNumber)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SentenceStatsCard(sentenceCount: Int) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.sentence_menu_stat_registered), color = TextMuted, fontSize = 12.sp)
                Text("$sentenceCount", color = DeepBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.sentence_menu_stat_bun), color = TextMuted, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.sentence_menu_stat_quizable), color = TextMuted, fontSize = 12.sp)
                Text("$sentenceCount", color = AccentBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.sentence_menu_stat_mon), color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

private const val SENTENCE_SET_SIZE = 10
private const val SENTENCE_SETS_PER_BLOCK = 10
private const val SENTENCE_BLOCK_SIZE = SENTENCE_SET_SIZE * SENTENCE_SETS_PER_BLOCK
