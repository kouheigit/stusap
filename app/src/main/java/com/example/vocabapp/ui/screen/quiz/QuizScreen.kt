package com.example.vocabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.viewmodel.QuizViewModel


@Composable
internal fun QuizScreen(navController: NavHostController, viewModel: QuizViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QuizTimerLifecycleEffect(
        onStart = viewModel::onScreenStarted,
        onStop = viewModel::onScreenStopped
    )
    LaunchedEffect(state.finishedAttemptId) {
        state.finishedAttemptId?.let {
            navController.navigate(Route.result(it)) {
                popUpTo(Route.Home.path)
            }
        }
    }
    BlueScaffold(title = if (state.isReview) "復習クイズ" else "10問クイズ", onBack = { navController.popBackStack() }) { inner ->
        if (state.questions.isEmpty()) {
            EmptyMessage(
                modifier = Modifier.padding(inner).background(BrightBlue),
                title = "出題できる単語がありません",
                button = "戻る",
                onClick = { navController.popBackStack() }
            )
        } else {
            QuizContent(Modifier.padding(inner), state, viewModel::submit)
        }
    }
}
