package com.example.vocabapp

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.vocabapp.domain.model.Word
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.viewmodel.AddWordViewModel
import com.example.vocabapp.viewmodel.CustomWordListViewModel
import com.example.vocabapp.viewmodel.CustomWordQuizViewModel
import com.example.vocabapp.viewmodel.FlashcardViewModel
import com.example.vocabapp.viewmodel.LessonListViewModel
import com.example.vocabapp.viewmodel.MainViewModel
import com.example.vocabapp.viewmodel.QuizViewModel
import com.example.vocabapp.viewmodel.ResultViewModel
import com.example.vocabapp.viewmodel.ReviewViewModel
import com.example.vocabapp.viewmodel.StudyLogViewModel
import com.example.vocabapp.viewmodel.TrainingListViewModel
import com.example.vocabapp.viewmodel.WordDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
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
import java.util.concurrent.ConcurrentHashMap

private fun playSynthSound(segments: List<Pair<Float, Int>>, squareWave: Boolean) {
    Thread {
        try {
            val sampleRate = 44100
            val totalSamples = segments.sumOf { (_, ms) -> sampleRate * ms / 1000 }
            val buffer = ShortArray(totalSamples)
            var pos = 0
            for ((freq, durationMs) in segments) {
                val numSamples = sampleRate * durationMs / 1000
                for (i in 0 until numSamples) {
                    if (freq == 0f) { buffer[pos++] = 0; continue }
                    val envelope = when {
                        i < numSamples * 0.05 -> i / (numSamples * 0.05)
                        i > numSamples * 0.75 -> (numSamples - i).toDouble() / (numSamples * 0.25)
                        else -> 1.0
                    }
                    val wave = kotlin.math.sin(2 * Math.PI * freq * i / sampleRate)
                    val shaped = if (squareWave) (if (wave > 0) 1.0 else -1.0) else wave
                    buffer[pos++] = (shaped * Short.MAX_VALUE * 0.85 * envelope).toInt().toShort()
                }
            }
            val audioTrack = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
                buffer.size * 2,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(totalSamples * 1000L / sampleRate + 150)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {}
    }.start()
}

private data class SoundPlayer(
    val playCorrect: () -> Unit,
    val playWrong: () -> Unit
)

@Composable
private fun rememberSoundPlayer(): SoundPlayer = remember {
    SoundPlayer(
        playCorrect = {
            // ピンポン: E5(659Hz) → A5(880Hz) の二音上昇チャイム
            playSynthSound(listOf(659f to 160, 880f to 280), squareWave = false)
        },
        playWrong = {
            // ブッブー: 低音バズ×2 (200ms 間隔)
            playSynthSound(listOf(200f to 190, 0f to 70, 160f to 230), squareWave = true)
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent { VocabTheme { AppNav() } }
    }
}

private object Route {
    const val Home = "home"
    const val Lessons = "lessons"
    const val Training = "training/{lessonId}"
    const val Quiz = "quiz?trainingId={trainingId}&isReview={isReview}"
    const val Result = "result/{attemptId}"
    const val Review = "review"
    const val WordDetail = "word/{wordId}"
    const val StudyLog = "study-log"
    const val Settings = "settings"
    const val AddWord = "add-word"
    const val CustomQuiz = "custom-quiz"
    const val CustomWordList = "custom-word-list"
    const val Flashcard = "flashcard/{trainingId}"
    const val Test = "test"

    fun flashcard(trainingId: Int) = "flashcard/$trainingId"

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
        composable(Route.CustomQuiz) { CustomWordQuizScreen(navController) }
        composable(Route.CustomWordList) { CustomWordListScreen(navController) }
        composable(
            Route.Flashcard,
            arguments = listOf(navArgument("trainingId") { type = NavType.IntType })
        ) { FlashcardScreen(navController) }
        composable(Route.Test) { TestInputScreen(navController) }
    }
}

@Composable
private fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isReady by remember { mutableStateOf(false) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val pendingAudioFiles = remember { ConcurrentHashMap<String, java.io.File>() }
    DisposableEffect(context) {
        val instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(0.92f)
                isReady = true
            }
        }
        instance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                val file = pendingAudioFiles.remove(utteranceId) ?: return
                mainHandler.post {
                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0)
                    runCatching {
                        MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            setDataSource(file.absolutePath)
                            setOnCompletionListener { player ->
                                player.release()
                                file.delete()
                            }
                            setOnErrorListener { player, _, _ ->
                                player.release()
                                file.delete()
                                true
                            }
                            prepare()
                            setVolume(1.0f, 1.0f)
                            start()
                        }
                    }.onFailure {
                        file.delete()
                    }
                }
            }

            @Deprecated("Deprecated by Android SDK")
            override fun onError(utteranceId: String?) {
                pendingAudioFiles.remove(utteranceId)?.delete()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                pendingAudioFiles.remove(utteranceId)?.delete()
            }
        })
        tts = instance
        onDispose {
            isReady = false
            pendingAudioFiles.values.forEach { it.delete() }
            pendingAudioFiles.clear()
            instance.stop()
            instance.shutdown()
        }
    }
    val speak: (String) -> Unit = { text ->
        val engine = tts
        if (engine != null && isReady) {
            val utteranceId = "word-${System.nanoTime()}"
            val file = java.io.File(context.cacheDir, "$utteranceId.wav")
            pendingAudioFiles[utteranceId] = file
            val result = engine.synthesizeToFile(text, null, file, utteranceId)
            if (result == TextToSpeech.ERROR) {
                pendingAudioFiles.remove(utteranceId)?.delete()
                val params = android.os.Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                }
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            }
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
                    StatCard("Master", "${summary.masteredLessons}/${summary.totalLessons}", Modifier.weight(1f))
                    StatCard("復習単語", "${summary.reviewCount}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("連続学習", "${summary.streakDays}日", Modifier.weight(1f))
                }
            }
            item {
                CardButton(
                    title = "レッスン一覧",
                    subtitle = "600 / 730 / 860 / 990点の単語を学習",
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onClick = { navController.navigate(Route.Lessons) }
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
            item {
                Button(
                    onClick = { navController.navigate(Route.Test) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("テスト", color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LessonListScreen(navController: NavHostController, viewModel: LessonListViewModel = hiltViewModel()) {
    val lessons by viewModel.lessons.collectAsState()
    BlueScaffold(title = "レッスン一覧", onBack = { navController.popBackStack() }) { inner ->
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
                    onClick = { navController.navigate(Route.CustomQuiz) },
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
private fun TrainingListScreen(navController: NavHostController, viewModel: TrainingListViewModel = hiltViewModel()) {
    val trainings by viewModel.trainings.collectAsState()
    BlueScaffold(title = "トレーニング一覧", onBack = { navController.popBackStack() }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(BrightBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                    Text("TOEIC向け英単語を、10問単位の4択クイズで学習するローカル保存型アプリです。", color = TextMuted)
                }
            }
            OutlinedButton(onClick = { showDeleteCustomDialog = true }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Danger)
                Spacer(Modifier.width(8.dp))
                Text("カスタム単語を全削除", color = Danger)
            }
            OutlinedButton(onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Danger)
                Spacer(Modifier.width(8.dp))
                Text("学習進捗をリセット", color = Danger)
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
    val resId = medalResId(correctCount, total)
    val title = medalTitle(correctCount, total)

    val animProgress = remember { Animatable(0f) }
    var displayedAccuracy by remember { mutableStateOf(0) }
    var medalVisible by remember { mutableStateOf(false) }
    val medalScale = remember { Animatable(0f) }
    val medalAlpha = remember { Animatable(0f) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val animDuration = 1800
        playSynthSound(
            listOf(Pair(440f, 150), Pair(523f, 150), Pair(659f, 200), Pair(784f, 250), Pair(1047f, 350)),
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
            val mp = MediaPlayer.create(context, R.raw.medal_sound)
            mp?.setVolume(0.6f, 0.6f)
            mp?.setOnCompletionListener { it.release() }
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
                if (medalVisible) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        modifier = Modifier.size(160.dp)
                            .scale(medalScale.value)
                            .alpha(medalAlpha.value),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Spacer(Modifier.size(160.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("$correctCount/${total}正解", color = TextMuted, fontSize = 16.sp)
                    Text("$displayedAccuracy%", color = DeepBlue, fontSize = 52.sp, fontWeight = FontWeight.Black, lineHeight = 58.sp)
                }
            }
            LinearProgressIndicator(
                progress = { animProgress.value },
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp).height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = Teal, trackColor = Color(0xFFDDE5EC)
            )
        }
        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = DeepBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
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
    BlueScaffold(title = "単語帳", onBack = { navController.popBackStack() }) { inner ->
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
                items(words, key = { it.id }) { word ->
                    CustomWordRow(word = word, onDelete = { viewModel.delete(word.id) })
                }
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

@Composable
private fun TestInputScreen(navController: NavHostController) {
    var japanese by rememberSaveable { mutableStateOf("") }
    var english by rememberSaveable { mutableStateOf("") }
    var englishInput by remember { mutableStateOf<EditText?>(null) }
    BlueScaffold(title = "テスト入力", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(SoftBlue)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    AddWordField(
                        label = "日本語",
                        placeholder = "例: りんご、美しい",
                        value = japanese,
                        onValueChange = { japanese = it },
                        imeAction = EditorInfo.IME_ACTION_NEXT,
                        autoFocus = true,
                        onImeAction = { englishInput?.focusAndShowKeyboard() },
                    )
                    AddWordField(
                        label = "英単語",
                        placeholder = "例: apple, beautiful",
                        value = english,
                        onValueChange = { english = it },
                        imeAction = EditorInfo.IME_ACTION_DONE,
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
                        onReady = { englishInput = it },
                    )
                }
            }
            if (japanese.isNotBlank() || english.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("入力内容", fontWeight = FontWeight.Bold, color = TextMuted)
                        if (japanese.isNotBlank()) Text("日本語: $japanese", color = TextDark, fontSize = 18.sp)
                        if (english.isNotBlank()) Text("英単語: $english", color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
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

@Composable
private fun AddWordScreen(navController: NavHostController, viewModel: AddWordViewModel = hiltViewModel()) {
    val saved by viewModel.saved.collectAsState()
    var english by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }
    var meaningInput by remember { mutableStateOf<EditText?>(null) }
    LaunchedEffect(saved) {
        if (saved) { viewModel.resetSaved(); navController.popBackStack() }
    }
    BlueScaffold(title = "新規単語登録", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AddWordCardPadding), verticalArrangement = Arrangement.spacedBy(AddWordCardSpacing)) {
                    AddWordField(
                        label = "英単語",
                        placeholder = "例: apple, beautiful",
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
                Image(
                    painter = painterResource(bestMedalResId(training.bestAccuracy)),
                    contentDescription = null,
                    modifier = Modifier.size(112.dp).clickable { onQuiz() },
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
                modifier = Modifier.weight(1f).clickable { onDetail(training.wordStartNumber) }.padding(bottom = 14.dp),
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
        if (speaker.isReady) {
            speaker.speak(question.word.english)
        }
    }
    LaunchedEffect(state.isAnswered, state.currentIndex) {
        if (state.isAnswered) {
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

    LaunchedEffect(Unit) {
        val animDuration = 1800
        if (!isPerfect) {
            playSynthSound(
                listOf(Pair(440f, 150), Pair(523f, 150), Pair(659f, 200), Pair(784f, 250), Pair(1047f, 350)),
                false
            )
        }
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
        try {
            val mp = MediaPlayer.create(context, R.raw.medal_sound)
            mp?.setVolume(0.6f, 0.6f)
            mp?.setOnCompletionListener { it.release() }
            mp?.start()
        } catch (_: Exception) {}
        launch { medalAlpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing)) }
        medalScale.animateTo(1.15f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        medalScale.animateTo(0.95f, animationSpec = tween(120))
        medalScale.animateTo(1f, animationSpec = tween(100))
    }

    val perfectPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(Unit) {
        if (isPerfect) {
            try {
                val mp = MediaPlayer.create(context, R.raw.perfect_native_male)
                mp?.setOnCompletionListener { it.release(); perfectPlayer.value = null }
                mp?.start()
                perfectPlayer.value = mp
            } catch (_: Exception) {}
        }
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
                        if (medalVisible) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = if (isPerfect) "パーフェクトメダル" else null,
                                modifier = Modifier.size(medalSize)
                                    .scale(medalScale.value)
                                    .alpha(medalAlpha.value),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Spacer(Modifier.size(medalSize))
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
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp).height(10.dp).clip(RoundedCornerShape(5.dp)),
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

private fun medalResId(correctCount: Int, totalQuestions: Int): Int = when {
    correctCount == totalQuestions -> R.drawable.medal_perfect
    correctCount >= 8 -> R.drawable.medal_gold
    correctCount >= 5 -> R.drawable.medal_silver
    else -> R.drawable.medal_bronze
}

private fun bestMedalResId(bestAccuracy: Float): Int = when {
    bestAccuracy >= 100f -> R.drawable.medal_perfect
    bestAccuracy >= 80f -> R.drawable.medal_gold
    bestAccuracy >= 50f -> R.drawable.medal_silver
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
