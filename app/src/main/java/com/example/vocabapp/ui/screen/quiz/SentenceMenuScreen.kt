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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
    BlueScaffold(title = "文章問題", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { navController.navigate(Route.AddSentence.path) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("文章登録", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { navController.navigate(Route.CustomSentenceList.path) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("登録一覧", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Button(
                    onClick = { navController.navigate(Route.SentenceImport.path) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("文章Excel / CSV一括取込", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Button(
                    onClick = { navController.navigate(Route.SentenceQuiz.path) },
                    enabled = sentences.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (sentences.isEmpty()) "文章を登録してから開始できます"
                        else "文章問題を開始（${sentences.size}文登録済み）",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (sentences.isNotEmpty()) {
                item {
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
                                Text("登録文章", color = TextMuted, fontSize = 12.sp)
                                Text("${sentences.size}", color = DeepBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                Text("文", color = TextMuted, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("出題可能", color = TextMuted, fontSize = 12.sp)
                                Text("${sentences.size}", color = AccentBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                Text("問", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            item {
                SectionTitle("文章問題について")
            }
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("登録した英文から並べ替え問題を自動生成します", color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
