package com.example.vocabapp

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import com.example.vocabapp.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vocabapp.domain.model.Lesson
import com.example.vocabapp.domain.model.LessonStatus
import com.example.vocabapp.domain.model.QuizResult
import com.example.vocabapp.domain.model.QuizState
import com.example.vocabapp.domain.model.Training
import com.example.vocabapp.domain.model.SentenceQuizResult
import com.example.vocabapp.domain.model.SentenceQuizState
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.viewmodel.AddIdiomViewModel
import com.example.vocabapp.viewmodel.AddSentenceViewModel
import com.example.vocabapp.viewmodel.AddWordViewModel
import com.example.vocabapp.viewmodel.CustomSentenceListViewModel
import com.example.vocabapp.viewmodel.CustomIdiomListViewModel
import com.example.vocabapp.viewmodel.CustomIdiomQuizViewModel
import com.example.vocabapp.viewmodel.CustomTrainingListViewModel
import com.example.vocabapp.viewmodel.CustomTrainingQuizViewModel
import com.example.vocabapp.viewmodel.CustomWordListViewModel
import com.example.vocabapp.viewmodel.CustomWordQuizViewModel
import com.example.vocabapp.viewmodel.FlashcardViewModel
import com.example.vocabapp.viewmodel.IdiomLessonListViewModel
import com.example.vocabapp.viewmodel.LessonListViewModel
import com.example.vocabapp.viewmodel.MainViewModel
import com.example.vocabapp.viewmodel.QuizViewModel
import com.example.vocabapp.viewmodel.RandomCustomQuizViewModel
import com.example.vocabapp.viewmodel.ResultViewModel
import com.example.vocabapp.viewmodel.ReviewViewModel
import com.example.vocabapp.viewmodel.SentenceQuizViewModel
import com.example.vocabapp.viewmodel.StudyLogViewModel
import com.example.vocabapp.viewmodel.TrainingListViewModel
import com.example.vocabapp.viewmodel.WordDetailViewModel
import com.example.vocabapp.viewmodel.WordImportViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import android.util.Log
import java.util.zip.ZipInputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory


internal const val IMPORT_TAG = "ExcelImport"

internal val activeSynthTrack = java.util.concurrent.atomic.AtomicReference<AudioTrack?>(null)


internal fun playSynthBuffer(buffer: ShortArray) {
    Thread {
        // 前の効果音を停止してから新しい音を再生
        activeSynthTrack.getAndSet(null)?.runCatching { stop(); flush(); release() }
        try {
            val sampleRate = 44100
            val trackBuf = buffer.size * 2 // MODE_STATICはデータサイズ分のバッファで十分
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(trackBuf)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(buffer, 0, buffer.size)
            track.setVolume(AudioTrack.getMaxVolume()) // 必ず最大音量で再生
            activeSynthTrack.set(track)
            track.play()
            Thread.sleep(buffer.size * 1000L / sampleRate + 80)
            activeSynthTrack.compareAndSet(track, null)
            track.stop()
            track.release()
        } catch (_: Exception) {}
    }.start()
}

internal fun playSynthSound(segments: List<Pair<Float, Int>>, interrupt: Boolean = true) {
    val buffer = buildSynthBuffer(segments)
    if (interrupt) {
        playSynthBuffer(buffer)
    } else {
        Thread {
            try {
                Thread.sleep(40)
                playSynthBuffer(buffer)
            } catch (_: Exception) {}
        }.start()
    }
}

internal data class SoundPlayer(
    val playCorrect: () -> Unit,
    val playWrong: () -> Unit
)

// 起動時に一度だけ音声バッファを計算してキャッシュする（解答時の遅延を排除）
internal val correctSoundBuffer: ShortArray by lazy { buildSynthBuffer(listOf(698f to 140, 880f to 260)) }
internal val wrongSoundBuffer: ShortArray by lazy { buildSynthBuffer(listOf(280f to 190, 0f to 45, 220f to 230)) }

internal fun buildSynthBuffer(segments: List<Pair<Float, Int>>): ShortArray {
    val sampleRate = 44100
    val totalSamples = segments.sumOf { (_, ms) -> sampleRate * ms / 1000 }
    val buffer = ShortArray(totalSamples)
    var pos = 0
    for ((freq, durationMs) in segments) {
        val numSamples = sampleRate * durationMs / 1000
        for (i in 0 until numSamples) {
            if (freq == 0f) { buffer[pos++] = 0; continue }
            val envelope = when {
                i < numSamples * 0.10 -> i / (numSamples * 0.10)
                i > numSamples * 0.65 -> (numSamples - i).toDouble() / (numSamples * 0.35)
                else -> 1.0
            }.coerceIn(0.0, 1.0)
            val wave = kotlin.math.sin(2 * Math.PI * freq * i / sampleRate)
            val sample = (wave * Short.MAX_VALUE * 0.95 * envelope).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[pos++] = sample.toShort()
        }
    }
    return buffer
}

@Composable
internal fun rememberSoundPlayer(): SoundPlayer = remember {
    SoundPlayer(
        playCorrect = {
            // ピンポン: E5(659Hz) → A5(880Hz) の二音上昇チャイム（バッファ事前計算済み）
            playSynthBuffer(correctSoundBuffer)
        },
        playWrong = {
            // ブッブー: 低音バズ×2（バッファ事前計算済み）
            playSynthBuffer(wrongSoundBuffer)
        }
    )
}

internal data class Speaker(
    val isReady: Boolean,
    val speak: (String) -> Unit
)


@Composable
internal fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isReady by remember { mutableStateOf(false) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val pendingSpeechText = remember { java.util.concurrent.atomic.AtomicReference<String?>(null) }
    val isTtsConfigured = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
    val focusRequest = remember {
        android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setOnAudioFocusChangeListener {}
            .build()
    }
    // 直前に発話した内容と時刻を記録して、短時間の重複リクエストを防ぐ
    val lastSpokenText = remember { java.util.concurrent.atomic.AtomicReference<String>("") }
    val lastSpokenAt = remember { java.util.concurrent.atomic.AtomicLong(0L) }
    fun speakNow(text: String, engine: TextToSpeech) {
        val now = System.currentTimeMillis()
        val isSameTextRecently = lastSpokenText.get() == text && now - lastSpokenAt.get() < 400L
        if (isSameTextRecently) return
        lastSpokenText.set(text)
        lastSpokenAt.set(now)
        engine.stop()
        audioManager.requestAudioFocus(focusRequest)
        val utteranceId = "utt-${System.nanoTime()}"
        val params = android.os.Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun configureTts(engine: TextToSpeech) {
        if (!isTtsConfigured.compareAndSet(false, true)) return

        val langResult = engine.setLanguage(Locale.US)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.language = Locale.ENGLISH
        }
        engine.setSpeechRate(0.9f)
        isReady = true
        pendingSpeechText.getAndSet(null)?.let { text ->
            speakNow(text, engine)
        }
    }

    DisposableEffect(context) {
        var ttsRef: TextToSpeech? = null
        val instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                mainHandler.post {
                    ttsRef?.let(::configureTts)
                }
            }
        }
        ttsRef = instance
        instance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                mainHandler.post { audioManager.abandonAudioFocusRequest(focusRequest) }
            }

            @Deprecated("Deprecated by Android SDK")
            override fun onError(utteranceId: String?) = Unit

            override fun onError(utteranceId: String?, errorCode: Int) = Unit
        })
        tts = instance
        onDispose {
            isReady = false
            isTtsConfigured.set(false)
            pendingSpeechText.set(null)
            instance.stop()
            instance.shutdown()
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }
    val speak: (String) -> Unit = speak@ { text ->
        if (text.isBlank()) return@speak
        val engine = tts
        if (engine != null && isReady) {
            speakNow(text, engine)
        } else {
            pendingSpeechText.set(text)
        }
    }
    return Speaker(isReady = isReady, speak = speak)
}


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

@Composable
internal fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(32.dp))
                    Text("${lesson.wordStartNumber}〜${lesson.wordEndNumber}語", color = DeepBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
                Text("学習状態  ${lesson.status.label()}", color = TextMuted, fontSize = 16.sp)
                Text("学習日  ${lesson.lastStudiedAt?.let(::formatDate) ?: "-"}", color = TextMuted, fontSize = 16.sp)
                LinearProgressIndicator(progress = { lesson.progressRate }, modifier = Modifier.fillMaxWidth(0.72f).height(8.dp).clip(RoundedCornerShape(4.dp)), color = Teal, trackColor = SoftBlue)
            }
            MasterBadge(isMaster = lesson.status == LessonStatus.Master)
        }
    }
}

@Composable
internal fun TrainingCard(training: Training, onQuiz: () -> Unit, onDetail: (Int) -> Unit, onFlashcard: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onQuiz),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(training.title, color = TextMuted, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(32.dp))
                    Text("${training.wordStartNumber}〜${training.wordEndNumber}語", color = DeepBlue, fontSize = 28.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                }
                Text("学習回数  ${training.studyCount}", color = TextMuted)
                Text("学習日  ${training.lastStudiedAt?.let(::formatDate) ?: "-"}", color = TextMuted)
                if (training.studyCount > 0) {
                    Text("ベスト  ${training.bestAccuracy.toInt()}%", color = TextMuted)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(3) { index ->
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFDDE5EC), modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("未挑戦", color = TextMuted)
                    }
                }
            }
            if (training.studyCount > 0) {
                val effectiveAccuracy = if (training.lastAccuracy > 0f) training.lastAccuracy else training.bestAccuracy
                val currentMedalResId = lastMedalResId(effectiveAccuracy)
                val isBronze = currentMedalResId == R.drawable.medal_bronze
                val medalMod = if (isBronze)
                    Modifier.size(width = 112.dp, height = 150.dp).clickable { onQuiz() }
                else
                    Modifier.size(112.dp).clickable { onQuiz() }
                Image(
                    painter = painterResource(currentMedalResId),
                    contentDescription = null,
                    modifier = medalMod,
                    contentScale = ContentScale.Fit
                )
            } else {
                Button(onClick = onQuiz, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue), shape = CircleShape, modifier = Modifier.size(86.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text("開始", fontSize = 12.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Text(
                "単語帳で学習",
                modifier = Modifier.weight(1f).clickable { onFlashcard() }.padding(bottom = 14.dp),
                textAlign = TextAlign.Center,
                color = AccentBlue,
                fontWeight = FontWeight.Bold
            )
            Text(
                "先頭単語の詳細を見る",
                modifier = Modifier.weight(1f).clickable { onDetail(training.firstWordId) }.padding(bottom = 14.dp),
                textAlign = TextAlign.Center,
                color = BrightBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

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

@Composable
internal fun ResultContent(result: QuizResult, modifier: Modifier, onRetry: () -> Unit, onHome: () -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    val isPerfect = result.correctCount == result.totalQuestions
    val resId = medalResId(result.correctCount, result.totalQuestions)
    val title = medalTitle(result.correctCount, result.totalQuestions)
    val message = medalMessage(result.correctCount, result.totalQuestions)

    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    var medalVisible by remember { mutableStateOf(false) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }
    val perfectPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        val animDuration = 1800
        playSynthSound(
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
            mp?.setOnCompletionListener { it.release(); am.abandonAudioFocusRequest(medalFocusReq) }
            mp?.start()
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
