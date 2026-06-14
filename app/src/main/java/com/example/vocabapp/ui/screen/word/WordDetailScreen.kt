package com.example.vocabapp

import com.example.vocabapp.ui.theme.Danger

import com.example.vocabapp.ui.theme.Success

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.TextDark

import com.example.vocabapp.ui.theme.SoftBlue

import com.example.vocabapp.ui.theme.AccentBlue

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.theme.DeepBlue

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.vocabapp.viewmodel.WordDetailViewModel


@Composable
internal fun WordDetailScreen(navController: NavHostController, viewModel: WordDetailViewModel = hiltViewModel()) {
    val word by viewModel.word.collectAsStateWithLifecycle()
    val relations by viewModel.relations.collectAsStateWithLifecycle()
    val speaker = rememberSpeaker()
    BlueScaffold(title = "単語詳細", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                word?.let {
                    GramCard {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(it.english, fontSize = 34.sp, fontWeight = FontWeight.Black, color = DeepBlue, modifier = Modifier.weight(1f))
                                IconButton(onClick = { speaker.speak(it.english) }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Audio", tint = BrightBlue) }
                            }
                            Text(it.phonetic, color = TextMuted, fontSize = 18.sp)
                            Text("${it.partOfSpeech}  ${it.meaning}", color = TextDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text(it.exampleSentence, color = TextDark, fontSize = 18.sp)
                            Text(it.exampleTranslation, color = TextMuted, fontSize = 16.sp)
                            if (relations.isNotEmpty()) {
                                Text("関連語", color = TextMuted, fontWeight = FontWeight.Bold)
                                relations.forEach { rel -> Text("${rel.relatedWord}: ${rel.relatedMeaning}", color = TextDark) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                GramSecondaryButton(
                                    text = if (it.isFavorite) "お気に入り中" else "お気に入り",
                                    icon = if (it.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    onClick = { viewModel.setFavorite(!it.isFavorite) },
                                    modifier = Modifier.weight(1f)
                                )
                                GramSecondaryButton(
                                    text = if (it.isLearned) "学習済み" else "未学習",
                                    icon = Icons.Default.Check,
                                    onClick = { viewModel.setLearned(!it.isLearned) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            GramPrimaryButton(
                                text = "復習対象に追加",
                                icon = Icons.Default.BookmarkBorder,
                                onClick = viewModel::addReview,
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = AccentBlue
                            )
                        }
                    }
                } ?: CircularProgressIndicator()
            }
        }
    }
}
