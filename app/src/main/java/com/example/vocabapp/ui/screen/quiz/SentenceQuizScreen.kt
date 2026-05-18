package com.example.vocabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.viewmodel.SentenceQuizViewModel
import kotlinx.coroutines.delay

@Composable
internal fun SentenceQuizScreen(
    navController: NavHostController,
    viewModel: SentenceQuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val result = state.result
    val soundPlayer = rememberSoundPlayer()

    LaunchedEffect(state.isAnswered) {
        if (state.isAnswered) {
            delay(30L)
            if (state.isCorrect == true) soundPlayer.playCorrect() else soundPlayer.playWrong()
        }
    }

    if (state.isFinished && result != null) {
        BlueScaffold(title = "文章問題") { inner ->
            SentenceResultContent(
                result = result,
                modifier = Modifier.padding(inner),
                onRetry = {
                    navController.navigate(Route.SentenceQuiz.path) {
                        popUpTo(Route.SentenceQuiz.path) { inclusive = true }
                    }
                },
                onHome = { navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } },
                onMenu = { navController.navigate(Route.SentenceMenu.path) { popUpTo(Route.SentenceMenu.path) { inclusive = true } } }
            )
        }
        return
    }

    BlueScaffold(title = "文章問題", onBack = { navController.popBackStack() }) { inner ->
        when {
            state.questions.isEmpty() && state.startedAt == 0L ->
                Box(Modifier.fillMaxSize().padding(inner).background(SoftBlue), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                        Text("問題を準備中...", color = TextMuted, fontSize = 14.sp)
                    }
                }
            state.questions.isEmpty() ->
                EmptyMessage(
                    modifier = Modifier.padding(inner).background(BrightBlue),
                    title = "出題できる文章がありません\n\n6語以上の英文、または\n[語句]形式で4つの語句を\n含む英文を登録してください",
                    button = "戻る",
                    onClick = { navController.popBackStack() }
                )
            else ->
                SentenceQuizContent(
                    modifier = Modifier.padding(inner),
                    state = state,
                    onSelectChoice = viewModel::selectWord,
                    onUndo = viewModel::undoLastWord,
                    onNext = viewModel::nextQuestion
                )
        }
    }
}
