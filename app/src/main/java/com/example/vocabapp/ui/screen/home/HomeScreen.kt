package com.example.vocabapp

import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.Gold
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.Teal
import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    Scaffold(
        containerColor = SoftBlue,
        bottomBar = { GramBottomNavigation(navController, activeRoute = Route.Home.path) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(SoftBlue),
            contentPadding = PaddingValues(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { HomeHero() }
            item {
                HomeStatsGrid(
                    totalStudy = formatSeconds(summary.totalStudySeconds),
                    weekStudy = formatSeconds(summary.weekStudySeconds),
                    wordMaster = "${summary.masteredLessons}",
                    idiomMaster = "${summary.idiomMasteredLessons}",
                    reviewCount = "${summary.reviewCount}",
                    streakDays = "${summary.streakDays}日",
                    sentenceCount = "${summary.sentenceCount}件"
                )
            }
            item {
                HomeFeatureGrid(
                    features = listOf(
                        HomeFeature("長文問題", Icons.Default.PlayArrow) { navController.navigate(Route.quiz()) },
                        HomeFeature("長文問題登録", Icons.Default.Add) { navController.navigate(Route.CustomPassageRegistration.path) },
                        HomeFeature("登録済み長文問題", Icons.AutoMirrored.Filled.FormatListBulleted) { navController.navigate(Route.CustomPassageList.path) },
                        HomeFeature(stringResource(R.string.home_add_word_title), Icons.Default.Add) { navController.navigate(Route.AddWord.path) },
                        HomeFeature(stringResource(R.string.home_bulk_import_title), Icons.AutoMirrored.Filled.FormatListBulleted) { navController.navigate(Route.BulkImport.path) },
                        HomeFeature(stringResource(R.string.home_custom_word_title), Icons.AutoMirrored.Filled.FormatListBulleted) { navController.navigate(Route.customTraining(ContentType.WORD.routeValue)) },
                        HomeFeature(stringResource(R.string.home_custom_idiom_title), Icons.Default.School) { navController.navigate(Route.customTraining(ContentType.IDIOM.routeValue)) },
                        HomeFeature(stringResource(R.string.home_sentence_title), Icons.AutoMirrored.Filled.FormatListBulleted) { navController.navigate(Route.SentenceMenu.path) },
                        HomeFeature(stringResource(R.string.home_random_title), Icons.Default.PlayArrow) { navController.navigate(Route.RandomCustomMenu.path) },
                        HomeFeature(stringResource(R.string.home_review_title), Icons.Default.Refresh) { navController.navigate(Route.Review.path) },
                        HomeFeature(stringResource(R.string.home_study_log_label), Icons.Default.School) { navController.navigate(Route.StudyLog.path) },
                        HomeFeature(stringResource(R.string.home_settings_label), Icons.Default.Settings) { navController.navigate(Route.Settings.path) }
                    )
                )
            }
        }
    }
}

private data class HomeFeature(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun HomeHero() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GramRewardPill("3", BrightBlue)
                Spacer(Modifier.size(6.dp))
                GramRewardPill("120", Teal)
                Spacer(Modifier.size(6.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = stringResource(R.string.home_title),
                color = DeepBlue,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(R.string.home_training_subtitle),
                color = DeepBlue.copy(alpha = 0.78f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            AnimatedMascot(
                mood = MascotMood.Wave,
                size = 82.dp,
                message = "今日も一緒にがんばりましょう！"
            )
        }
    }
}

@Composable
private fun HomeStatsGrid(
    totalStudy: String,
    weekStudy: String,
    wordMaster: String,
    idiomMaster: String,
    reviewCount: String,
    streakDays: String,
    sentenceCount: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                listOf(
                    stringResource(R.string.stat_total_study) to totalStudy,
                    stringResource(R.string.stat_this_week) to weekStudy,
                    stringResource(R.string.stat_word_master) to wordMaster
                ),
                listOf(
                    stringResource(R.string.stat_idiom_master) to idiomMaster,
                    stringResource(R.string.stat_review_count) to reviewCount,
                    stringResource(R.string.stat_streak_days) to streakDays
                )
            ).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (label, value) ->
                        GramMiniStat(label = label, value = value, modifier = Modifier.weight(1f))
                    }
                }
            }
            GramMiniStat(
                label = stringResource(R.string.stat_sentence_count_label),
                value = sentenceCount,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HomeFeatureGrid(features: List<HomeFeature>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        features.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { feature ->
                    HomeFeatureTile(feature, Modifier.weight(1f))
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeFeatureTile(feature: HomeFeature, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(62.dp)
            .clickable(onClick = feature.onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(BrightBlue.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(feature.icon, contentDescription = null, tint = BrightBlue, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = feature.title,
                color = DeepBlue,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
