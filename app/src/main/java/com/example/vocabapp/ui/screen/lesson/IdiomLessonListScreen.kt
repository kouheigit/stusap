package com.example.vocabapp

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.viewmodel.IdiomLessonListViewModel


@Composable
internal fun IdiomLessonListScreen(navController: NavHostController, viewModel: IdiomLessonListViewModel = hiltViewModel()) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val totalWords = lessons.sumOf { it.wordEndNumber - it.wordStartNumber + 1 }
    BlueScaffold(title = "英熟語", onBack = { navController.popBackStack() }) { inner ->
        if (lessons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(inner), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("読み込み中…", color = Color.White, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("カスタム英熟語")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate(Route.AddIdiom.path) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                                Spacer(Modifier.width(4.dp))
                                Text("英熟語登録", color = DeepBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { navController.navigate(Route.CustomIdiomList.path) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = BrightBlue)
                                Spacer(Modifier.width(4.dp))
                                Text("登録一覧", color = DeepBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = { navController.navigate(Route.customTraining("idiom")) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("カスタム英熟語クイズ", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { SectionTitle("英熟語レッスン（全${totalWords}語）") }
                items(lessons) { lesson ->
                    LessonCard(lesson) { navController.navigate(Route.training(lesson.id)) }
                }
            }
        }
    }
}
