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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.vocabapp.viewmodel.AddWordViewModel
import com.example.vocabapp.viewmodel.CustomWordQuizViewModel
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
            }
            item {
                CardButton(
                    title = "レッスン一覧",
                    subtitle = "600 / 730 / 860 / 990点の単語を学習",
                    icon = Icons.Default.FormatListBulleted,
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
                Button(
                    onClick = { navController.navigate(Route.AddWord) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BrightBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("新規単語登録", color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                    onDetail = { wordId -> navController.navigate(Route.word(wordId)) }
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
    BlueScaffold(title = "結果", onBack = { navController.navigate(Route.Home) }) { inner ->
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
                                IconButton(onClick = { speaker.speak(it.english) }) { Icon(Icons.Default.VolumeUp, contentDescription = "Audio", tint = BrightBlue) }
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
            OutlinedButton(onClick = viewModel::resetProgress, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Danger)
                Spacer(Modifier.width(8.dp))
                Text("学習進捗をリセット", color = Danger)
            }
        }
    }
}

@Composable
private fun CustomWordQuizScreen(navController: NavHostController, viewModel: CustomWordQuizViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    if (state.finishedAttemptId != null) {
        val accuracy = if (state.questions.isNotEmpty()) state.correctCount * 100f / state.questions.size else 0f
        BlueScaffold(title = "カスタム単語クイズ") { inner ->
            Column(
                modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${accuracy.toInt()}%", color = DeepBlue, fontSize = 54.sp, fontWeight = FontWeight.Black)
                        Row {
                            repeat(3) { i ->
                                Icon(Icons.Default.Star, contentDescription = null, tint = if (i < when { accuracy >= 90f -> 3; accuracy >= 70f -> 2; accuracy >= 50f -> 1; else -> 0 }) Gold else Color(0xFFDDE5EC), modifier = Modifier.size(42.dp))
                            }
                        }
                        Text(when { accuracy >= 90f -> "Excellent!"; accuracy >= 70f -> "Good job!"; accuracy >= 50f -> "Nice try!"; else -> "Keep going!" }, color = TextDark, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        Text("正解 ${state.correctCount} / 不正解 ${state.wrongCount} / 全${state.questions.size}問", color = TextMuted)
                    }
                }
                Button(
                    onClick = { navController.navigate(Route.CustomQuiz) { popUpTo(Route.CustomQuiz) { inclusive = true } } },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("再チャレンジ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { navController.navigate(Route.Home) { popUpTo(Route.Home) { inclusive = true } } }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                    Text("ホームへ")
                }
            }
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
private fun AddWordScreen(navController: NavHostController, viewModel: AddWordViewModel = hiltViewModel()) {
    val saved by viewModel.saved.collectAsState()
    var english by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    val meaningFocus = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(saved) {
        if (saved) { viewModel.resetSaved(); navController.popBackStack() }
    }
    BlueScaffold(title = "新規単語登録", onBack = { navController.popBackStack() }) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).background(SoftBlue)
                .verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("英単語", fontWeight = FontWeight.Bold, color = TextMuted)
                        OutlinedTextField(
                            value = english,
                            onValueChange = { english = it },
                            placeholder = { Text("例: apple") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { meaningFocus.requestFocus() })
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("日本語", fontWeight = FontWeight.Bold, color = TextMuted)
                        OutlinedTextField(
                            value = meaning,
                            onValueChange = { meaning = it },
                            placeholder = { Text("例: りんご") },
                            modifier = Modifier.fillMaxWidth().focusRequester(meaningFocus),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                        )
                    }
                    Button(
                        onClick = { keyboardController?.hide(); viewModel.save(english, meaning) },
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
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White) }
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
private fun TrainingCard(training: Training, onQuiz: () -> Unit, onDetail: (Int) -> Unit) {
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
                    Text("${training.wordStartNumber}〜${training.wordEndNumber}語", color = DeepBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
                Text("学習回数  ${training.studyCount}", color = TextMuted)
                Text("学習日  ${training.lastStudiedAt?.let(::formatDate) ?: "-"}", color = TextMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { index ->
                        Icon(Icons.Default.Star, contentDescription = null, tint = if (index < training.bestStarCount) Gold else Color(0xFFDDE5EC), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("${training.bestAccuracy.toInt()}%", color = TextMuted)
                }
            }
            Button(onClick = onQuiz, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue), shape = CircleShape, modifier = Modifier.size(86.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("開始", fontSize = 12.sp)
                }
            }
        }
        Text(
            "先頭単語の詳細を見る",
            modifier = Modifier.fillMaxWidth().clickable { onDetail(training.wordStartNumber) }.padding(bottom = 14.dp),
            textAlign = TextAlign.Center,
            color = BrightBlue,
            fontWeight = FontWeight.Bold
        )
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
                IconButton(onClick = { speaker.speak(question.word.english) }) { Icon(Icons.Default.VolumeUp, contentDescription = "Audio", tint = BrightBlue) }
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
    Column(
        modifier = modifier.fillMaxSize().background(SoftBlue).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${result.accuracy.toInt()}%", color = DeepBlue, fontSize = 54.sp, fontWeight = FontWeight.Black)
                Row {
                    repeat(3) { index -> Icon(Icons.Default.Star, contentDescription = null, tint = if (index < result.starCount) Gold else Color(0xFFDDE5EC), modifier = Modifier.size(42.dp)) }
                }
                Text(result.message(), color = TextDark, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("正解 ${result.correctCount} / 不正解 ${result.wrongCount} / 全${result.totalQuestions}問", color = TextMuted)
                Text("今回の学習時間 ${formatSeconds(result.studySeconds)}", color = TextMuted)
            }
        }
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(58.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("再チャレンジ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onHome, modifier = Modifier.weight(1f).height(54.dp)) { Text("ホーム") }
            OutlinedButton(onClick = onNext, modifier = Modifier.weight(1f).height(54.dp)) { Text("次へ") }
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

private fun QuizResult.message(): String = when {
    accuracy >= 90f -> "Excellent!"
    accuracy >= 70f -> "Good job!"
    accuracy >= 50f -> "Nice try!"
    else -> "Keep going!"
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN).format(Date(millis))

private fun formatSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val hours = minutes / 60
    val remainMinutes = minutes % 60
    return if (hours > 0) "${hours}時間${remainMinutes}分" else "${seconds.coerceAtLeast(0) / 60}分"
}
