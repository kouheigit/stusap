package com.example.vocabapp.ui.screen.passage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.vocabapp.data.local.entity.CustomPassageSummary
import com.example.vocabapp.domain.model.DocumentKind
import com.example.vocabapp.domain.model.PassageSet as DomainPassageSet
import com.example.vocabapp.ui.navigation.Route
import com.example.vocabapp.ui.screen.common.BlueScaffold
import com.example.vocabapp.ui.screen.common.EmptyCard
import com.example.vocabapp.ui.screen.common.GramCard
import com.example.vocabapp.ui.screen.common.GramPrimaryButton
import com.example.vocabapp.ui.theme.BrightBlue
import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.TextDark
import com.example.vocabapp.ui.theme.TextMuted
import com.example.vocabapp.viewmodel.CustomPassageListViewModel

@Composable
internal fun CustomPassageListScreen(
    navController: NavHostController,
    viewModel: CustomPassageListViewModel = hiltViewModel()
) {
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedSet = state.selectedSet
    if (selectedSet != null) {
        PassagePracticeScreen(
            navController = navController,
            sets = listOf(selectedSet.toPracticeSet())
        )
        return
    }

    BlueScaffold(title = "登録済み長文問題", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(SoftBlue),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GramPrimaryButton(
                    text = "長文問題を登録",
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate(Route.CustomPassageRegistration.path) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    containerColor = DeepBlue
                )
            }
            state.errorMessage?.let { message ->
                item {
                    MessageCard(message = message, color = Danger)
                }
            }
            if (summaries.isEmpty()) {
                item {
                    EmptyCard("登録済みの長文問題はありません")
                }
            } else {
                items(summaries.size) { index ->
                    PassageSummaryCard(
                        summary = summaries[index],
                        onClick = { viewModel.openSet(summaries[index].id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PassageSummaryCard(summary: CustomPassageSummary, onClick: () -> Unit) {
    GramCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = BrightBlue)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(summary.title, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text("${summary.documentKind.lowercase()} / ${summary.questionCount}問 / ${summary.timeLimitSec ?: 300}秒", color = TextMuted, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
private fun MessageCard(message: String, color: Color) {
    GramCard {
        Text(message, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))
    }
}

private fun DomainPassageSet.toPracticeSet(): PassageSet = PassageSet(
    id = id,
    instruction = instruction,
    documents = documents.map { document ->
        PassageDocument(
            kind = document.kind.toPracticeKind(),
            title = document.title,
            body = document.body
        )
    },
    questions = questions.map { question ->
        PassageQuestion(
            number = question.number,
            stem = question.stem,
            options = question.options,
            answerIndex = question.answerIndex,
            explanation = question.explanation
        )
    },
    timeLimitSec = timeLimitSec
)

private fun DocumentKind.toPracticeKind(): PassageDocumentKind = when (this) {
    DocumentKind.EMAIL -> PassageDocumentKind.Email
    DocumentKind.NOTICE -> PassageDocumentKind.Notice
    DocumentKind.ARTICLE -> PassageDocumentKind.Article
}
