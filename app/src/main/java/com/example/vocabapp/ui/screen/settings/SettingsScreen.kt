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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.example.vocabapp.viewmodel.SettingsViewModel
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


@Composable
internal fun SettingsScreen(navController: NavHostController, viewModel: SettingsViewModel = hiltViewModel()) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteCustomWordsDialog by remember { mutableStateOf(false) }
    var showDeleteSentenceDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("学習進捗をリセット") },
            text = {
                Text(
                    "すべての学習進捗・クイズ履歴・復習リストを削除します。\n" +
                        "登録したカスタム単語・熟語・文章も削除されます。\n" +
                        "この操作は元に戻せません。"
                )
            },
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

    if (showDeleteCustomWordsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCustomWordsDialog = false },
            title = { Text("カスタム単語・熟語を全削除") },
            text = { Text("登録したすべてのカスタム単語・熟語を削除します。\nこの操作は元に戻せません。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllCustomWordsAndIdioms()
                        showDeleteCustomWordsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text("削除") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteCustomWordsDialog = false }) { Text("キャンセル") }
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
                    Text("バージョン ${BuildConfig.VERSION_NAME}", color = TextMuted, fontSize = 13.sp)
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
                        onClick = { showDeleteCustomWordsDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        border = null
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text("カスタム単語・熟語を全削除", color = Danger, modifier = Modifier.weight(1f))
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
