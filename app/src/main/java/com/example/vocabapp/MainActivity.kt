package com.example.vocabapp

import com.example.vocabapp.ui.theme.VocabTheme

import com.example.vocabapp.ui.navigation.Route

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.ui.screen.quiz.SentenceImportScreen
import com.example.vocabapp.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent { VocabTheme { AppNav() } }
    }
}

@Composable
private fun AppNav(navController: NavHostController = rememberNavController()) {
    hiltViewModel<MainViewModel>()
    NavHost(navController = navController, startDestination = Route.Home.path) {
        composable(Route.Home.path) { HomeScreen(navController) }
        composable(Route.Lessons.path) { LessonListScreen(navController) }
        composable(Route.IdiomLessons.path) { IdiomLessonListScreen(navController) }
        composable(
            Route.Training.PATTERN,
            arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
        ) {
            TrainingListScreen(navController)
        }
        composable(
            Route.Quiz.PATTERN,
            arguments = listOf(
                navArgument("trainingId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("isReview") { type = NavType.BoolType; defaultValue = false }
            )
        ) { QuizScreen(navController) }
        composable(
            Route.Result.PATTERN,
            arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
        ) { ResultScreen(navController) }
        composable(Route.Review.path) { ReviewScreen(navController) }
        composable(
            Route.WordDetail.PATTERN,
            arguments = listOf(navArgument("wordId") { type = NavType.IntType })
        ) { WordDetailScreen(navController) }
        composable(Route.StudyLog.path) { StudyLogScreen(navController) }
        composable(Route.Settings.path) { SettingsScreen(navController) }
        composable(Route.AddWord.path) { AddWordScreen(navController) }
        composable(Route.WordImport.path) { WordImportScreen(navController) }
        composable(Route.CustomQuiz.path) {
            CustomTrainingRedirect(navController, ContentType.WORD)
        }
        composable(Route.CustomWordList.path) { CustomWordListScreen(navController) }
        composable(Route.AddIdiom.path) { AddIdiomScreen(navController) }
        composable(Route.CustomIdiomList.path) { CustomIdiomListScreen(navController) }
        composable(Route.CustomIdiomQuiz.path) {
            CustomTrainingRedirect(navController, ContentType.IDIOM)
        }
        composable(
            Route.CustomTraining.PATTERN,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { CustomTrainingListScreen(navController) }
        composable(
            Route.CustomTrainingBlock.PATTERN,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("blockNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            CustomTrainingBlockScreen(navController, checkNotNull(backStackEntry.arguments?.getInt("blockNumber")))
        }
        composable(
            Route.CustomTrainingQuiz.PATTERN,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("setNumber") { type = NavType.IntType }
            )
        ) { CustomTrainingQuizScreen(navController) }
        composable(Route.RandomCustomMenu.path) { RandomCustomMenuScreen(navController) }
        composable(
            Route.RandomCustomQuiz.PATTERN,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { RandomCustomQuizScreen(navController) }
        composable(
            Route.Flashcard.PATTERN,
            arguments = listOf(navArgument("trainingId") { type = NavType.IntType })
        ) { FlashcardScreen(navController) }
        composable(Route.SentenceMenu.path) { SentenceMenuScreen(navController) }
        composable(Route.AddSentence.path) { AddSentenceScreen(navController) }
        composable(Route.SentenceImport.path) { SentenceImportScreen(navController) }
        composable(Route.CustomSentenceList.path) { CustomSentenceListScreen(navController) }
        composable(
            Route.SentenceTrainingBlock.PATTERN,
            arguments = listOf(navArgument("blockNumber") { type = NavType.IntType })
        ) { backStackEntry ->
            SentenceTrainingBlockScreen(
                navController = navController,
                blockNumber = backStackEntry.arguments?.getInt("blockNumber") ?: 1
            )
        }
        composable(
            Route.SentenceQuiz.PATTERN,
            arguments = listOf(navArgument("setNumber") { type = NavType.IntType })
        ) { SentenceQuizScreen(navController) }
    }
}

@Composable
private fun CustomTrainingRedirect(navController: NavHostController, contentType: ContentType) {
    LaunchedEffect(contentType) {
        navController.navigate(Route.customTraining(contentType.routeValue)) {
            val sourceRoute = if (contentType == ContentType.IDIOM) {
                Route.CustomIdiomQuiz.path
            } else {
                Route.CustomQuiz.path
            }
            popUpTo(sourceRoute) { inclusive = true }
        }
    }
}
