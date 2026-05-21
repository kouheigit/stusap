package com.example.vocabapp.ui.screen.common

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.theme.DeepBlue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BlueScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                    } else {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.padding(start = 16.dp))
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue)
            )
        },
        content = content
    )
}

@Composable
internal fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0x44FFFFFF))) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
internal fun CardButton(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = BrightBlue, modifier = Modifier.size(42.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = DeepBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = TextMuted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
internal fun BottomAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(54.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBlue)) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.Bold)
    }
}

