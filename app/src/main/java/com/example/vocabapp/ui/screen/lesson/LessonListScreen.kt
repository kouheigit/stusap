package com.example.vocabapp

import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.domain.model.LessonStatus
import com.example.vocabapp.viewmodel.LessonListViewModel


@Composable
internal fun LessonListScreen(navController: NavHostController, viewModel: LessonListViewModel = hiltViewModel()) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    BlueScaffold(title = "レッスン", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        GramSecondaryButton(
                            text = "単語登録",
                            icon = Icons.Default.Add,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Route.AddWord.path) }
                        )
                        GramSecondaryButton(
                            text = "登録一覧",
                            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Route.CustomWordList.path) }
                        )
                    }
                    GramPrimaryButton(
                        text = "カスタム単語クイズ",
                        icon = Icons.Default.School,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate(Route.customTraining("word")) },
                    )
                }
            }
            val grouped = lessons.groupBy { it.scoreTarget }
            grouped.forEach { (score, items) ->
                item { SectionTitle("目標${score}点") }
                itemsIndexed(items, key = { _, lesson -> lesson.id }) { index, lesson ->
                    LessonPathRow(
                        lesson = lesson,
                        label = (index + 1).toString(),
                        showConnector = index < items.lastIndex,
                        onClick = { navController.navigate(Route.training(lesson.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonPathRow(
    lesson: Lesson,
    label: String,
    showConnector: Boolean,
    onClick: () -> Unit
) {
    val mastered = lesson.status == LessonStatus.Master
    val active = !mastered && (lesson.status == LessonStatus.InProgress || lesson.progressRate > 0f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GramLessonPathNode(label = label, active = active, completed = mastered)
            if (showConnector) {
                GramPathConnector(Modifier.padding(top = 6.dp))
            }
        }
        GramCard(modifier = Modifier.clickable(onClick = onClick)) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${lesson.wordStartNumber}〜${lesson.wordEndNumber}語",
                        color = DeepBlue,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("学習状態  ${lesson.status.label()}", color = TextMuted, fontSize = 13.sp)
                    GramProgressBar(lesson.progressRate)
                }
                if (mastered) {
                    MasterBadge("Master")
                } else {
                    GramPrimaryButton(text = "スタート", modifier = Modifier.width(112.dp), onClick = onClick)
                }
            }
        }
    }
}
