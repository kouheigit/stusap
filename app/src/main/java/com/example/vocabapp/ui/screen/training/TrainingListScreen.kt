package com.example.vocabapp

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.viewmodel.TrainingListViewModel


@Composable
internal fun TrainingListScreen(navController: NavHostController, viewModel: TrainingListViewModel = hiltViewModel()) {
    val trainings by viewModel.trainings.collectAsStateWithLifecycle()
    val isIdiom = viewModel.lessonId >= 100
    val screenTitle = if (isIdiom) "英熟語 トレーニング" else "トレーニング一覧"
    val sectionTitle = if (isIdiom) "英熟語 トレーニング一覧" else "トレーニング一覧"
    BlueScaffold(title = screenTitle, onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionTitle(sectionTitle) }
            items(trainings) { training ->
                TrainingCard(
                    training = training,
                    onQuiz = { navController.navigate(Route.quiz(training.id)) },
                    onDetail = { wordId -> navController.navigate(Route.word(wordId)) },
                    onFlashcard = { navController.navigate(Route.flashcard(training.id)) }
                )
            }
        }
    }
}
