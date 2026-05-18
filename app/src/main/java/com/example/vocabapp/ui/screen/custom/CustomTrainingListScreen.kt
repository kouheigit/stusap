package com.example.vocabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.viewmodel.CustomTrainingListViewModel


@Composable
internal fun CustomTrainingListScreen(navController: NavHostController, viewModel: CustomTrainingListViewModel = hiltViewModel()) {
    val trainings by viewModel.trainings.collectAsStateWithLifecycle()
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
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { navController.navigate(if (isIdiom) Route.AddIdiom.path else Route.AddWord.path) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isIdiom) {
                                stringResource(R.string.custom_idiom_register)
                            } else {
                                stringResource(R.string.custom_word_register)
                            },
                            color = DeepBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { navController.navigate(listRoute) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.custom_registered_list),
                            color = DeepBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            item { SectionTitle(stringResource(R.string.custom_block_100)) }
            if (trainings.isEmpty()) {
                item { EmptyCard(stringResource(R.string.custom_empty_questions)) }
            } else {
                val blocks = trainings.chunked(10)
                items(blocks) { block ->
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
    val isIdiom = ContentType.fromRouteValue(viewModel.contentType) == ContentType.IDIOM
    val titlePrefix = if (isIdiom) {
        stringResource(R.string.custom_idiom_title)
    } else {
        stringResource(R.string.custom_word_title)
    }
    val listRoute = if (isIdiom) Route.CustomIdiomList.path else Route.CustomWordList.path
    val startQuestion = (blockNumber - 1).coerceAtLeast(0) * 100 + 1
    val endQuestion = blockNumber * 100
    val blockTrainings = trainings.drop((blockNumber - 1).coerceAtLeast(0) * 10).take(10)
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
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionTitle(stringResource(R.string.custom_block_10)) }
            if (blockTrainings.isEmpty()) {
                item { EmptyCard(stringResource(R.string.custom_empty_range)) }
            } else {
                items(blockTrainings) { training ->
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
    ((training.wordStartNumber - 1) / 10) + 1
