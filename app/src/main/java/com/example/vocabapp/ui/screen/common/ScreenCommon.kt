package com.example.vocabapp

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.vocabapp.R
import com.example.vocabapp.domain.model.LessonStatus
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.Word
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


internal const val IMPORT_TAG = "ExcelImport"

@Composable
internal fun ResultContent(result: QuizResult, modifier: Modifier, onRetry: () -> Unit, onHome: () -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    val isPerfect = result.correctCount == result.totalQuestions
    val resId = medalResId(result.correctCount, result.totalQuestions)
    val title = medalTitle(result.correctCount, result.totalQuestions)
    val message = medalMessage(result.correctCount, result.totalQuestions)
    val soundPlayer = rememberSoundPlayer()

    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    var medalVisible by remember { mutableStateOf(false) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }
    val medalPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val perfectPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        val animDuration = 1800
        soundPlayer.playSequence(
            listOf(Pair(440f, 150), Pair(523f, 150), Pair(659f, 200), Pair(784f, 250), Pair(1047f, 350)),
            false
        )
        launch {
            animProgress.animateTo(
                targetValue = (result.accuracy / 100f).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = animDuration, easing = LinearEasing)
            )
        }
        val finalAcc = result.accuracy.toInt()
        val startTime = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed >= animDuration) { displayedAccuracy = finalAcc; break }
            displayedAccuracy = ((elapsed.toFloat() / animDuration) * finalAcc).toInt()
            delay(16L)
        }
        delay(200L)
        medalVisible = true
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val medalFocusReq = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build())
            .setOnAudioFocusChangeListener {}.build()
        try {
            am.requestAudioFocus(medalFocusReq)
            val mp = MediaPlayer.create(context, R.raw.new_medal_sound)
            mp?.setVolume(1f, 1f)
            mp?.setOnCompletionListener {
                it.release()
                medalPlayer.value = null
                am.abandonAudioFocusRequest(medalFocusReq)
            }
            mp?.start()
            medalPlayer.value = mp
        } catch (_: Exception) { am.abandonAudioFocusRequest(medalFocusReq) }
        if (isPerfect) {
            try {
                val mp = MediaPlayer.create(context, R.raw.perfect_native_male)
                mp?.setOnCompletionListener { it.release(); perfectPlayer.value = null }
                mp?.start()
                perfectPlayer.value = mp
            } catch (_: Exception) {}
        }
        launch { medalAlpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing)) }
        medalScale.animateTo(1.15f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        medalScale.animateTo(0.95f, animationSpec = tween(120))
        medalScale.animateTo(1f, animationSpec = tween(100))
    }

    DisposableEffect(Unit) {
        onDispose {
            medalPlayer.value?.let { mp ->
                if (mp.isPlaying) mp.stop()
                mp.release()
                medalPlayer.value = null
            }
            perfectPlayer.value?.let { mp ->
                if (mp.isPlaying) mp.stop()
                mp.release()
                perfectPlayer.value = null
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(BrightBlue)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ResultSectionCard(header = "正解率") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val medalSize = if (isPerfect) 110.dp else 160.dp
                        val resultMedalMod = when {
                            isPerfect -> Modifier.size(medalSize)
                            resId == R.drawable.medal_bronze -> Modifier.size(width = 107.dp, height = 160.dp)
                            else -> Modifier.size(medalSize)
                        }
                        if (medalVisible) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = if (isPerfect) "パーフェクトメダル" else null,
                                modifier = resultMedalMod.scale(medalScale.value).alpha(medalAlpha.value),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Spacer(resultMedalMod)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${result.correctCount}/${result.totalQuestions}正解", color = TextMuted, fontSize = 16.sp)
                            Text(
                                "$displayedAccuracy%",
                                color = if (isPerfect) Gold else DeepBlue,
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 58.sp
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { animProgress.value },
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp).height(14.dp).clip(RoundedCornerShape(7.dp)),
                        color = Teal,
                        trackColor = Color(0xFFDDE5EC)
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(title, color = DeepBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text(message, color = TextMuted, textAlign = TextAlign.Center, fontSize = 16.sp)
                    }
                }
            }

            item {
                ResultSectionCard(header = "学習状況") {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("正解 / 不正解", color = TextDark, fontWeight = FontWeight.Bold)
                            }
                            Text("${result.correctCount} / ${result.wrongCount}", color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                        androidx.compose.material3.HorizontalDivider(color = SoftBlue)
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("今回の学習時間", color = TextDark, fontWeight = FontWeight.Bold)
                            }
                            Text(formatStudyTime(result.studySeconds), color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            if (result.wrongWords.isNotEmpty()) {
                item {
                    Text("間違えた単語 (${result.wrongWords.size}語)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                }
                items(result.wrongWords) { word ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Danger.copy(alpha = 0.4f))
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Danger, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(word.english, color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Text(word.meaning, color = TextMuted, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f).height(54.dp)) {
                Text("再チャレンジ", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("次へ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun WordRow(word: Word, action: @Composable () -> Unit, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(word.english, color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("${word.meaning}  ${word.phonetic}", color = TextMuted)
                Text(word.partOfSpeech, color = TextMuted)
            }
            action()
        }
    }
}

@Composable
internal fun MasterBadge(isMaster: Boolean) {
    Box(
        modifier = Modifier.size(92.dp).clip(CircleShape).background(if (isMaster) Gold else Color(0xFFEAF1F7)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            Text(if (isMaster) "Master" else "Start", color = if (isMaster) Color.White else TextMuted, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
internal fun ResultSectionCard(header: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(SoftBlue).padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(header, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            content()
        }
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(text, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
}

@Composable
internal fun EmptyCard(text: String) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Text(text, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(28.dp))
    }
}

@Composable
internal fun EmptyMessage(modifier: Modifier, title: String, button: String, onClick: () -> Unit) {
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        EmptyCard(title)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onClick) { Text(button) }
    }
}

internal fun LessonStatus.label(): String = when (this) {
    LessonStatus.NotStarted -> "未学習"
    LessonStatus.InProgress -> "学習中"
    LessonStatus.Complete -> "完了"
    LessonStatus.Master -> "Master"
}

internal fun medalResId(correctCount: Int, totalQuestions: Int): Int {
    if (totalQuestions == 0) return R.drawable.medal_bronze
    val accuracy = correctCount * 100f / totalQuestions
    return when {
        correctCount == totalQuestions -> R.drawable.medal_perfect
        accuracy >= 80f -> R.drawable.medal_gold
        accuracy >= 50f -> R.drawable.medal_silver
        else -> R.drawable.medal_bronze
    }
}

internal fun lastMedalResId(lastAccuracy: Float): Int = when {
    lastAccuracy >= 100f -> R.drawable.medal_perfect
    lastAccuracy >= 80f -> R.drawable.medal_gold
    lastAccuracy >= 50f -> R.drawable.medal_silver
    else -> R.drawable.medal_bronze
}

internal fun medalTitle(correctCount: Int, totalQuestions: Int): String = when {
    correctCount == totalQuestions -> "Perfect！"
    correctCount >= 8 -> "Excellent！"
    correctCount >= 5 -> "Good！"
    else -> "Keep trying！"
}

internal fun medalMessage(correctCount: Int, totalQuestions: Int): String = when {
    correctCount == totalQuestions -> "頑張りましたね！おめでとうございます。\nトレーニングをマスターしました！"
    correctCount >= 8 -> "もう少しでパーフェクト！\nこの調子で頑張りましょう！"
    correctCount >= 5 -> "いい調子です！\n焦らず、コツコツ続けることが大事です！努力は嘘をつかないです！"
    else -> "諦めない事が肝心です！\nただひたすらに頑張れば、結果はおのずとついてきます！"
}

internal fun formatDate(millis: Long): String =
    SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN).format(Date(millis))

internal fun formatSeconds(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val hours = minutes / 60
    val remainMinutes = minutes % 60
    return if (hours > 0) "${hours}時間${remainMinutes}分" else "${minutes}分"
}

internal fun formatStudyTime(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    return if (safeSeconds < 60) "${safeSeconds}秒" else formatSeconds(safeSeconds)
}

internal val AddWordCardPadding = 24.dp
internal val AddWordCardSpacing = 20.dp

@Composable
internal fun AddWordField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: Int,
    inputType: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
    autoFocus: Boolean = false,
    onImeAction: () -> Unit = {},
    onReady: (EditText) -> Unit = {},
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnImeAction by rememberUpdatedState(onImeAction)
    val currentOnReady by rememberUpdatedState(onReady)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontWeight = FontWeight.Bold, color = TextMuted)
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            factory = { context ->
                EditText(context).apply {
                    setSingleLine(true)
                    hint = placeholder
                    textSize = 16f
                    setTextColor(TextDark.toArgb())
                    setHintTextColor(TextMuted.toArgb())
                    setPadding(32, 0, 32, 0)
                    this.inputType = inputType
                    imeOptions = imeAction
                    background = android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = 8.dp.value * resources.displayMetrics.density
                        setColor(android.graphics.Color.WHITE)
                        setStroke((1.dp.value * resources.displayMetrics.density).toInt(), Color(0xFFB0BEC5).toArgb())
                    }
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            currentOnValueChange(s?.toString().orEmpty())
                        }
                        override fun afterTextChanged(s: Editable?) = Unit
                    })
                    setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == imeAction) {
                            currentOnImeAction()
                            true
                        } else {
                            false
                        }
                    }
                    setOnFocusChangeListener { view, hasFocus ->
                        if (hasFocus) view.showKeyboard()
                    }
                    if (autoFocus) postDelayed({ focusAndShowKeyboard() }, 300)
                    currentOnReady(this)
                }
            },
            update = { editText ->
                if (editText.text.toString() != value) {
                    editText.setText(value)
                    editText.setSelection(value.length)
                }
                if (editText.imeOptions != imeAction) editText.imeOptions = imeAction
                if (editText.inputType != inputType) editText.inputType = inputType
                currentOnReady(editText)
            }
        )
    }
}

internal fun android.view.View.showKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

internal fun EditText.focusAndShowKeyboard() {
    requestFocus()
    setSelection(text?.length ?: 0)
    showKeyboard()
}
