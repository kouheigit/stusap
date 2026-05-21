package com.example.vocabapp

import com.example.vocabapp.ui.theme.AccentBlue

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.theme.DeepBlue

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.viewmodel.LessonListViewModel


@Composable
internal fun LessonListScreen(navController: NavHostController, viewModel: LessonListViewModel = hiltViewModel()) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    BlueScaffold(title = "英単語", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { navController.navigate(Route.AddWord.path) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("単語登録", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { navController.navigate(Route.CustomWordList.path) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("登録一覧", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { navController.navigate(Route.customTraining("word")) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("カスタム単語クイズ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            val grouped = lessons.groupBy { it.scoreTarget }
            grouped.forEach { (score, items) ->
                item { SectionTitle("目標${score}点") }
                items(items) { lesson -> LessonCard(lesson) { navController.navigate(Route.training(lesson.id)) } }
            }
        }
    }
}

