package com.example.vocabapp

import com.example.vocabapp.ui.theme.SoftBlue

import com.example.vocabapp.ui.navigation.Route

import com.example.vocabapp.ui.screen.common.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.viewmodel.RandomCustomQuizViewModel


@Composable
internal fun RandomCustomMenuScreen(navController: NavHostController) {
    BlueScaffold(
        title = stringResource(R.string.random_custom_menu_title),
        onBack = { navController.popBackStack() }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionTitle(stringResource(R.string.random_custom_section)) }
            item {
                CardButton(
                    title = stringResource(R.string.random_word_title),
                    subtitle = stringResource(R.string.random_word_subtitle),
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = {
                        navController.navigate(Route.randomCustomQuiz(ContentType.WORD.routeValue))
                    }
                )
            }
            item {
                CardButton(
                    title = stringResource(R.string.random_idiom_title),
                    subtitle = stringResource(R.string.random_idiom_subtitle),
                    icon = Icons.Default.School,
                    onClick = {
                        navController.navigate(Route.randomCustomQuiz(ContentType.IDIOM.routeValue))
                    }
                )
            }
        }
    }
}

@Composable
internal fun RandomCustomQuizScreen(
    navController: NavHostController,
    viewModel: RandomCustomQuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QuizTimerLifecycleEffect(
        onStart = viewModel::onScreenStarted,
        onStop = viewModel::onScreenStopped
    )
    val isIdiom = ContentType.fromRouteValue(viewModel.contentType) == ContentType.IDIOM
    val title = if (isIdiom) {
        stringResource(R.string.random_idiom_title)
    } else {
        stringResource(R.string.random_word_title)
    }
    LaunchedEffect(state.finishedAttemptId) {
        state.finishedAttemptId?.let {
            navController.navigate(Route.result(it)) {
                popUpTo(Route.Home.path)
            }
        }
    }
    BlueScaffold(title = title, onBack = { navController.popBackStack() }) { inner ->
        when {
            state.startedAt == 0L -> Box(
                Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            state.questions.isEmpty() -> EmptyMessage(
                Modifier.padding(inner).background(SoftBlue),
                stringResource(
                    R.string.random_quiz_requires_four,
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
