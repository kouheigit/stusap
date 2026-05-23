package com.example.vocabapp

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.viewmodel.HomeViewModel


@Composable
internal fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel = hiltViewModel()) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    BlueScaffold(
        title = stringResource(R.string.home_title),
        actions = {
            IconButton(onClick = { navController.navigate(Route.Settings.path) }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.home_training_subtitle),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(stringResource(R.string.stat_total_study), formatSeconds(summary.totalStudySeconds), Modifier.weight(1f))
                    StatCard(stringResource(R.string.stat_this_week), formatSeconds(summary.weekStudySeconds), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(stringResource(R.string.stat_word_master), "${summary.masteredLessons}/${summary.totalLessons}", Modifier.weight(1f))
                    StatCard(stringResource(R.string.stat_idiom_master), "${summary.idiomMasteredLessons}/${summary.idiomTotalLessons}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(stringResource(R.string.stat_review_count), "${summary.reviewCount}", Modifier.weight(1f))
                    StatCard(stringResource(R.string.stat_streak_days), "${summary.streakDays}日", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                StatCard(stringResource(R.string.stat_sentence_count_label), "${summary.sentenceCount}文", Modifier.fillMaxWidth())
            }
            item {
                CardButton(
                    title = stringResource(R.string.home_add_word_title),
                    subtitle = stringResource(R.string.home_add_word_subtitle),
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate(Route.AddWord.path) }
                )
            }
            item {
                CardButton(
                    title = stringResource(R.string.home_import_title),
                    subtitle = stringResource(R.string.home_import_subtitle),
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = { navController.navigate(Route.WordImport.path) }
                )
            }
            item {
                CardButton(
                    title = stringResource(R.string.home_custom_word_title),
                    subtitle = stringResource(R.string.home_custom_word_subtitle),
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = {
                        navController.navigate(Route.customTraining(ContentType.WORD.routeValue))
                    }
                )
            }
            item {
                CardButton(
                    title = stringResource(R.string.home_custom_idiom_title),
                    subtitle = stringResource(R.string.home_custom_idiom_subtitle),
                    icon = Icons.Default.School,
                    onClick = {
                        navController.navigate(Route.customTraining(ContentType.IDIOM.routeValue))
                    }
                )
            }
            item {
                CardButton(
                    title = stringResource(R.string.home_sentence_title),
                    subtitle = if (summary.sentenceCount > 0)
                        stringResource(R.string.home_sentence_subtitle_with_count, summary.sentenceCount)
                    else
                        stringResource(R.string.home_sentence_subtitle),
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = { navController.navigate(Route.SentenceMenu.path) }
                )
            }
            item {
                CardButton(
                    title = stringResource(R.string.home_random_title),
                    subtitle = stringResource(R.string.home_random_subtitle),
                    icon = Icons.Default.PlayArrow,
                    onClick = { navController.navigate(Route.RandomCustomMenu.path) }
                )
            }
            item {
                CardButton(
                    title = stringResource(R.string.home_sentence_import_title),
                    subtitle = stringResource(R.string.home_sentence_import_subtitle),
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = { navController.navigate(Route.SentenceImport.path) }
                )
            }
            item {
                CardButton(
                    title = stringResource(R.string.home_review_title),
                    subtitle = stringResource(R.string.home_review_subtitle),
                    icon = Icons.Default.Refresh,
                    onClick = { navController.navigate(Route.Review.path) }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    BottomAction("学習ログ", Icons.Default.School, Modifier.weight(1f)) { navController.navigate(Route.StudyLog.path) }
                    BottomAction("設定", Icons.Default.Settings, Modifier.weight(1f)) { navController.navigate(Route.Settings.path) }
                }
            }
        }
    }
}
