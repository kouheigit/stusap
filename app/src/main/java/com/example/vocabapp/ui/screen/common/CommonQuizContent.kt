package com.example.vocabapp.ui.screen.common

import com.example.vocabapp.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.vocabapp.domain.model.QuizState
import kotlinx.coroutines.delay

@Composable
internal fun QuizContent(modifier: Modifier, state: QuizState, onAnswer: (Int?) -> Unit) {
    val question = state.currentQuestion ?: return
    val speaker = rememberSpeaker()
    val soundPlayer = rememberSoundPlayer()
    LaunchedEffect(question.word.id, speaker.isReady) {
        delay(150L)
        speaker.speak(question.word.english)
    }
    LaunchedEffect(state.isAnswered, state.currentIndex) {
        if (state.isAnswered) {
            delay(30L) // UIフィードバック表示直後に効果音を再生
            if (state.isCorrect == true) soundPlayer.playCorrect() else soundPlayer.playWrong()
        }
    }
    Box(modifier.fillMaxSize().background(SoftBlue)) {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("${state.currentIndex + 1} / ${state.questions.size}", color = TextDark, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1) / state.questions.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = AccentBlue,
                trackColor = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("残り ${state.remainingMillis / 1000}秒", color = TextMuted, modifier = Modifier.weight(1f))
                IconButton(onClick = { speaker.speak(question.word.english) }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Audio", tint = BrightBlue) }
            }
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(question.word.english, fontSize = 38.sp, fontWeight = FontWeight.Black, color = DeepBlue, textAlign = TextAlign.Center)
                    Text(question.word.phonetic, color = TextMuted, fontSize = 18.sp)
                }
            }
            question.choices.forEach { choice ->
                val correctId = question.choices.firstOrNull { it.isCorrect }?.id
                val color = when {
                    state.isAnswered && choice.id == correctId -> Success
                    state.isAnswered && choice.id == state.selectedChoiceId -> Danger
                    else -> Color.White
                }
                val textColor = if (color == Color.White) TextDark else Color.White
                Button(
                    onClick = { onAnswer(choice.id) },
                    enabled = !state.isAnswered,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = color, contentColor = textColor, disabledContentColor = textColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(choice.choiceText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            OutlinedButton(onClick = { onAnswer(null) }, enabled = !state.isAnswered, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Text("わからない", fontWeight = FontWeight.Bold)
            }
        }
        AnimatedVisibility(visible = state.isAnswered, modifier = Modifier.align(Alignment.Center)) {
            Box(
                modifier = Modifier.size(132.dp).clip(CircleShape).background(if (state.isCorrect == true) Success else Danger),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (state.isCorrect == true) Icons.Default.Check else Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(92.dp))
            }
        }
    }
}

@Composable
internal fun QuizTimerLifecycleEffect(
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> currentOnStart()
                Lifecycle.Event.ON_STOP -> currentOnStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            currentOnStop()
        }
    }
}

