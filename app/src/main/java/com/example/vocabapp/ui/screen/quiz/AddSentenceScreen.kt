package com.example.vocabapp

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.vocabapp.data.repository.MAX_CUSTOM_MEANING_CHARS
import com.example.vocabapp.data.repository.MAX_CUSTOM_SENTENCE_CHARS
import com.example.vocabapp.viewmodel.AddSentenceViewModel


@Composable
internal fun AddSentenceScreen(
    navController: NavHostController,
    viewModel: AddSentenceViewModel = hiltViewModel()
) {
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    var sentence by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(saved) {
        if (saved) {
            viewModel.resetSaved()
            sentence = ""
            meaning = ""
        }
    }

    BlueScaffold(title = "文章登録", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(SoftBlue)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("英文", fontWeight = FontWeight.Bold, color = TextMuted)
                            val wordCount = sentence.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                            val bracketCount = "\\[([^\\]]+)\\]".toRegex().findAll(sentence).count()
                            val countLabel = when {
                                bracketCount > 0 -> "[語句] $bracketCount / 4"
                                sentence.isBlank() -> ""
                                else -> "${wordCount}語"
                            }
                            val countColor = when {
                                bracketCount > 0 && bracketCount < 4 -> Danger
                                bracketCount == 4 -> Success
                                wordCount in 1..5 && sentence.isNotBlank() -> Danger
                                wordCount >= 6 -> Success
                                else -> TextMuted
                            }
                            if (countLabel.isNotEmpty()) {
                                Text(countLabel, fontSize = 12.sp, color = countColor, fontWeight = FontWeight.Bold)
                            }
                        }
                        AddWordField(
                            label = "",
                            placeholder = "例: I [might][stay][as][well] as join in a tour",
                            value = sentence,
                            onValueChange = { sentence = it.take(MAX_CUSTOM_SENTENCE_CHARS) },
                            imeAction = EditorInfo.IME_ACTION_NEXT,
                            autoFocus = true
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("日本語の意味", fontWeight = FontWeight.Bold, color = TextMuted)
                        AddWordField(
                            label = "",
                            placeholder = "例: パッケージツアーに参加するよりも家にいた方がいい",
                            value = meaning,
                            onValueChange = { meaning = it.take(MAX_CUSTOM_MEANING_CHARS) },
                            imeAction = EditorInfo.IME_ACTION_DONE,
                            onImeAction = { viewModel.save(sentence, meaning) }
                        )
                    }
                    val wordCount2 = sentence.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                    val bracketCount2 = "\\[([^\\]]+)\\]".toRegex().findAll(sentence).count()
                    val isValidSentence = sentence.isNotBlank() && (
                        (bracketCount2 == 0 && wordCount2 >= 6) ||
                        bracketCount2 == 4
                    )
                    val validationMsg = when {
                        sentence.isBlank() -> null
                        bracketCount2 > 0 && bracketCount2 < 4 -> "[語句]は4つ必要です（現在${bracketCount2}つ）"
                        bracketCount2 > 4 -> "[語句]は4つまでにしてください"
                        bracketCount2 == 0 && wordCount2 < 6 -> "6語以上の英文が必要です（現在${wordCount2}語）"
                        else -> null
                    }
                    if (validationMsg != null) {
                        Text(validationMsg, color = Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.save(sentence, meaning) },
                        enabled = isValidSentence && meaning.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("登録する", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (saved) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Success),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("登録しました", color = Color.White, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = { navController.navigate(Route.CustomSentenceList.path) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            Text("一覧へ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("入力形式", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("① そのまま英文を入力（6語以上必要）", color = TextMuted, fontSize = 12.sp)
                    Text("   → 4語をランダムに自動で空白に変換", color = TextMuted, fontSize = 12.sp)
                    Text("② [語句]で語句を囲む（4つ必要）", color = TextMuted, fontSize = 12.sp)
                    Text("   → その語句が並べ替え対象になる", color = TextMuted, fontSize = 12.sp)
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))
                    Text("例② : I [might][stay][as][well] as join in a tour", color = AccentBlue, fontSize = 11.sp)
                }
            }
        }
    }
}
