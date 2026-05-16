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
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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

private const val IMPORT_TAG = "ExcelImport"

private val activeSynthTrack = java.util.concurrent.atomic.AtomicReference<AudioTrack?>(null)


private fun playSynthBuffer(buffer: ShortArray) {
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

private fun playSynthSound(segments: List<Pair<Float, Int>>, interrupt: Boolean = true) {
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

private data class SoundPlayer(
    val playCorrect: () -> Unit,
    val playWrong: () -> Unit
)

// 起動時に一度だけ音声バッファを計算してキャッシュする（解答時の遅延を排除）
private val correctSoundBuffer: ShortArray by lazy { buildSynthBuffer(listOf(698f to 140, 880f to 260)) }
private val wrongSoundBuffer: ShortArray by lazy { buildSynthBuffer(listOf(280f to 190, 0f to 45, 220f to 230)) }

private fun buildSynthBuffer(segments: List<Pair<Float, Int>>): ShortArray {
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
            val sample = (wave * Short.MAX_VALUE * 0.75 * envelope).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[pos++] = sample.toShort()
        }
    }
    return buffer
}

@Composable
private fun rememberSoundPlayer(): SoundPlayer = remember {
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

private data class Speaker(
    val isReady: Boolean,
    val speak: (String) -> Unit
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent { VocabTheme { AppNav() } }
    }
}

private object Route {
    const val Home = "home"
    const val Lessons = "lessons"
    const val IdiomLessons = "idiom-lessons"
    const val Training = "training/{lessonId}"
    const val Quiz = "quiz?trainingId={trainingId}&isReview={isReview}"
    const val Result = "result/{attemptId}"
    const val Review = "review"
    const val WordDetail = "word/{wordId}"
    const val StudyLog = "study-log"
    const val Settings = "settings"
    const val AddWord = "add-word"
    const val WordImport = "word-import"
    const val CustomQuiz = "custom-quiz"
    const val CustomWordList = "custom-word-list"
    const val AddIdiom = "add-idiom"
    const val CustomIdiomList = "custom-idiom-list"
    const val CustomIdiomQuiz = "custom-idiom-quiz"
    const val CustomTraining = "custom-training/{type}"
    const val CustomTrainingBlock = "custom-training/{type}/block/{blockNumber}"
    const val CustomTrainingQuiz = "custom-training-quiz/{type}/{setNumber}"
    const val RandomCustomMenu = "random-custom-menu"
    const val RandomCustomQuiz = "random-custom-quiz/{type}"
    const val Flashcard = "flashcard/{trainingId}"
    const val SentenceMenu = "sentence-menu"
    const val AddSentence = "add-sentence"
    const val CustomSentenceList = "custom-sentence-list"
    const val SentenceQuiz = "sentence-quiz"

    fun flashcard(trainingId: Int) = "flashcard/$trainingId"
    fun customTraining(type: String) = "custom-training/$type"
    fun customTrainingBlock(type: String, blockNumber: Int) = "custom-training/$type/block/$blockNumber"
    fun customTrainingQuiz(type: String, setNumber: Int) = "custom-training-quiz/$type/$setNumber"
    fun randomCustomQuiz(type: String) = "random-custom-quiz/$type"

    fun training(lessonId: Int) = "training/$lessonId"
    fun quiz(trainingId: Int? = null, isReview: Boolean = false) =
        "quiz?trainingId=${trainingId ?: 0}&isReview=$isReview"
    fun result(attemptId: Long) = "result/$attemptId"
    fun word(wordId: Int) = "word/$wordId"
}

private val DeepBlue = Color(0xFF0D47A1)
private val BrightBlue = Color(0xFF1E88E5)
private val AccentBlue = Color(0xFF03A9E6)
private val SoftBlue = Color(0xFFE3F2FD)
private val TextDark = Color(0xFF17203C)
private val TextMuted = Color(0xFF7B8A95)
private val Success = Color(0xFF22A852)
private val Danger = Color(0xFFE5395A)
private val Gold = Color(0xFFFFC943)
private val Teal = Color(0xFF41C7BE)

@Composable
private fun VocabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = BrightBlue,
            secondary = Teal,
            background = Color.White,
            surface = Color.White,
            error = Danger
        ),
        content = content
    )
}

@Composable
private fun AppNav(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Route.Home) {
        composable(Route.Home) { HomeScreen(navController) }
        composable(Route.Lessons) { LessonListScreen(navController) }
        composable(Route.IdiomLessons) { IdiomLessonListScreen(navController) }
        composable(
            Route.Training,
            arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
        ) { TrainingListScreen(navController) }
        composable(
            Route.Quiz,
            arguments = listOf(
                navArgument("trainingId") { type = NavType.IntType; defaultValue = 0 },
                navArgument("isReview") { type = NavType.BoolType; defaultValue = false }
            )
        ) { QuizScreen(navController) }
        composable(
            Route.Result,
            arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
        ) { ResultScreen(navController) }
        composable(Route.Review) { ReviewScreen(navController) }
        composable(
            Route.WordDetail,
            arguments = listOf(navArgument("wordId") { type = NavType.IntType })
        ) { WordDetailScreen(navController) }
        composable(Route.StudyLog) { StudyLogScreen(navController) }
        composable(Route.Settings) { SettingsScreen(navController) }
        composable(Route.AddWord) { AddWordScreen(navController) }
        composable(Route.WordImport) { WordImportScreen(navController) }
        composable(Route.CustomQuiz) { CustomTrainingRedirect(navController, "word") }
        composable(Route.CustomWordList) { CustomWordListScreen(navController) }
        composable(Route.AddIdiom) { AddIdiomScreen(navController) }
        composable(Route.CustomIdiomList) { CustomIdiomListScreen(navController) }
        composable(Route.CustomIdiomQuiz) { CustomTrainingRedirect(navController, "idiom") }
        composable(
            Route.CustomTraining,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { CustomTrainingListScreen(navController) }
        composable(
            Route.CustomTrainingBlock,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("blockNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            CustomTrainingBlockScreen(
                navController = navController,
                blockNumber = checkNotNull(backStackEntry.arguments?.getInt("blockNumber"))
            )
        }
        composable(
            Route.CustomTrainingQuiz,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("setNumber") { type = NavType.IntType }
            )
        ) { CustomTrainingQuizScreen(navController) }
        composable(Route.RandomCustomMenu) { RandomCustomMenuScreen(navController) }
        composable(
            Route.RandomCustomQuiz,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { RandomCustomQuizScreen(navController) }
        composable(
            Route.Flashcard,
            arguments = listOf(navArgument("trainingId") { type = NavType.IntType })
        ) { FlashcardScreen(navController) }
        composable(Route.SentenceMenu) { SentenceMenuScreen(navController) }
        composable(Route.AddSentence) { AddSentenceScreen(navController) }
        composable(Route.CustomSentenceList) { CustomSentenceListScreen(navController) }
        composable(Route.SentenceQuiz) { SentenceQuizScreen(navController) }
    }
}

@Composable
private fun CustomTrainingRedirect(navController: NavHostController, type: String) {
    LaunchedEffect(type) {
        navController.navigate(Route.customTraining(type)) {
            popUpTo(if (type == "idiom") Route.CustomIdiomQuiz else Route.CustomQuiz) {
                inclusive = true
            }
        }
    }
}

@Composable
private fun rememberSpeaker(): Speaker {
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

@Composable
private fun HomeScreen(navController: NavHostController, viewModel: MainViewModel = hiltViewModel()) {
    val summary by viewModel.summary.collectAsState()
    BlueScaffold(
        title = "TOEIC Vocab Trainer",
        actions = {
            IconButton(onClick = { navController.navigate(Route.Settings) }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("目標スコア別に10問トレーニング", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("累計学習", formatSeconds(summary.totalStudySeconds), Modifier.weight(1f))
                    StatCard("今週", formatSeconds(summary.weekStudySeconds), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("英単語Master", "${summary.masteredLessons}/${summary.totalLessons}", Modifier.weight(1f))
                    StatCard("熟語Master", "${summary.idiomMasteredLessons}/${summary.idiomTotalLessons}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("復習単語", "${summary.reviewCount}", Modifier.weight(1f))
                    StatCard("連続学習", "${summary.streakDays}日", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("文章登録", "${summary.sentenceCount}文", Modifier.weight(1f))
                    StatCard("今週学習", "${summary.weekStudySeconds / 60}分", Modifier.weight(1f))
                }
            }
            item {
                CardButton(
                    title = "新規単語登録",
                    subtitle = "覚えたい英単語と日本語訳を追加",
                    icon = Icons.Default.Add,
                    onClick = { navController.navigate(Route.AddWord) }
                )
            }
            item {
                CardButton(
                    title = "単語・熟語インポート",
                    subtitle = "Excelから保存したCSVを一括登録",
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = { navController.navigate(Route.WordImport) }
                )
            }
            item {
                CardButton(
                    title = "英単語",
                    subtitle = "カスタム登録した英単語の問題一覧",
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = { navController.navigate(Route.customTraining("word")) }
                )
            }
            item {
                CardButton(
                    title = "英熟語",
                    subtitle = "カスタム登録した英熟語の問題一覧",
                    icon = Icons.Default.School,
                    onClick = { navController.navigate(Route.customTraining("idiom")) }
                )
            }
            item {
                CardButton(
                    title = "ランダム英単語&英熟語",
                    subtitle = "登録済みの英単語・英熟語からランダム10問",
                    icon = Icons.Default.PlayArrow,
                    onClick = { navController.navigate(Route.RandomCustomMenu) }
                )
            }
            item {
                CardButton(
                    title = "文章問題",
                    subtitle = if (summary.sentenceCount > 0)
                        "登録済み ${summary.sentenceCount}文 · 並べ替え問題を出題"
                    else
                        "登録した英文から並べ替え問題を出題",
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = { navController.navigate(Route.SentenceMenu) }
                )
            }
            item {
                CardButton(
                    title = "復習トレーニング",
                    subtitle = "間違えた単語とチェックした単語を10問で復習",
                    icon = Icons.Default.Refresh,
                    onClick = { navController.navigate(Route.Review) }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    BottomAction("学習ログ", Icons.Default.School, Modifier.weight(1f)) { navController.navigate(Route.StudyLog) }
                    BottomAction("設定", Icons.Default.Settings, Modifier.weight(1f)) { navController.navigate(Route.Settings) }
                }
            }
        }
    }
}

@Composable
private fun LessonListScreen(navController: NavHostController, viewModel: LessonListViewModel = hiltViewModel()) {
    val lessons by viewModel.lessons.collectAsState()
    BlueScaffold(title = "英単語", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { navController.navigate(Route.AddWord) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("単語登録", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { navController.navigate(Route.CustomWordList) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("登録一覧", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { navController.navigate(Route.customTraining("word")) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("カスタム単語クイズ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            val grouped = lessons.groupBy { it.scoreTarget }
            grouped.forEach { (score, items) ->
                item { SectionTitle("目標${score}点") }
                items(items) { lesson -> LessonCard(lesson) { navController.navigate(Route.training(lesson.id)) } }
            }
        }
    }
}

@Composable
private fun IdiomLessonListScreen(navController: NavHostController, viewModel: IdiomLessonListViewModel = hiltViewModel()) {
    val lessons by viewModel.lessons.collectAsState()
    val totalWords = lessons.sumOf { it.wordEndNumber - it.wordStartNumber + 1 }
    BlueScaffold(title = "英熟語", onBack = { navController.popBackStack() }) { inner ->
        if (lessons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(inner), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("読み込み中…", color = Color.White, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("カスタム英熟語")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate(Route.AddIdiom) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                                Spacer(Modifier.width(4.dp))
                                Text("英熟語登録", color = DeepBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { navController.navigate(Route.CustomIdiomList) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = BrightBlue)
                                Spacer(Modifier.width(4.dp))
                                Text("登録一覧", color = DeepBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = { navController.navigate(Route.customTraining("idiom")) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("カスタム英熟語クイズ", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { SectionTitle("英熟語レッスン（全${totalWords}語）") }
                items(lessons) { lesson ->
                    LessonCard(lesson) { navController.navigate(Route.training(lesson.id)) }
                }
            }
        }
    }
}

@Composable
private fun TrainingListScreen(navController: NavHostController, viewModel: TrainingListViewModel = hiltViewModel()) {
    val trainings by viewModel.trainings.collectAsState()
    val isIdiom = viewModel.lessonId >= 100
    val screenTitle = if (isIdiom) "英熟語 トレーニング" else "トレーニング一覧"
    val sectionTitle = if (isIdiom) "英熟語 トレーニング一覧" else "トレーニング一覧"
    BlueScaffold(title = screenTitle, onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionTitle(sectionTitle) }
            items(trainings) { training ->
                TrainingCard(
                    training = training,
                    onQuiz = { navController.navigate(Route.quiz(training.id)) },
                    onDetail = { wordId -> navController.navigate(Route.word(wordId)) },
                    onFlashcard = { navController.navigate(Route.flashcard(training.id)) }
                )
            }
        }
    }
}

@Composable
private fun QuizScreen(navController: NavHostController, viewModel: QuizViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.finishedAttemptId) {
        state.finishedAttemptId?.let {
            navController.navigate(Route.result(it)) {
                popUpTo(Route.Home)
            }
        }
    }
    BlueScaffold(title = if (state.isReview) "復習クイズ" else "10問クイズ", onBack = { navController.popBackStack() }) { inner ->
        if (state.questions.isEmpty()) {
            EmptyMessage(
                modifier = Modifier.padding(inner).background(BrightBlue),
                title = "出題できる単語がありません",
                button = "戻る",
                onClick = { navController.popBackStack() }
            )
        } else {
            QuizContent(Modifier.padding(inner), state, viewModel::submit)
        }
    }
}

@Composable
private fun ResultScreen(navController: NavHostController, viewModel: ResultViewModel = hiltViewModel()) {
    val result by viewModel.result.collectAsState()
    val trainingLabel by viewModel.trainingLabel.collectAsState()
    val title = trainingLabel ?: "クイズ結果"
    BlueScaffold(title = title, onBack = { navController.navigate(Route.Home) }) { inner ->
        result?.let {
            ResultContent(
                result = it,
                modifier = Modifier.padding(inner),
                onRetry = { navController.navigate(Route.quiz(it.trainingId, it.isReview)) },
                onHome = { navController.navigate(Route.Home) { popUpTo(Route.Home) { inclusive = true } } },
                onNext = { navController.navigate(Route.Lessons) }
            )
        } ?: Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ReviewScreen(navController: NavHostController, viewModel: ReviewViewModel = hiltViewModel()) {
    val words by viewModel.words.collectAsState()
    BlueScaffold(title = "復習", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = { navController.navigate(Route.quiz(isReview = true)) },
                    enabled = words.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("復習クイズを開始", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (words.isEmpty()) {
                item { EmptyCard("復習対象の単語はまだありません") }
            } else {
                items(words) { word ->
                    WordRow(
                        word = word,
                        action = {
                            IconButton(onClick = { viewModel.remove(word.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Danger)
                            }
                        },
                        onClick = { navController.navigate(Route.word(word.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WordDetailScreen(navController: NavHostController, viewModel: WordDetailViewModel = hiltViewModel()) {
    val word by viewModel.word.collectAsState()
    val relations by viewModel.relations.collectAsState()
    val speaker = rememberSpeaker()
    BlueScaffold(title = "単語詳細", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                word?.let {
                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(it.english, fontSize = 34.sp, fontWeight = FontWeight.Black, color = DeepBlue, modifier = Modifier.weight(1f))
                                IconButton(onClick = { speaker.speak(it.english) }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Audio", tint = BrightBlue) }
                            }
                            Text(it.phonetic, color = TextMuted, fontSize = 18.sp)
                            Text("${it.partOfSpeech}  ${it.meaning}", color = TextDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text(it.exampleSentence, color = TextDark, fontSize = 18.sp)
                            Text(it.exampleTranslation, color = TextMuted, fontSize = 16.sp)
                            if (relations.isNotEmpty()) {
                                Text("関連語", color = TextMuted, fontWeight = FontWeight.Bold)
                                relations.forEach { rel -> Text("${rel.relatedWord}: ${rel.relatedMeaning}", color = TextDark) }
                            }
                            Button(onClick = viewModel::addReview, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("復習対象に追加")
                            }
                        }
                    }
                } ?: CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun StudyLogScreen(navController: NavHostController, viewModel: StudyLogViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsState()
    BlueScaffold(title = "学習ログ", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (logs.isEmpty()) {
                item { EmptyCard("学習ログはまだありません") }
            } else {
                items(logs) { log ->
                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(formatDate(log.studiedAt), fontWeight = FontWeight.Bold, color = TextDark)
                                Text("正解 ${log.correctCount} / 不正解 ${log.wrongCount}", color = TextMuted)
                            }
                            Text(formatSeconds(log.studySeconds), color = DeepBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(navController: NavHostController, viewModel: MainViewModel = hiltViewModel()) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteCustomDialog by remember { mutableStateOf(false) }
    var showDeleteSentenceDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("学習進捗をリセット") },
            text = { Text("すべての学習進捗・クイズ履歴・復習リストを削除します。\nこの操作は元に戻せません。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetProgress(); showResetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text("リセット") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) { Text("キャンセル") }
            }
        )
    }

    if (showDeleteCustomDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCustomDialog = false },
            title = { Text("カスタム単語を全削除") },
            text = { Text("登録したすべてのカスタム単語を削除します。\nこの操作は元に戻せません。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAllCustomWords(); showDeleteCustomDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text("削除") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteCustomDialog = false }) { Text("キャンセル") }
            }
        )
    }

    if (showDeleteSentenceDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSentenceDialog = false },
            title = { Text("カスタム文章を全削除") },
            text = { Text("登録したすべての文章問題を削除します。\nこの操作は元に戻せません。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAllCustomSentences(); showDeleteSentenceDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text("削除") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteSentenceDialog = false }) { Text("キャンセル") }
            }
        )
    }

    BlueScaffold(title = "設定", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("アプリ情報", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Text("TOEIC向け英単語・英熟語を、10問単位の4択クイズで学習するローカル保存型アプリです。", color = TextMuted)
                    Text("英単語 100語（Lesson 1〜4） / 英熟語 30語（Lesson 1）", color = TextMuted, fontSize = 13.sp)
                    Text("バージョン 1.9.0", color = TextMuted, fontSize = 13.sp)
                }
            }
            Text("データ管理", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(4.dp)) {
                    OutlinedButton(
                        onClick = { showDeleteCustomDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = null
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text("カスタム単語を全削除", color = Danger, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = TextMuted.copy(alpha = 0.1f))
                    OutlinedButton(
                        onClick = { showDeleteSentenceDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = null
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text("カスタム文章を全削除", color = Danger, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = TextMuted.copy(alpha = 0.1f))
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = null
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text("学習進捗をリセット", color = Danger, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomWordQuizResultContent(
    correctCount: Int,
    total: Int,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accuracy = correctCount * 100f / total
    val isPerfect = correctCount == total
    val resId = medalResId(correctCount, total)
    val title = medalTitle(correctCount, total)
    val message = medalMessage(correctCount, total)

    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    var medalVisible by remember { mutableStateOf(false) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val animDuration = 1800
        playSynthSound(
            listOf(Pair(330f, 120), Pair(440f, 130), Pair(523f, 140), Pair(659f, 160), Pair(784f, 200), Pair(1047f, 280)),
            false
        )
        launch {
            animProgress.animateTo(
                targetValue = (accuracy / 100f).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = animDuration, easing = LinearEasing)
            )
        }
        val finalAcc = accuracy.toInt()
        val startTime = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed >= animDuration) { displayedAccuracy = finalAcc; break }
            displayedAccuracy = ((elapsed.toFloat() / animDuration) * finalAcc).toInt()
            delay(16L)
        }
        delay(200L)
        medalVisible = true
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val req = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build())
                .setOnAudioFocusChangeListener {}.build()
            am.requestAudioFocus(req)
            val mp = MediaPlayer.create(context, R.raw.new_medal_sound)
            mp?.setVolume(0.6f, 0.6f)
            mp?.setOnCompletionListener { it.release(); am.abandonAudioFocusRequest(req) }
            mp?.start()
        } catch (_: Exception) {}
        launch { medalAlpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing)) }
        medalScale.animateTo(1.15f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        medalScale.animateTo(0.95f, animationSpec = tween(120))
        medalScale.animateTo(1f, animationSpec = tween(100))
    }

    Column(
        modifier = modifier.background(BrightBlue).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ResultSectionCard(header = "正解率") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val medalMod = when {
                    isPerfect -> Modifier.size(110.dp)
                    resId == R.drawable.medal_bronze -> Modifier.size(width = 107.dp, height = 160.dp)
                    else -> Modifier.size(160.dp)
                }
                if (medalVisible) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        modifier = medalMod.scale(medalScale.value).alpha(medalAlpha.value),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Spacer(medalMod)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("$correctCount/${total}正解", color = TextMuted, fontSize = 16.sp)
                    Text("$displayedAccuracy%", color = if (isPerfect) Gold else DeepBlue, fontSize = 52.sp, fontWeight = FontWeight.Black, lineHeight = 58.sp)
                }
            }
            LinearProgressIndicator(
                progress = { animProgress.value },
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp).height(14.dp).clip(RoundedCornerShape(7.dp)),
                color = Teal, trackColor = Color(0xFFDDE5EC)
            )
        }
        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(title, color = DeepBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(message, color = TextMuted, textAlign = TextAlign.Center, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("再チャレンジ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("ホームへ")
        }
    }
}

@Composable
private fun CustomIdiomQuizScreen(navController: NavHostController, viewModel: CustomIdiomQuizViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    if (state.finishedAttemptId != null) {
        val total = state.questions.size.coerceAtLeast(1)
        BlueScaffold(title = "カスタム英熟語クイズ") { inner ->
            CustomWordQuizResultContent(
                correctCount = state.correctCount,
                total = total,
                onRetry = { navController.navigate(Route.CustomIdiomQuiz) { popUpTo(Route.CustomIdiomQuiz) { inclusive = true } } },
                onHome = { navController.navigate(Route.Home) { popUpTo(Route.Home) { inclusive = true } } },
                modifier = Modifier.fillMaxSize().padding(inner)
            )
        }
    } else {
        BlueScaffold(title = "カスタム英熟語クイズ", onBack = { navController.popBackStack() }) { inner ->
            when {
                state.startedAt == 0L -> Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.questions.isEmpty() -> EmptyMessage(Modifier.padding(inner).background(BrightBlue), "クイズには4つ以上の英熟語を登録してください", "戻る") { navController.popBackStack() }
                else -> QuizContent(Modifier.padding(inner), state, viewModel::submit)
            }
        }
    }
}

@Composable
private fun CustomWordQuizScreen(navController: NavHostController, viewModel: CustomWordQuizViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    if (state.finishedAttemptId != null) {
        val total = state.questions.size.coerceAtLeast(1)
        BlueScaffold(title = "カスタム単語クイズ") { inner ->
            CustomWordQuizResultContent(
                correctCount = state.correctCount,
                total = total,
                onRetry = { navController.navigate(Route.CustomQuiz) { popUpTo(Route.CustomQuiz) { inclusive = true } } },
                onHome = { navController.navigate(Route.Home) { popUpTo(Route.Home) { inclusive = true } } },
                modifier = Modifier.fillMaxSize().padding(inner)
            )
        }
    } else {
        BlueScaffold(title = "カスタム単語クイズ", onBack = { navController.popBackStack() }) { inner ->
            when {
                state.startedAt == 0L -> Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.questions.isEmpty() -> EmptyMessage(Modifier.padding(inner).background(BrightBlue), "クイズには4つ以上の単語を登録してください", "戻る") { navController.popBackStack() }
                else -> QuizContent(Modifier.padding(inner), state, viewModel::submit)
            }
        }
    }
}

@Composable
private fun FlashcardScreen(navController: NavHostController, viewModel: FlashcardViewModel = hiltViewModel()) {
    val words by viewModel.words.collectAsState()
    val index by viewModel.index.collectAsState()
    val revealed by viewModel.revealed.collectAsState()
    val speaker = rememberSpeaker()
    val word = words.getOrNull(index)
    val title = if (viewModel.trainingId >= 100) "英熟語帳" else "単語帳"
    // 単語帳でもカード切り替え時に自動読み上げ
    LaunchedEffect(word?.id, speaker.isReady) {
        if (word == null) return@LaunchedEffect
        delay(150L)
        speaker.speak(word.english)
    }
    BlueScaffold(title = title, onBack = { navController.popBackStack() }) { inner ->
        if (words.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${index + 1} / ${words.size}", color = TextMuted, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { (index + 1) / words.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = AccentBlue, trackColor = Color.White
                )
                word?.let { w ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().weight(1f).clickable { viewModel.toggleReveal() },
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            Modifier.fillMaxSize().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(w.english, fontSize = 38.sp, fontWeight = FontWeight.Black, color = DeepBlue, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                IconButton(onClick = { speaker.speak(w.english) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "読み上げ", tint = BrightBlue)
                                }
                            }
                            Text(w.phonetic, color = TextMuted, fontSize = 18.sp)
                            if (revealed) {
                                Spacer(Modifier.height(20.dp))
                                Text(w.meaning, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Success, textAlign = TextAlign.Center)
                                if (w.partOfSpeech.isNotBlank()) Text(w.partOfSpeech, color = TextMuted, fontSize = 16.sp)
                                if (w.exampleSentence.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(w.exampleSentence, color = TextDark, fontSize = 15.sp, textAlign = TextAlign.Center)
                                    Text(w.exampleTranslation, color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                                }
                            } else {
                                Spacer(Modifier.height(24.dp))
                                Text("タップして意味を確認", color = TextMuted, fontSize = 16.sp)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = viewModel::prev,
                        enabled = index > 0,
                        modifier = Modifier.weight(1f).height(54.dp)
                    ) { Text("← 前へ", fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = viewModel::next,
                        enabled = index < words.lastIndex,
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue)
                    ) { Text("次へ →", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun CustomWordListScreen(navController: NavHostController, viewModel: CustomWordListViewModel = hiltViewModel()) {
    val words by viewModel.words.collectAsState()
    BlueScaffold(title = "登録単語一覧 (${words.size}語)", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (words.isEmpty()) {
                item { EmptyCard("登録された単語はありません\n「単語登録」から追加してください") }
            } else {
                val chunks = words.chunked(10)
                chunks.forEachIndexed { idx, chunk ->
                    val start = idx * 10 + 1
                    val end = start + chunk.size - 1
                    val preview = buildSectionPreview(chunk.map { it.english })
                    item(key = "word_header_$idx") { ListSectionHeader(start = start, end = end, preview = preview, showDivider = idx > 0) }
                    items(chunk, key = { it.id }) { word ->
                        CustomWordRow(word = word, onDelete = { viewModel.delete(word.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomIdiomListScreen(navController: NavHostController, viewModel: CustomIdiomListViewModel = hiltViewModel()) {
    val idioms by viewModel.idioms.collectAsState()
    BlueScaffold(title = "登録英熟語一覧 (${idioms.size}語)", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (idioms.isEmpty()) {
                item { EmptyCard("登録された英熟語はありません\n「英熟語登録」から追加してください") }
            } else {
                val chunks = idioms.chunked(10)
                chunks.forEachIndexed { idx, chunk ->
                    val start = idx * 10 + 1
                    val end = start + chunk.size - 1
                    val preview = buildSectionPreview(chunk.map { it.english })
                    item(key = "idiom_header_$idx") { ListSectionHeader(start = start, end = end, preview = preview, showDivider = idx > 0) }
                    items(chunk, key = { it.id }) { idiom ->
                        CustomIdiomRow(idiom = idiom, onDelete = { viewModel.delete(idiom.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomIdiomRow(idiom: CustomIdiomEntity, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(idiom.english, color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(idiom.meaning, color = TextMuted, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Danger)
            }
        }
    }
}

@Composable
private fun AddIdiomScreen(navController: NavHostController, viewModel: AddIdiomViewModel = hiltViewModel()) {
    val saved by viewModel.saved.collectAsState()
    var english by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }
    var meaningInput by remember { mutableStateOf<EditText?>(null) }
    LaunchedEffect(saved) {
        if (saved) { viewModel.resetSaved(); navController.popBackStack() }
    }
    BlueScaffold(title = "英熟語登録", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AddWordCardPadding), verticalArrangement = Arrangement.spacedBy(AddWordCardSpacing)) {
                    AddWordField(
                        label = "英熟語",
                        placeholder = "例: as soon as possible, keep in mind",
                        value = english,
                        onValueChange = { english = it },
                        imeAction = EditorInfo.IME_ACTION_NEXT,
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
                        autoFocus = true,
                        onImeAction = { meaningInput?.focusAndShowKeyboard() },
                    )
                    AddWordField(
                        label = "日本語の意味",
                        placeholder = "例: できるだけ早く、心に留めておく",
                        value = meaning,
                        onValueChange = { meaning = it },
                        imeAction = EditorInfo.IME_ACTION_DONE,
                        onReady = { meaningInput = it },
                    )
                    Button(
                        onClick = { viewModel.save(english, meaning) },
                        enabled = english.isNotBlank() && meaning.isNotBlank(),
                        modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.65f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue)
                    ) {
                        Text("登録する", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomTrainingListScreen(navController: NavHostController, viewModel: CustomTrainingListViewModel = hiltViewModel()) {
    val trainings by viewModel.trainings.collectAsState()
    val isIdiom = viewModel.type == "idiom"
    val title = if (isIdiom) "カスタム英熟語" else "カスタム英単語"
    val listRoute = if (isIdiom) Route.CustomIdiomList else Route.CustomWordList
    val blockLabel = if (isIdiom) "英熟語" else "英単語"
    BlueScaffold(title = title, onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { navController.navigate(if (isIdiom) Route.AddIdiom else Route.AddWord) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isIdiom) "英熟語登録" else "単語登録", color = DeepBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { navController.navigate(listRoute) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("登録一覧", color = DeepBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item { SectionTitle("100問単位") }
            if (trainings.isEmpty()) {
                item { EmptyCard("登録済みの問題がありません") }
            } else {
                val blocks = trainings.chunked(10)
                items(blocks) { block ->
                    val first = block.first()
                    val last = block.last()
                    val blockNumber = ((first.wordStartNumber - 1) / 100) + 1
                    CardButton(
                        title = "${first.wordStartNumber}~${last.wordEndNumber}問",
                        subtitle = "$blockLabel ${block.size}セット",
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        onClick = { navController.navigate(Route.customTrainingBlock(viewModel.type, blockNumber)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomTrainingBlockScreen(
    navController: NavHostController,
    blockNumber: Int,
    viewModel: CustomTrainingListViewModel = hiltViewModel()
) {
    val trainings by viewModel.trainings.collectAsState()
    val isIdiom = viewModel.type == "idiom"
    val titlePrefix = if (isIdiom) "カスタム英熟語" else "カスタム英単語"
    val listRoute = if (isIdiom) Route.CustomIdiomList else Route.CustomWordList
    val startQuestion = (blockNumber - 1).coerceAtLeast(0) * 100 + 1
    val endQuestion = blockNumber * 100
    val blockTrainings = trainings.drop((blockNumber - 1).coerceAtLeast(0) * 10).take(10)
    val titleEndQuestion = blockTrainings.lastOrNull()?.wordEndNumber ?: endQuestion
    BlueScaffold(title = "$titlePrefix $startQuestion~${titleEndQuestion}問", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionTitle("10問単位") }
            if (blockTrainings.isEmpty()) {
                item { EmptyCard("この範囲に登録済みの問題がありません") }
            } else {
                items(blockTrainings) { training ->
                    val setNumber = customSetNumber(training)
                    TrainingCard(
                        training = training,
                        onQuiz = { navController.navigate(Route.customTrainingQuiz(viewModel.type, setNumber)) },
                        onDetail = { navController.navigate(listRoute) },
                        onFlashcard = { navController.navigate(listRoute) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomTrainingQuizScreen(navController: NavHostController, viewModel: CustomTrainingQuizViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val result by viewModel.result.collectAsState()
    val isIdiom = viewModel.type == "idiom"
    val startQuestion = (viewModel.setNumber - 1).coerceAtLeast(0) * 10 + 1
    val endQuestion = viewModel.setNumber * 10
    val title = if (isIdiom) "カスタム英熟語 $startQuestion~${endQuestion}問" else "カスタム英単語 $startQuestion~${endQuestion}問"
    val listRoute = Route.customTrainingBlock(viewModel.type, ((viewModel.setNumber - 1).coerceAtLeast(0) / 10) + 1)
    if (state.finishedAttemptId != null && result != null) {
        BlueScaffold(title = title) { inner ->
            ResultContent(
                result = result!!,
                modifier = Modifier.padding(inner),
                onRetry = {
                    navController.navigate(Route.customTrainingQuiz(viewModel.type, viewModel.setNumber)) {
                        popUpTo(Route.customTrainingQuiz(viewModel.type, viewModel.setNumber)) { inclusive = true }
                    }
                },
                onHome = { navController.navigate(Route.Home) { popUpTo(Route.Home) { inclusive = true } } },
                onNext = { navController.navigate(listRoute) { popUpTo(listRoute) { inclusive = true } } }
            )
        }
    } else {
        BlueScaffold(title = title, onBack = { navController.popBackStack() }) { inner ->
            when {
                state.startedAt == 0L -> Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.questions.isEmpty() -> EmptyMessage(
                    Modifier.padding(inner).background(BrightBlue),
                    "クイズには4つ以上の${if (isIdiom) "英熟語" else "単語"}を登録してください",
                    "戻る"
                ) { navController.popBackStack() }
                else -> QuizContent(Modifier.padding(inner), state, viewModel::submit)
            }
        }
    }
}

@Composable
private fun RandomCustomMenuScreen(navController: NavHostController) {
    BlueScaffold(title = "ランダム英単語&英熟語", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionTitle("ランダム10問") }
            item {
                CardButton(
                    title = "ランダム英単語",
                    subtitle = "登録済みの英単語から毎回ランダムに10問出題",
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = { navController.navigate(Route.randomCustomQuiz("word")) }
                )
            }
            item {
                CardButton(
                    title = "ランダム英熟語",
                    subtitle = "登録済みの英熟語から毎回ランダムに10問出題",
                    icon = Icons.Default.School,
                    onClick = { navController.navigate(Route.randomCustomQuiz("idiom")) }
                )
            }
        }
    }
}

@Composable
private fun RandomCustomQuizScreen(navController: NavHostController, viewModel: RandomCustomQuizViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val result by viewModel.result.collectAsState()
    val isIdiom = viewModel.type == "idiom"
    val title = if (isIdiom) "ランダム英熟語" else "ランダム英単語"
    if (state.finishedAttemptId != null && result != null) {
        BlueScaffold(title = title) { inner ->
            ResultContent(
                result = result!!,
                modifier = Modifier.padding(inner),
                onRetry = {
                    navController.navigate(Route.randomCustomQuiz(viewModel.type)) {
                        popUpTo(Route.randomCustomQuiz(viewModel.type)) { inclusive = true }
                    }
                },
                onHome = { navController.navigate(Route.Home) { popUpTo(Route.Home) { inclusive = true } } },
                onNext = { navController.navigate(Route.RandomCustomMenu) { popUpTo(Route.RandomCustomMenu) { inclusive = true } } }
            )
        }
    } else {
        BlueScaffold(title = title, onBack = { navController.popBackStack() }) { inner ->
            when {
                state.startedAt == 0L -> Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.questions.isEmpty() -> EmptyMessage(
                    Modifier.padding(inner).background(BrightBlue),
                    "ランダムクイズには4つ以上の${if (isIdiom) "英熟語" else "単語"}を登録してください",
                    "戻る"
                ) { navController.popBackStack() }
                else -> QuizContent(Modifier.padding(inner), state, viewModel::submit)
            }
        }
    }
}

private fun buildSectionPreview(items: List<String>, limit: Int = 2): String {
    val head = items.take(limit).joinToString(", ") { it.trim() }
    return if (items.size > limit) "$head, ...(省略)" else head
}

private fun customSetNumber(training: Training): Int =
    ((training.wordStartNumber - 1) / 10) + 1

@Composable
private fun ListSectionHeader(start: Int, end: Int, preview: String = "", showDivider: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (showDivider) {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = TextMuted.copy(alpha = 0.2f), thickness = 0.5.dp)
            Spacer(Modifier.height(4.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "[$start~$end]",
                color = DeepBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            if (preview.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Text(preview, color = TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CustomWordRow(word: CustomWordEntity, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(word.english, color = DeepBlue, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(word.meaning, color = TextMuted, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Danger)
            }
        }
    }
}

private val AddWordCardPadding = 24.dp
private val AddWordCardSpacing = 20.dp

@Composable
private fun AddWordField(
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

private fun android.view.View.showKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

private fun EditText.focusAndShowKeyboard() {
    requestFocus()
    setSelection(text?.length ?: 0)
    showKeyboard()
}

private fun Context.readImportFileAsCsv(uri: Uri): String {
    Log.d(IMPORT_TAG, "readImportFileAsCsv: start uri=$uri")
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: error("ファイルを開けませんでした")
    if (bytes.isEmpty()) error("ファイルが空です")

    val fileName = queryDisplayName(uri).lowercase(Locale.ROOT)
    val mimeType = contentResolver.getType(uri).orEmpty().lowercase(Locale.ROOT)
    Log.d(IMPORT_TAG, "readImportFileAsCsv: fileName=$fileName mimeType=$mimeType size=${bytes.size}")

    val isZipMagic = bytes.startsWith(byteArrayOf(0x50, 0x4B, 0x03, 0x04))
    val isOle2Magic = bytes.startsWith(byteArrayOf(0xD0.toByte(), 0xCF.toByte(), 0x11, 0xE0.toByte()))
    val isXlsx = isZipMagic ||
        fileName.endsWith(".xlsx") ||
        mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    val isOldXls = isOle2Magic ||
        (!isZipMagic && fileName.endsWith(".xls")) ||
        (!isZipMagic && mimeType == "application/vnd.ms-excel")
    Log.d(IMPORT_TAG, "readImportFileAsCsv: isXlsx=$isXlsx isOldXls=$isOldXls isZipMagic=$isZipMagic isOle2Magic=$isOle2Magic")

    if (isOldXls && !isXlsx) {
        error("古い .xls 形式は未対応です。Excelで .xlsx または CSV として保存してから選択してください")
    }

    return if (isXlsx) {
        Log.d(IMPORT_TAG, "readImportFileAsCsv: parsing as XLSX")
        parseXlsxRows(bytes).toCsvText()
    } else {
        Log.d(IMPORT_TAG, "readImportFileAsCsv: parsing as CSV")
        decodeCsvBytes(bytes)
    }
}

private fun Context.queryDisplayName(uri: Uri): String {
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return cursor.getString(index).orEmpty()
        }
    }
    return uri.lastPathSegment.orEmpty()
}

private fun decodeCsvBytes(bytes: ByteArray): String {
    val withoutBom = if (bytes.startsWith(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))) {
        bytes.copyOfRange(3, bytes.size)
    } else {
        bytes
    }
    return runCatching { strictDecode(withoutBom, StandardCharsets.UTF_8) }
        .getOrElse { strictDecode(withoutBom, Charset.forName("MS932")) }
}

private fun strictDecode(bytes: ByteArray, charset: Charset): String =
    charset.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(bytes))
        .toString()

private val XLSX_TARGET_ENTRIES = setOf(
    "xl/sharedStrings.xml",
    "xl/worksheets/sheet1.xml",
    "xl/_rels/workbook.xml.rels",
    "xl/workbook.xml"
)

private fun parseXlsxRows(bytes: ByteArray): List<List<String>> {
    Log.d(IMPORT_TAG, "parseXlsxRows: start, bytes=${bytes.size}")
    if (bytes.size < 4 || !bytes.startsWith(byteArrayOf(0x50, 0x4B, 0x03, 0x04))) {
        Log.e(IMPORT_TAG, "parseXlsxRows: not a valid ZIP/XLSX file (magic bytes mismatch)")
        error("選択されたファイルは有効なExcelファイル(.xlsx)ではありません。ファイル形式を確認してください。")
    }
    val entries = mutableMapOf<String, ByteArray>()
    try {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name
                Log.d(IMPORT_TAG, "parseXlsxRows: ZIP entry name=$name isDir=${entry.isDirectory}")
                var entryBytes: ByteArray? = null
                try {
                    entryBytes = zip.readBytes()
                } catch (e: Exception) {
                    Log.w(IMPORT_TAG, "parseXlsxRows: readBytes error on '$name': ${e.javaClass.simpleName}: ${e.message}")
                }
                if (entryBytes != null && !entry.isDirectory && name in XLSX_TARGET_ENTRIES) {
                    entries[name] = entryBytes
                    Log.d(IMPORT_TAG, "parseXlsxRows: captured $name (${entryBytes.size} bytes)")
                }
                try {
                    zip.closeEntry()
                } catch (e: java.util.zip.ZipException) {
                    Log.w(IMPORT_TAG, "parseXlsxRows: closeEntry ZipException on '$name': ${e.message} (ignoring CRC issue)")
                } catch (e: Exception) {
                    Log.w(IMPORT_TAG, "parseXlsxRows: closeEntry error on '$name': ${e.javaClass.simpleName}: ${e.message}")
                }
                entry = try { zip.nextEntry } catch (e: java.util.zip.ZipException) {
                    Log.w(IMPORT_TAG, "parseXlsxRows: ZipException advancing to next entry: ${e.message}")
                    break
                }
            }
        }
    } catch (e: java.util.zip.ZipException) {
        Log.e(IMPORT_TAG, "parseXlsxRows: fatal ZipException reading XLSX ZIP: ${e.message}")
        if (entries.isEmpty()) error("ZIPファイルの読み込みに失敗しました: ${e.message}")
    }
    Log.d(IMPORT_TAG, "parseXlsxRows: captured entries=${entries.keys}")

    val sheetPath = parseWorkbookSheetPath(entries["xl/_rels/workbook.xml.rels"])
        ?: "xl/worksheets/sheet1.xml"
    Log.d(IMPORT_TAG, "parseXlsxRows: resolved sheetPath=$sheetPath")

    if (!entries.containsKey(sheetPath) && sheetPath != "xl/worksheets/sheet1.xml") {
        Log.d(IMPORT_TAG, "parseXlsxRows: sheet at $sheetPath not captured, re-scanning ZIP")
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null && !entries.containsKey(sheetPath)) {
                    if (!entry.isDirectory && entry.name == sheetPath) {
                        entries[sheetPath] = zip.readBytes()
                        Log.d(IMPORT_TAG, "parseXlsxRows: re-scan captured $sheetPath")
                    }
                    try { zip.closeEntry() } catch (e: java.util.zip.ZipException) {
                        Log.w(IMPORT_TAG, "parseXlsxRows: re-scan closeEntry error: ${e.message}")
                    }
                    entry = try { zip.nextEntry } catch (e: java.util.zip.ZipException) { break }
                }
            }
        } catch (e: Exception) {
            Log.w(IMPORT_TAG, "parseXlsxRows: re-scan failed: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    val sheet = entries[sheetPath]
        ?: error("Excelファイルの1枚目のシートを読み込めませんでした (パス: $sheetPath, 取得済みエントリ: ${entries.keys})")
    val sharedStrings = entries["xl/sharedStrings.xml"]?.let(::parseSharedStrings).orEmpty()
    Log.d(IMPORT_TAG, "parseXlsxRows: sharedStrings.size=${sharedStrings.size}")
    return parseWorksheetRows(sheet, sharedStrings)
}

private fun parseWorkbookSheetPath(relsBytes: ByteArray?): String? {
    if (relsBytes == null) {
        Log.d(IMPORT_TAG, "parseWorkbookSheetPath: no workbook.xml.rels found, using default")
        return null
    }
    return try {
        val parser = newXmlParser(relsBytes)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                val localName = parser.name.substringAfterLast(':')
                if (localName == "Relationship") {
                    val type = parser.getAttributeValue(null, "Type").orEmpty()
                    val target = parser.getAttributeValue(null, "Target").orEmpty()
                    Log.d(IMPORT_TAG, "parseWorkbookSheetPath: Relationship type=$type target=$target")
                    if (type.endsWith("/worksheet") && target.isNotBlank()) {
                        val resolved = if (target.startsWith("/")) {
                            target.trimStart('/')
                        } else {
                            "xl/$target"
                        }
                        Log.d(IMPORT_TAG, "parseWorkbookSheetPath: resolved sheet path=$resolved")
                        return resolved
                    }
                }
            }
        }
        Log.w(IMPORT_TAG, "parseWorkbookSheetPath: no worksheet relationship found in workbook.xml.rels")
        null
    } catch (e: Exception) {
        Log.w(IMPORT_TAG, "parseWorkbookSheetPath: failed to parse workbook relationships: ${e.javaClass.simpleName}: ${e.message}")
        null
    }
}

private fun parseSharedStrings(bytes: ByteArray): List<String> {
    Log.d(IMPORT_TAG, "parseSharedStrings: parsing ${bytes.size} bytes")
    val parser = newXmlParser(bytes)
    val values = mutableListOf<String>()
    var insideSi = false
    var current = StringBuilder()

    try {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    if (tag == "si") {
                        insideSi = true
                        current = StringBuilder()
                    }
                }
                XmlPullParser.TEXT -> if (insideSi) current.append(parser.text)
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    if (tag == "si") {
                        values += current.toString()
                        insideSi = false
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e(IMPORT_TAG, "parseSharedStrings: parse error after ${values.size} entries: ${e.javaClass.simpleName}: ${e.message}")
    }
    Log.d(IMPORT_TAG, "parseSharedStrings: parsed ${values.size} shared strings")
    return values
}

private fun parseWorksheetRows(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
    Log.d(IMPORT_TAG, "parseWorksheetRows: parsing ${bytes.size} bytes, sharedStrings=${sharedStrings.size}")
    val parser = newXmlParser(bytes)
    val rows = mutableListOf<List<String>>()
    var currentRow: MutableList<String>? = null
    var cellReference = ""
    var cellType = ""
    var cellValue = StringBuilder()
    var readingValue = false
    var readingInlineText = false

    try {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    when (tag) {
                        "row" -> currentRow = mutableListOf()
                        "c" -> {
                            cellReference = parser.getAttributeValue(null, "r").orEmpty()
                            cellType = parser.getAttributeValue(null, "t").orEmpty()
                            cellValue = StringBuilder()
                        }
                        "v" -> readingValue = true
                        "t" -> if (cellType == "inlineStr") readingInlineText = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (readingValue || readingInlineText) cellValue.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.substringAfterLast(':')
                    when (tag) {
                        "v" -> readingValue = false
                        "t" -> readingInlineText = false
                        "c" -> {
                            currentRow?.let { row ->
                                val columnIndex = xlsxColumnIndex(cellReference)
                                while (row.size < columnIndex) row += ""
                                row += resolveXlsxCellValue(cellValue.toString(), cellType, sharedStrings)
                            }
                        }
                        "row" -> {
                            currentRow?.dropLastWhile { it.isBlank() }
                                ?.takeIf { row -> row.any { it.isNotBlank() } }
                                ?.let(rows::add)
                            currentRow = null
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e(IMPORT_TAG, "parseWorksheetRows: parse error after ${rows.size} rows: ${e.javaClass.simpleName}: ${e.message}")
    }
    Log.d(IMPORT_TAG, "parseWorksheetRows: parsed ${rows.size} rows")
    return rows
}

private fun newXmlParser(bytes: ByteArray): XmlPullParser {
    val factory = XmlPullParserFactory.newInstance().apply {
        isNamespaceAware = false
    }
    return factory.newPullParser().apply {
        setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        setInput(InputStreamReader(ByteArrayInputStream(bytes), StandardCharsets.UTF_8))
    }
}

private fun resolveXlsxCellValue(rawValue: String, type: String, sharedStrings: List<String>): String {
    val resolved = when (type) {
        "s" -> {
            val idx = rawValue.toIntOrNull()
            if (idx == null) {
                Log.w(IMPORT_TAG, "resolveXlsxCellValue: shared string index not an int: '$rawValue'")
                ""
            } else {
                sharedStrings.getOrNull(idx).also {
                    if (it == null) Log.w(IMPORT_TAG, "resolveXlsxCellValue: shared string index $idx out of range (size=${sharedStrings.size})")
                }.orEmpty()
            }
        }
        "b" -> if (rawValue == "1") "TRUE" else "FALSE"
        "e" -> ""
        else -> rawValue
    }.trim()
    return resolved
}

private fun xlsxColumnIndex(reference: String): Int {
    if (reference.isBlank()) {
        Log.w(IMPORT_TAG, "xlsxColumnIndex: empty cell reference, defaulting to column 0")
        return 0
    }
    var result = 0
    val letters = reference.takeWhile { it.isLetter() }.uppercase(Locale.ROOT)
    if (letters.isEmpty()) {
        Log.w(IMPORT_TAG, "xlsxColumnIndex: no letter prefix in reference '$reference', defaulting to column 0")
        return 0
    }
    letters.forEach { char ->
        result = result * 26 + (char - 'A' + 1)
    }
    return maxOf(result - 1, 0)
}

private fun List<List<String>>.toCsvText(): String {
    Log.d(IMPORT_TAG, "toCsvText: converting ${size} rows to CSV")
    return joinToString("\n") { row ->
        row.joinToString(",") { cell ->
            val normalized = cell.replace("\r\n", " ").replace("\r", " ").replace("\n", " ")
            val escaped = normalized.replace("\"", "\"\"")
            if (escaped.any { it == ',' || it == '"' }) "\"$escaped\"" else escaped
        }
    }.also { Log.d(IMPORT_TAG, "toCsvText: result length=${it.length}") }
}

private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
    size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

@Composable
private fun WordImportScreen(navController: NavHostController, viewModel: WordImportViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val preview by viewModel.preview.collectAsState()
    val result by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.showLoading()
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val result = runCatching { context.readImportFileAsCsv(uri) }
                result.onSuccess { csvText ->
                    viewModel.loadCsv(csvText)
                }.onFailure { error ->
                    Log.e(IMPORT_TAG, "File read error: ${error.javaClass.simpleName}: ${error.message}", error)
                    viewModel.showMessage(
                        error.message?.let { "${error.javaClass.simpleName}: $it" }
                            ?: "ファイルを開けませんでした"
                    )
                }
            }
        }
    }

    BlueScaffold(title = "単語・熟語インポート", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Button(
                    onClick = {
                        picker.launch(
                            arrayOf(
                                "text/*",
                                "text/csv",
                                "application/csv",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Excel / CSVファイルを選択", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = BrightBlue)
                        Spacer(Modifier.width(12.dp))
                        Text("処理中...", color = TextDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
            message?.let { text ->
                item { Text(text, color = Danger, fontWeight = FontWeight.Bold) }
            }
            preview?.let { currentPreview ->
                item {
                    ImportSummaryCard(
                        title = if (result == null) "読み込み結果" else "登録結果",
                        totalRows = result?.totalRows ?: currentPreview.totalRows,
                        newCount = result?.insertedCount ?: currentPreview.newWords.count { it.type == "word" },
                        newIdiomCount = result?.insertedIdiomCount ?: currentPreview.newWords.count { it.type == "phrase" || it.type == "sentence" },
                        duplicateCount = result?.duplicateCount ?: currentPreview.duplicateCount,
                        errorCount = result?.errorCount ?: currentPreview.errorCount
                    )
                }
                if (result == null) {
                    item {
                        Button(
                            onClick = { viewModel.registerPreview() },
                            enabled = currentPreview.newCount > 0 && !isLoading,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Success),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("登録する", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (currentPreview.newWords.isNotEmpty()) {
                    item { SectionTitle("登録予定 (${currentPreview.newCount}件)") }
                    items(currentPreview.newWords) { word ->
                        ImportWordRow(word.english, word.meaning, word.type)
                    }
                }
                if (currentPreview.duplicateWords.isNotEmpty()) {
                    item { SectionTitle("重複スキップ (${currentPreview.duplicateCount}件)") }
                    items(currentPreview.duplicateWords.take(20)) { word ->
                        ImportWordRow(word.english, word.meaning, word.type)
                    }
                }
                if (currentPreview.errors.isNotEmpty()) {
                    item { SectionTitle("エラー (${currentPreview.errorCount}件)") }
                    items(currentPreview.errors.take(20)) { error ->
                        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("${error.rowNumber}行目", color = Danger, fontWeight = FontWeight.Bold)
                                Text(error.reason, color = TextDark)
                                Text(error.rawValues.joinToString(", "), color = TextMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportSummaryCard(title: String, totalRows: Int, newCount: Int, newIdiomCount: Int, duplicateCount: Int, errorCount: Int) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryChip("読み込み", "${totalRows}件", Modifier.weight(1f))
                SummaryChip("単語登録", "${newCount}件", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryChip("熟語登録", "${newIdiomCount}件", Modifier.weight(1f))
                SummaryChip("重複", "${duplicateCount}件", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryChip("エラー", "${errorCount}件", Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(SoftBlue, RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ImportWordRow(english: String, meaning: String, type: String) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(english, color = DeepBlue, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(meaning, color = TextDark, fontSize = 15.sp)
            }
            Text(if (type == "phrase") "熟語" else "単語", color = BrightBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddWordScreen(navController: NavHostController, viewModel: AddWordViewModel = hiltViewModel()) {
    val saved by viewModel.saved.collectAsState()
    var english by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }
    var meaningInput by remember { mutableStateOf<EditText?>(null) }
    LaunchedEffect(saved) {
        if (saved) { viewModel.resetSaved(); navController.popBackStack() }
    }
    BlueScaffold(title = "新規単語・熟語登録", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AddWordCardPadding), verticalArrangement = Arrangement.spacedBy(AddWordCardSpacing)) {
                    AddWordField(
                        label = "英語",
                        placeholder = "例: apple, give up",
                        value = english,
                        onValueChange = { english = it },
                        imeAction = EditorInfo.IME_ACTION_NEXT,
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
                        autoFocus = true,
                        onImeAction = { meaningInput?.focusAndShowKeyboard() },
                    )
                    AddWordField(
                        label = "日本語",
                        placeholder = "例: りんご、美しい",
                        value = meaning,
                        onValueChange = { meaning = it },
                        imeAction = EditorInfo.IME_ACTION_DONE,
                        onReady = { meaningInput = it },
                    )
                    Text(
                        text = "スペースを含む場合は自動的にカスタム熟語として登録されます",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.save(english, meaning) },
                        enabled = english.isNotBlank() && meaning.isNotBlank(),
                        modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.65f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue)
                    ) {
                        Text("登録する", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlueScaffold(
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
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0x44FFFFFF))) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
private fun CardButton(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
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
private fun BottomAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(54.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBlue)) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
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
private fun TrainingCard(training: Training, onQuiz: () -> Unit, onDetail: (Int) -> Unit, onFlashcard: () -> Unit = {}) {
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
private fun QuizContent(modifier: Modifier, state: QuizState, onAnswer: (Int?) -> Unit) {
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
private fun ResultContent(result: QuizResult, modifier: Modifier, onRetry: () -> Unit, onHome: () -> Unit, onNext: () -> Unit) {
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
            mp?.setVolume(0.6f, 0.6f)
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
private fun WordRow(word: Word, action: @Composable () -> Unit, onClick: () -> Unit) {
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
private fun MasterBadge(isMaster: Boolean) {
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
private fun ResultSectionCard(header: String, content: @Composable () -> Unit) {
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
private fun SectionTitle(text: String) {
    Text(text, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
}

@Composable
private fun EmptyCard(text: String) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Text(text, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(28.dp))
    }
}

@Composable
private fun EmptyMessage(modifier: Modifier, title: String, button: String, onClick: () -> Unit) {
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        EmptyCard(title)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onClick) { Text(button) }
    }
}

private fun LessonStatus.label(): String = when (this) {
    LessonStatus.NotStarted -> "未学習"
    LessonStatus.InProgress -> "学習中"
    LessonStatus.Complete -> "完了"
    LessonStatus.Master -> "Master"
}

private fun medalResId(correctCount: Int, totalQuestions: Int): Int {
    if (totalQuestions == 0) return R.drawable.medal_bronze
    val accuracy = correctCount * 100f / totalQuestions
    return when {
        correctCount == totalQuestions -> R.drawable.medal_perfect
        accuracy >= 80f -> R.drawable.medal_gold
        accuracy >= 50f -> R.drawable.medal_silver
        else -> R.drawable.medal_bronze
    }
}

private fun lastMedalResId(lastAccuracy: Float): Int = when {
    lastAccuracy >= 100f -> R.drawable.medal_perfect
    lastAccuracy >= 80f -> R.drawable.medal_gold
    lastAccuracy >= 50f -> R.drawable.medal_silver
    else -> R.drawable.medal_bronze
}

private fun medalTitle(correctCount: Int, totalQuestions: Int): String = when {
    correctCount == totalQuestions -> "Perfect！"
    correctCount >= 8 -> "Excellent！"
    correctCount >= 5 -> "Good！"
    else -> "Keep trying！"
}

private fun medalMessage(correctCount: Int, totalQuestions: Int): String = when {
    correctCount == totalQuestions -> "頑張りましたね！おめでとうございます。\nトレーニングをマスターしました！"
    correctCount >= 8 -> "もう少しでパーフェクト！\nこの調子で頑張りましょう！"
    correctCount >= 5 -> "いい調子です！\n焦らず、コツコツ続けることが大事です！努力は嘘をつかないです！"
    else -> "諦めない事が肝心です！\nただひたすらに頑張れば、結果はおのずとついてきます！"
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN).format(Date(millis))

private fun formatSeconds(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val hours = minutes / 60
    val remainMinutes = minutes % 60
    return if (hours > 0) "${hours}時間${remainMinutes}分" else "${minutes}分"
}

private fun formatStudyTime(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    return if (safeSeconds < 60) "${safeSeconds}秒" else formatSeconds(safeSeconds)
}

@Composable
private fun SentenceMenuScreen(
    navController: NavHostController,
    viewModel: CustomSentenceListViewModel = hiltViewModel()
) {
    val sentences by viewModel.sentences.collectAsState()
    BlueScaffold(title = "文章問題", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { navController.navigate(Route.AddSentence) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("文章登録", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { navController.navigate(Route.CustomSentenceList) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, tint = BrightBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("登録一覧", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Button(
                    onClick = { navController.navigate(Route.SentenceQuiz) },
                    enabled = sentences.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (sentences.isEmpty()) "文章を登録してから開始できます"
                        else "文章問題を開始（${sentences.size}文登録済み）",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (sentences.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("登録文章", color = TextMuted, fontSize = 12.sp)
                                Text("${sentences.size}", color = DeepBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                Text("文", color = TextMuted, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("出題可能", color = TextMuted, fontSize = 12.sp)
                                Text("${sentences.size}", color = AccentBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                Text("問", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            item {
                SectionTitle("文章問題について")
            }
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("登録した英文から並べ替え問題を自動生成します", color = TextDark, fontSize = 15.sp)
                        Text("・英文をそのまま入力→4語を自動で空白に", color = TextMuted, fontSize = 13.sp)
                        Text("・[語句]形式で入力→その語句が問題に", color = TextMuted, fontSize = 13.sp)
                        Text("例: I [might][stay][as][well] as join in a tour", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSentenceScreen(
    navController: NavHostController,
    viewModel: AddSentenceViewModel = hiltViewModel()
) {
    val saved by viewModel.saved.collectAsState()
    var sentence by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(saved) {
        if (saved) {
            viewModel.resetSaved()
            sentence = ""
            meaning = ""
        }
    }

    BlueScaffold(title = "文章登録", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(SoftBlue)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("英文", fontWeight = FontWeight.Bold, color = TextMuted)
                            val wordCount = sentence.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                            val bracketCount = "\\[([^\\]]+)\\]".toRegex().findAll(sentence).count()
                            val countLabel = when {
                                bracketCount > 0 -> "[語句] $bracketCount / 4"
                                sentence.isBlank() -> ""
                                else -> "${wordCount}語"
                            }
                            val countColor = when {
                                bracketCount > 0 && bracketCount < 4 -> Danger
                                bracketCount == 4 -> Success
                                wordCount in 1..5 && sentence.isNotBlank() -> Danger
                                wordCount >= 6 -> Success
                                else -> TextMuted
                            }
                            if (countLabel.isNotEmpty()) {
                                Text(countLabel, fontSize = 12.sp, color = countColor, fontWeight = FontWeight.Bold)
                            }
                        }
                        AddWordField(
                            label = "",
                            placeholder = "例: I [might][stay][as][well] as join in a tour",
                            value = sentence,
                            onValueChange = { sentence = it },
                            imeAction = EditorInfo.IME_ACTION_NEXT,
                            autoFocus = true
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("日本語の意味", fontWeight = FontWeight.Bold, color = TextMuted)
                        AddWordField(
                            label = "",
                            placeholder = "例: パッケージツアーに参加するよりも家にいた方がいい",
                            value = meaning,
                            onValueChange = { meaning = it },
                            imeAction = EditorInfo.IME_ACTION_DONE,
                            onImeAction = { viewModel.save(sentence, meaning) }
                        )
                    }
                    val wordCount2 = sentence.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                    val bracketCount2 = "\\[([^\\]]+)\\]".toRegex().findAll(sentence).count()
                    val isValidSentence = sentence.isNotBlank() && (
                        (bracketCount2 == 0 && wordCount2 >= 6) ||
                        bracketCount2 == 4
                    )
                    val validationMsg = when {
                        sentence.isBlank() -> null
                        bracketCount2 > 0 && bracketCount2 < 4 -> "[語句]は4つ必要です（現在${bracketCount2}つ）"
                        bracketCount2 > 4 -> "[語句]は4つまでにしてください"
                        bracketCount2 == 0 && wordCount2 < 6 -> "6語以上の英文が必要です（現在${wordCount2}語）"
                        else -> null
                    }
                    if (validationMsg != null) {
                        Text(validationMsg, color = Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.save(sentence, meaning) },
                        enabled = isValidSentence && meaning.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("登録する", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (saved) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Success),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("登録しました", color = Color.White, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = { navController.navigate(Route.CustomSentenceList) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            Text("一覧へ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("入力形式", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("① そのまま英文を入力（6語以上必要）", color = TextMuted, fontSize = 12.sp)
                    Text("   → 4語をランダムに自動で空白に変換", color = TextMuted, fontSize = 12.sp)
                    Text("② [語句]で語句を囲む（4つ必要）", color = TextMuted, fontSize = 12.sp)
                    Text("   → その語句が並べ替え対象になる", color = TextMuted, fontSize = 12.sp)
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))
                    Text("例② : I [might][stay][as][well] as join in a tour", color = AccentBlue, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun CustomSentenceListScreen(
    navController: NavHostController,
    viewModel: CustomSentenceListViewModel = hiltViewModel()
) {
    val sentences by viewModel.sentences.collectAsState()
    BlueScaffold(title = "登録文章一覧", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Button(
                    onClick = { navController.navigate(Route.AddSentence) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                    Spacer(Modifier.width(4.dp))
                    Text("文章を追加", color = DeepBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (sentences.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentBlue.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text("登録済みの文章はまだありません", color = TextMuted, fontSize = 15.sp)
                            Text("上のボタンから英文を追加してください", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                itemsIndexed(sentences) { idx, s ->
                    SentenceRow(index = idx + 1, sentence = s, onDelete = { viewModel.delete(s.id) })
                }
            }
        }
    }
}

@Composable
private fun SentenceRow(index: Int, sentence: CustomSentenceEntity, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("文章を削除") },
            text = { Text("この文章を削除しますか？") },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text("削除", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("キャンセル") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$index",
                color = AccentBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    sentence.sentence,
                    color = DeepBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    sentence.meaning,
                    color = TextMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dateStr = remember(sentence.addedAt) {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = sentence.addedAt }
                    "${cal.get(java.util.Calendar.YEAR)}/${cal.get(java.util.Calendar.MONTH) + 1}/${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
                }
                Text(dateStr, color = TextMuted.copy(alpha = 0.6f), fontSize = 11.sp)
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "削除", tint = Danger)
            }
        }
    }
}

@Composable
private fun SentenceQuizScreen(
    navController: NavHostController,
    viewModel: SentenceQuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val result by viewModel.result.collectAsState()
    val soundPlayer = rememberSoundPlayer()

    LaunchedEffect(state.isAnswered) {
        if (state.isAnswered) {
            delay(30L)
            if (state.isCorrect == true) soundPlayer.playCorrect() else soundPlayer.playWrong()
        }
    }

    if (state.isFinished && result != null) {
        BlueScaffold(title = "文章問題") { inner ->
            SentenceResultContent(
                result = result!!,
                modifier = Modifier.padding(inner),
                onRetry = {
                    navController.navigate(Route.SentenceQuiz) {
                        popUpTo(Route.SentenceQuiz) { inclusive = true }
                    }
                },
                onHome = { navController.navigate(Route.Home) { popUpTo(Route.Home) { inclusive = true } } },
                onMenu = { navController.navigate(Route.SentenceMenu) { popUpTo(Route.SentenceMenu) { inclusive = true } } }
            )
        }
        return
    }

    BlueScaffold(title = "文章問題", onBack = { navController.popBackStack() }) { inner ->
        when {
            state.questions.isEmpty() && state.startedAt == 0L ->
                Box(Modifier.fillMaxSize().padding(inner).background(SoftBlue), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                        Text("問題を準備中...", color = TextMuted, fontSize = 14.sp)
                    }
                }
            state.questions.isEmpty() ->
                EmptyMessage(
                    modifier = Modifier.padding(inner).background(BrightBlue),
                    title = "出題できる文章がありません\n\n6語以上の英文、または\n[語句]形式で4つの語句を\n含む英文を登録してください",
                    button = "戻る",
                    onClick = { navController.popBackStack() }
                )
            else ->
                SentenceQuizContent(
                    modifier = Modifier.padding(inner),
                    state = state,
                    onSelectChoice = viewModel::selectWord,
                    onUndo = viewModel::undoLastWord,
                    onNext = viewModel::nextQuestion
                )
        }
    }
}

private fun buildAnnotatedSentenceTemplate(template: String, nextSlotIndex: Int = -1) = buildAnnotatedString {
    val markerList = listOf("①", "②", "③", "④")
    val words = template.split(" ")
    var markerCount = 0
    words.forEachIndexed { i, word ->
        if (word in markerList) {
            val isNext = markerCount == nextSlotIndex
            val markerColor = if (isNext) Gold else AccentBlue
            withStyle(SpanStyle(
                color = markerColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                background = if (isNext) Gold.copy(alpha = 0.12f) else Color.Transparent
            )) {
                append(word)
            }
            markerCount++
        } else {
            withStyle(SpanStyle(color = DeepBlue, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                append(word)
            }
        }
        if (i < words.lastIndex) append(" ")
    }
}

@Composable
private fun SentenceQuizContent(
    modifier: Modifier,
    state: SentenceQuizState,
    onSelectChoice: (Int) -> Unit,
    onUndo: () -> Unit,
    onNext: () -> Unit
) {
    val question = state.currentQuestion ?: return
    val selectedIndexSet = state.selectedChoiceIndices.toSet()

    Box(modifier.fillMaxSize().background(SoftBlue)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "問題 ${state.currentIndex + 1} / ${state.questions.size}",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("○ ${state.correctCount}", color = Success, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("✗ ${state.wrongCount}", color = Danger, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1) / state.questions.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = AccentBlue,
                trackColor = Color.White
            )
            Text(
                if (state.isAnswered) "答え合わせ" else "並べ替えて文を完成させよう",
                color = if (state.isAnswered) {
                    if (state.isCorrect == true) Success else Danger
                } else TextDark,
                fontSize = 15.sp,
                fontWeight = if (state.isAnswered) FontWeight.Bold else FontWeight.Normal
            )
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val nextSlot = if (state.isAnswered) -1 else state.selectedWords.size
                    val annotated = buildAnnotatedSentenceTemplate(question.template, nextSlot)
                    Text(
                        annotated,
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    )
                    if (state.isAnswered) {
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
                        val answerText = question.answers.joinToString("  ")
                        val resultLabel = if (state.isCorrect == true) "✓ 正解" else "✗ 不正解"
                        Text(
                            resultLabel,
                            color = if (state.isCorrect == true) Success else Danger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "正解: $answerText",
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(question.meaning, color = TextMuted, fontSize = 14.sp)
                    }
                }
            }
            Text("選択済み", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val markers = listOf("①", "②", "③", "④")
                markers.forEachIndexed { i, marker ->
                    val filledWord = state.selectedWords.getOrNull(i)
                    val isAnsweredCorrect = state.isAnswered && filledWord == question.answers.getOrNull(i)
                    val isAnsweredWrong = state.isAnswered && filledWord != null && !isAnsweredCorrect
                    val bgColor = when {
                        filledWord == null -> Color.White.copy(alpha = 0.5f)
                        isAnsweredCorrect -> Success.copy(alpha = 0.15f)
                        isAnsweredWrong -> Danger.copy(alpha = 0.15f)
                        else -> Color.White
                    }
                    val borderColor = when {
                        isAnsweredCorrect -> Success
                        isAnsweredWrong -> Danger
                        filledWord != null -> AccentBlue.copy(alpha = 0.5f)
                        else -> Color.Transparent
                    }
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                marker,
                                color = if (filledWord != null) AccentBlue else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                filledWord ?: "—",
                                color = if (filledWord != null) DeepBlue else TextMuted,
                                fontSize = 14.sp,
                                fontWeight = if (filledWord != null) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            Text("選択肢", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            val indexedChoices = question.shuffledChoices.mapIndexed { i, w -> i to w }
            indexedChoices.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { (choiceIdx, word) ->
                        val isSelected = selectedIndexSet.contains(choiceIdx)
                        val selectedOrder = state.selectedChoiceIndices.indexOf(choiceIdx)
                        val isCorrectPosition = state.isAnswered && isSelected &&
                            selectedOrder in question.answers.indices &&
                            word == question.answers[selectedOrder]
                        val isWrongPosition = state.isAnswered && isSelected && !isCorrectPosition
                        val containerColor = when {
                            isCorrectPosition -> Success
                            isWrongPosition -> Danger
                            isSelected -> Color.White.copy(alpha = 0.3f)
                            else -> AccentBlue
                        }
                        Button(
                            onClick = { if (!isSelected && !state.isAnswered) onSelectChoice(choiceIdx) },
                            enabled = !isSelected && !state.isAnswered,
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = containerColor,
                                disabledContainerColor = containerColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                word,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (!state.isAnswered) {
                val lastWord = state.selectedWords.lastOrNull()
                OutlinedButton(
                    onClick = onUndo,
                    enabled = lastWord != null,
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (lastWord != null) "「$lastWord」をもどす" else "もどす",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (state.currentIndex >= state.questions.lastIndex) "結果を見る" else "次の問題",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SentenceResultContent(
    result: SentenceQuizResult,
    modifier: Modifier,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    onMenu: () -> Unit
) {
    val isPerfect = result.correctCount == result.totalQuestions
    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }
    var medalVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val animDuration = 1200
        playSynthSound(listOf(Pair(440f, 150), Pair(523f, 150), Pair(659f, 200), Pair(784f, 250)), false)
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
        launch { medalAlpha.animateTo(1f, animationSpec = tween(300)) }
        medalScale.animateTo(1.1f, animationSpec = tween(280))
        medalScale.animateTo(0.95f, animationSpec = tween(100))
        medalScale.animateTo(1f, animationSpec = tween(100))
    }

    Column(modifier.fillMaxSize().background(BrightBlue)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("文章問題 結果", color = TextMuted, fontSize = 15.sp)
                        if (isPerfect && medalVisible) {
                            Text(
                                "🎉 全問正解！",
                                color = Gold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.alpha(medalAlpha.value)
                            )
                        }
                        if (medalVisible) {
                            Text(
                                "$displayedAccuracy%",
                                color = if (isPerfect) Gold else DeepBlue,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.scale(medalScale.value).alpha(medalAlpha.value)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { animProgress.value },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color = Teal,
                            trackColor = Color(0xFFDDE5EC)
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) { idx ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (idx < result.starCount) Gold else TextMuted.copy(alpha = 0.3f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            val rankLabel = when (result.starCount) {
                                3 -> "Excellent!"
                                2 -> "Good!"
                                1 -> "Keep trying!"
                                else -> "Practice more"
                            }
                            Text(rankLabel, color = if (result.starCount >= 2) Success else TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("正解", color = TextMuted, fontSize = 13.sp)
                                Text("${result.correctCount}", color = Success, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("不正解", color = TextMuted, fontSize = 13.sp)
                                Text("${result.wrongCount}", color = Danger, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("問題数", color = TextMuted, fontSize = 13.sp)
                                Text("${result.totalQuestions}", color = TextDark, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        HorizontalDivider(color = TextMuted.copy(alpha = 0.15f))
                        val mins = result.studySeconds / 60
                        val secs = result.studySeconds % 60
                        val timeStr = if (mins > 0) "${mins}分${secs}秒" else "${secs}秒"
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("学習時間: $timeStr", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("もう一度", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("文章問題メニュー", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onHome,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("ホーム", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
