package com.example.vocabapp

import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.Teal
import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Image(
                painter = painterResource(R.drawable.gram_home_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    // Keep the approved background art, but frame it so the castle stays visible
                    // above the home cards instead of sitting behind the speech bubble/stat panel.
                    .scale(scaleX = 1.42f, scaleY = 1.42f)
                    .offset(x = (-60).dp, y = (-165).dp),
                contentScale = ContentScale.FillBounds
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            HomeFeature("長文問題", R.drawable.gram_feature_passing_cutout) { navController.navigate(Route.quiz()) },
                            HomeFeature("長文問題登録", R.drawable.gram_feature_passage_register) { navController.navigate(Route.CustomPassageRegistration.path) },
                            HomeFeature("登録済み長文問題", R.drawable.gram_feature_passage_saved) { navController.navigate(Route.CustomPassageList.path) },
                            HomeFeature(stringResource(R.string.home_add_word_title), R.drawable.gram_feature_add_word) { navController.navigate(Route.AddWord.path) },
                            HomeFeature(stringResource(R.string.home_bulk_import_title), R.drawable.gram_feature_bulk_import) { navController.navigate(Route.BulkImport.path) },
                            HomeFeature(stringResource(R.string.home_custom_word_title), R.drawable.gram_feature_words) { navController.navigate(Route.customTraining(ContentType.WORD.routeValue)) },
                            HomeFeature(stringResource(R.string.home_custom_idiom_title), R.drawable.gram_feature_idioms) { navController.navigate(Route.customTraining(ContentType.IDIOM.routeValue)) },
                            HomeFeature(stringResource(R.string.home_sentence_title), R.drawable.gram_feature_sentence) { navController.navigate(Route.SentenceMenu.path) },
                            HomeFeature(stringResource(R.string.home_random_title), R.drawable.gram_feature_random) { navController.navigate(Route.RandomCustomMenu.path) },
                            HomeFeature(stringResource(R.string.home_review_title), R.drawable.gram_feature_review) { navController.navigate(Route.Review.path) },
                            HomeFeature(stringResource(R.string.home_study_log_label), R.drawable.gram_feature_study_log) { navController.navigate(Route.StudyLog.path) },
                            HomeFeature(stringResource(R.string.home_settings_label), R.drawable.gram_feature_settings) { navController.navigate(Route.Settings.path) }
                        )
                    )
                }
            }
        }
    }
}

private data class HomeFeature(
    val title: String,
    val imageResId: Int,
    val onClick: () -> Unit
)

private data class HomeStat(
    val label: String,
    val value: String,
    val imageResId: Int?
)

@Composable
private fun HomeHero() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(224.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 12.dp),
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
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFFFC837), modifier = Modifier.size(19.dp))
                }
            }
            Image(
                painter = painterResource(R.drawable.gram_home_title_cutout),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .width(218.dp),
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(R.drawable.gram_home_robot_cutout),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 18.dp, bottom = 22.dp)
                    .size(122.dp),
                contentScale = ContentScale.Fit
            )
            SpeechBubble(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp, top = 42.dp)
                    .width(188.dp),
                message = "今日も一緒に\nがんばりましょう！"
            )
        }
    }
}

@Composable
private fun SpeechBubble(modifier: Modifier = Modifier, message: String) {
    Box(modifier = modifier) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = message,
                color = DeepBlue,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp)
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
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            listOf(
                listOf(
                    HomeStat(stringResource(R.string.stat_total_study), totalStudy, R.drawable.gram_stat_total_time),
                    HomeStat(stringResource(R.string.stat_this_week), weekStudy, R.drawable.gram_stat_week),
                    HomeStat(stringResource(R.string.stat_word_master), wordMaster, R.drawable.gram_stat_word_master)
                ),
                listOf(
                    HomeStat(stringResource(R.string.stat_idiom_master), idiomMaster, R.drawable.gram_stat_idiom_master),
                    HomeStat(stringResource(R.string.stat_review_count), reviewCount, R.drawable.gram_stat_review_words),
                    HomeStat(stringResource(R.string.stat_streak_days), streakDays, R.drawable.gram_stat_streak)
                )
            ).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    row.forEach { stat ->
                        HomeStatCell(stat = stat, modifier = Modifier.weight(1f))
                    }
                }
            }
            HomeStatCell(
                stat = HomeStat(stringResource(R.string.stat_sentence_count_label), sentenceCount, null),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HomeStatCell(stat: HomeStat, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(64.dp)
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stat.imageResId?.let { imageResId ->
            Card(
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.size(24.dp)
            ) {
                Image(
                    painter = painterResource(imageResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(5.dp))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stat.label, color = TextMuted, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(stat.value, color = DeepBlue, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
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
            .height(68.dp)
            .clickable(onClick = feature.onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Image(
            painter = painterResource(feature.imageResId),
            contentDescription = feature.title,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Fit
        )
    }
}
