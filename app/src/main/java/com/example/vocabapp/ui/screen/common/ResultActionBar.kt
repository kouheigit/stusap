package com.example.vocabapp.ui.screen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun ResultActionBar(onRetry: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GramSecondaryButton(
            text = "再チャレンジ",
            onClick = onRetry,
            modifier = Modifier.weight(1f)
        )
        GramPrimaryButton(
            text = "次へ",
            onClick = onNext,
            modifier = Modifier.weight(1f)
        )
    }
}
