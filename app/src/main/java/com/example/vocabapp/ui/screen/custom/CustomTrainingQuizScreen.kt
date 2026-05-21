package com.example.vocabapp

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.viewmodel.CustomIdiomQuizViewModel
import com.example.vocabapp.viewmodel.CustomTrainingQuizViewModel
import com.example.vocabapp.viewmodel.CustomWordQuizViewModel


@Composable
internal fun CustomIdiomQuizScreen(
    navController: NavHostController,
    viewModel: CustomIdiomQuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QuizTimerLifecycleEffect(
        onStart = viewModel::onScreenStarted,
        onStop = viewModel::onScreenStopped
    )
    if (state.finishedAttemptId != null) {
        val total = state.questions.size.coerceAtLeast(1)
        BlueScaffold(title = stringResource(R.string.custom_idiom_quiz)) { inner ->
            CustomWordQuizResultContent(
                correctCount = state.correctCount,
                total = total,
                onRetry = {
                    navController.navigate(Route.CustomIdiomQuiz.path) {
                        popUpTo(Route.CustomIdiomQuiz.path) { inclusive = true }
                    }
                },
                onHome = { navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } },
                modifier = Modifier.fillMaxSize().padding(inner)
            )
        }
    } else {
        BlueScaffold(
            title = stringResource(R.string.custom_idiom_quiz),
            onBack = { navController.popBackStack() }
        ) { inner ->
            when {
                state.startedAt == 0L -> LoadingBox(Modifier.padding(inner))
                state.questions.isEmpty() -> EmptyMessage(
                    Modifier.padding(inner).background(BrightBlue),
                    stringResource(
                        R.string.custom_quiz_requires_four,
                        stringResource(R.string.custom_idiom_label)
                    ),
                    stringResource(R.string.nav_back)
                ) { navController.popBackStack() }
                else -> QuizContent(Modifier.padding(inner), state, viewModel::submit)
            }
        }
    }
}

@Composable
internal fun CustomWordQuizScreen(
    navController: NavHostController,
    viewModel: CustomWordQuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QuizTimerLifecycleEffect(
        onStart = viewModel::onScreenStarted,
        onStop = viewModel::onScreenStopped
    )
    if (state.finishedAttemptId != null) {
        val total = state.questions.size.coerceAtLeast(1)
        BlueScaffold(title = stringResource(R.string.custom_word_quiz)) { inner ->
            CustomWordQuizResultContent(
                correctCount = state.correctCount,
                total = total,
                onRetry = {
                    navController.navigate(Route.CustomQuiz.path) {
                        popUpTo(Route.CustomQuiz.path) { inclusive = true }
                    }
                },
                onHome = { navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } },
                modifier = Modifier.fillMaxSize().padding(inner)
            )
        }
    } else {
        BlueScaffold(
            title = stringResource(R.string.custom_word_quiz),
            onBack = { navController.popBackStack() }
        ) { inner ->
            when {
                state.startedAt == 0L -> LoadingBox(Modifier.padding(inner))
                state.questions.isEmpty() -> EmptyMessage(
                    Modifier.padding(inner).background(BrightBlue),
                    stringResource(
                        R.string.custom_quiz_requires_four,
                        stringResource(R.string.custom_word_label)
                    ),
                    stringResource(R.string.nav_back)
                ) { navController.popBackStack() }
                else -> QuizContent(Modifier.padding(inner), state, viewModel::submit)
            }
        }
    }
}


@Composable
internal fun CustomTrainingQuizScreen(
    navController: NavHostController,
    viewModel: CustomTrainingQuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QuizTimerLifecycleEffect(
        onStart = viewModel::onScreenStarted,
        onStop = viewModel::onScreenStopped
    )
    val isIdiom = ContentType.fromRouteValue(viewModel.contentType) == ContentType.IDIOM
    val startQuestion = (viewModel.setNumber - 1).coerceAtLeast(0) * 10 + 1
    val endQuestion = viewModel.setNumber * 10
    val titlePrefix = if (isIdiom) {
        stringResource(R.string.custom_idiom_title)
    } else {
        stringResource(R.string.custom_word_title)
    }
    val title = stringResource(R.string.custom_training_title, titlePrefix, startQuestion, endQuestion)
    LaunchedEffect(state.finishedAttemptId) {
        state.finishedAttemptId?.let {
            navController.navigate(Route.result(it)) {
                popUpTo(Route.Home.path)
            }
        }
    }
    BlueScaffold(title = title, onBack = { navController.popBackStack() }) { inner ->
        when {
            state.startedAt == 0L -> LoadingBox(Modifier.padding(inner))
            state.questions.isEmpty() -> EmptyMessage(
                Modifier.padding(inner).background(BrightBlue),
                stringResource(
                    R.string.custom_quiz_requires_four,
                    if (isIdiom) {
                        stringResource(R.string.custom_idiom_label)
                    } else {
                        stringResource(R.string.custom_word_label)
                    }
                ),
                stringResource(R.string.nav_back)
            ) { navController.popBackStack() }
            else -> QuizContent(Modifier.padding(inner), state, viewModel::submit)
        }
    }
}

@Composable
private fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
