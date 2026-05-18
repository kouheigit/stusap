package com.example.vocabapp

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.viewmodel.FlashcardViewModel
import kotlinx.coroutines.delay


@Composable
internal fun FlashcardScreen(navController: NavHostController, viewModel: FlashcardViewModel = hiltViewModel()) {
    val words by viewModel.words.collectAsStateWithLifecycle()
    val index by viewModel.index.collectAsStateWithLifecycle()
    val revealed by viewModel.revealed.collectAsStateWithLifecycle()
    val speaker = rememberSpeaker()
    val word = words.getOrNull(index)
    val title = if (viewModel.trainingId >= 100) "英熟語帳" else "単語帳"
    // 単語帳でもカード切り替え時に自動読み上げ
    LaunchedEffect(word?.id, speaker.isReady) {
        if (word == null) return@LaunchedEffect
        delay(150L)
        speaker.speak(word.english)
    }
    BlueScaffold(title = title, onBack = { navController.popBackStack() }) { inner ->
        if (words.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${index + 1} / ${words.size}", color = TextMuted, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { (index + 1) / words.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = AccentBlue, trackColor = Color.White
                )
                word?.let { w ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().weight(1f).clickable { viewModel.toggleReveal() },
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            Modifier.fillMaxSize().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(w.english, fontSize = 38.sp, fontWeight = FontWeight.Black, color = DeepBlue, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                IconButton(onClick = { speaker.speak(w.english) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "読み上げ", tint = BrightBlue)
                                }
                            }
                            Text(w.phonetic, color = TextMuted, fontSize = 18.sp)
                            if (revealed) {
                                Spacer(Modifier.height(20.dp))
                                Text(w.meaning, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Success, textAlign = TextAlign.Center)
                                if (w.partOfSpeech.isNotBlank()) Text(w.partOfSpeech, color = TextMuted, fontSize = 16.sp)
                                if (w.exampleSentence.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(w.exampleSentence, color = TextDark, fontSize = 15.sp, textAlign = TextAlign.Center)
                                    Text(w.exampleTranslation, color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                                }
                            } else {
                                Spacer(Modifier.height(24.dp))
                                Text("タップして意味を確認", color = TextMuted, fontSize = 16.sp)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = viewModel::prev,
                        enabled = index > 0,
                        modifier = Modifier.weight(1f).height(54.dp)
                    ) { Text("← 前へ", fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = viewModel::next,
                        enabled = index < words.lastIndex,
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue)
                    ) { Text("次へ →", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
