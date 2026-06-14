package com.example.vocabapp

import com.example.vocabapp.ui.theme.SoftBlue

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.QuizConstants
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.viewmodel.CustomTrainingListViewModel


@Composable
internal fun CustomTrainingListScreen(navController: NavHostController, viewModel: CustomTrainingListViewModel = hiltViewModel()) {
    val trainings by viewModel.trainings.collectAsStateWithLifecycle()
    val orderedTrainings = trainings.sortedBy { it.wordStartNumber }
    val isIdiom = ContentType.fromRouteValue(viewModel.contentType) == ContentType.IDIOM
    val title = if (isIdiom) {
        stringResource(R.string.custom_idiom_title)
    } else {
        stringResource(R.string.custom_word_title)
    }
    val listRoute = if (isIdiom) Route.CustomIdiomList.path else Route.CustomWordList.path
    val blockLabel = if (isIdiom) {
        stringResource(R.string.custom_idiom_label)
    } else {
        stringResource(R.string.custom_word_label)
    }
    BlueScaffold(title = title, onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    GramPrimaryButton(
                        text = if (isIdiom) {
                            stringResource(R.string.custom_idiom_register)
                        } else {
                            stringResource(R.string.custom_word_register)
                        },
                        icon = Icons.Default.Add,
                        onClick = { navController.navigate(if (isIdiom) Route.AddIdiom.path else Route.AddWord.path) },
                        modifier = Modifier.weight(1f).height(56.dp)
                    )
                    GramSecondaryButton(
                        text = stringResource(R.string.custom_registered_list),
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        onClick = { navController.navigate(listRoute) },
                        modifier = Modifier.weight(1f).height(56.dp)
                    )
                }
                if (isIdiom) {
                    Spacer(Modifier.height(8.dp))
                    GramSecondaryButton(
                        text = stringResource(R.string.custom_idiom_import),
                        icon = Icons.Default.FileUpload,
                        onClick = { navController.navigate(Route.IdiomImport.path) },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    )
                }
            }
            item { SectionTitle(stringResource(R.string.custom_block_100)) }
            if (orderedTrainings.isEmpty()) {
                item { EmptyCard(stringResource(R.string.custom_empty_questions)) }
            } else {
                val blocks = orderedTrainings.chunked(10)
                items(blocks, key = { block -> block.first().wordStartNumber }) { block ->
                    val first = block.first()
                    val last = block.last()
                    val blockNumber = ((first.wordStartNumber - 1) / 100) + 1
                    CardButton(
                        title = stringResource(
                            R.string.custom_question_range,
                            first.wordStartNumber,
                            last.wordEndNumber
                        ),
                        subtitle = stringResource(R.string.custom_set_count, blockLabel, block.size),
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        onClick = {
                            navController.navigate(
                                Route.customTrainingBlock(viewModel.contentType, blockNumber)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun CustomTrainingBlockScreen(
    navController: NavHostController,
    blockNumber: Int,
    viewModel: CustomTrainingListViewModel = hiltViewModel()
) {
    val trainings by viewModel.trainings.collectAsStateWithLifecycle()
    val orderedTrainings = trainings.sortedBy { it.wordStartNumber }
    val isIdiom = ContentType.fromRouteValue(viewModel.contentType) == ContentType.IDIOM
    val titlePrefix = if (isIdiom) {
        stringResource(R.string.custom_idiom_title)
    } else {
        stringResource(R.string.custom_word_title)
    }
    val listRoute = if (isIdiom) Route.CustomIdiomList.path else Route.CustomWordList.path
    val startQuestion = (blockNumber - 1).coerceAtLeast(0) * 100 + 1
    val endQuestion = blockNumber * 100
    val blockTrainings = orderedTrainings.drop((blockNumber - 1).coerceAtLeast(0) * 10).take(10)
    val titleEndQuestion = blockTrainings.lastOrNull()?.wordEndNumber ?: endQuestion
    BlueScaffold(
        title = stringResource(
            R.string.custom_training_title,
            titlePrefix,
            startQuestion,
            titleEndQuestion
        ),
        onBack = { navController.popBackStack() }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionTitle(stringResource(R.string.custom_block_10)) }
            if (blockTrainings.isEmpty()) {
                item { EmptyCard(stringResource(R.string.custom_empty_range)) }
            } else {
                items(blockTrainings, key = { training -> training.wordStartNumber }) { training ->
                    val setNumber = customSetNumber(training)
                    TrainingCard(
                        training = training,
                        onQuiz = {
                            navController.navigate(
                                Route.customTrainingQuiz(viewModel.contentType, setNumber)
                            )
                        },
                        onDetail = { navController.navigate(listRoute) },
                        onFlashcard = { navController.navigate(listRoute) }
                    )
                }
            }
        }
    }
}

internal fun buildSectionPreview(items: List<String>, limit: Int = 2): String {
    val head = items.take(limit).joinToString(", ") { it.trim() }
    return if (items.size > limit) "$head, ...(省略)" else head
}

internal fun customSetNumber(training: Training): Int =
    ((training.wordStartNumber - 1).coerceAtLeast(0) / QuizConstants.QUESTION_COUNT) + 1
