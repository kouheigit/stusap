package com.example.vocabapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.vocabapp.ui.screen.common.BlueScaffold
import com.example.vocabapp.ui.screen.common.CardButton
import com.example.vocabapp.ui.screen.common.AnimatedMascot
import com.example.vocabapp.ui.screen.common.MascotMood
import com.example.vocabapp.ui.theme.SoftBlue
import com.example.vocabapp.ui.theme.TextDark

@Composable
internal fun BulkImportScreen(navController: NavHostController) {
    BlueScaffold(
        title = stringResource(R.string.bulk_import_screen_title),
        onBack = { navController.popBackStack() }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(SoftBlue),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AnimatedMascot(
                    mood = MascotMood.Wave,
                    message = "取り込みたい形式を選びましょう"
                )
            }
            item {
                Text(
                    stringResource(R.string.bulk_import_screen_subtitle),
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
            }
            bulkImportOptions().forEach { option ->
                item {
                    CardButton(
                        title = stringResource(option.titleResId),
                        subtitle = stringResource(option.subtitleResId),
                        icon = option.icon,
                        onClick = { navController.navigate(option.route) }
                    )
                }
            }
        }
    }
}

private val BulkImportOption.titleResId: Int
    get() = when (kind) {
        BulkImportKind.WordIdiom -> R.string.bulk_import_word_title
        BulkImportKind.Sentence -> R.string.bulk_import_sentence_title
    }

private val BulkImportOption.subtitleResId: Int
    get() = when (kind) {
        BulkImportKind.WordIdiom -> R.string.bulk_import_word_subtitle
        BulkImportKind.Sentence -> R.string.bulk_import_sentence_subtitle
    }

private val BulkImportOption.icon: ImageVector
    get() = when (kind) {
        BulkImportKind.WordIdiom -> Icons.AutoMirrored.Filled.FormatListBulleted
        BulkImportKind.Sentence -> Icons.Default.School
    }
