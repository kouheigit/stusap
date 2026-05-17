package com.example.vocabapp

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
import java.util.zip.ZipInputStream
import org.xmlpull.v1.XmlPullParser

internal val XLSX_TARGET_ENTRIES = setOf(
    "xl/sharedStrings.xml",
    "xl/worksheets/sheet1.xml",
    "xl/_rels/workbook.xml.rels",
    "xl/workbook.xml"
)

internal fun parseXlsxRows(bytes: ByteArray): List<List<String>> {
    debugImportLog("parseXlsxRows: start, bytes=${bytes.size}")
    if (bytes.size < 4 || !bytes.startsWith(byteArrayOf(0x50, 0x4B, 0x03, 0x04))) {
        errorImportLog("parseXlsxRows: not a valid ZIP/XLSX file (magic bytes mismatch)")
        error("選択されたファイルは有効なExcelファイル(.xlsx)ではありません。ファイル形式を確認してください。")
    }
    val entries = mutableMapOf<String, ByteArray>()
    try {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name
                debugImportLog("parseXlsxRows: ZIP entry name=$name isDir=${entry.isDirectory}")
                var entryBytes: ByteArray? = null
                if (!entry.isDirectory && name in XLSX_TARGET_ENTRIES) {
                    try {
                        entryBytes = zip.readBytesWithLimit(MAX_XLSX_ENTRY_BYTES)
                    } catch (e: Exception) {
                        warnImportLog("parseXlsxRows: readBytes error on '$name': ${e.javaClass.simpleName}: ${e.message}")
                    }
                }
                if (entryBytes != null) {
                    entries[name] = entryBytes
                    debugImportLog("parseXlsxRows: captured $name (${entryBytes.size} bytes)")
                }
                try {
                    zip.closeEntry()
                } catch (e: java.util.zip.ZipException) {
                    warnImportLog("parseXlsxRows: closeEntry ZipException on '$name': ${e.message} (ignoring CRC issue)")
                } catch (e: Exception) {
                    warnImportLog("parseXlsxRows: closeEntry error on '$name': ${e.javaClass.simpleName}: ${e.message}")
                }
                entry = try { zip.nextEntry } catch (e: java.util.zip.ZipException) {
                    warnImportLog("parseXlsxRows: ZipException advancing to next entry: ${e.message}")
                    break
                }
            }
        }
    } catch (e: java.util.zip.ZipException) {
        errorImportLog("parseXlsxRows: fatal ZipException reading XLSX ZIP: ${e.message}")
        if (entries.isEmpty()) error("ZIPファイルの読み込みに失敗しました: ${e.message}")
    }
    debugImportLog("parseXlsxRows: captured entries=${entries.keys}")

    val sheetPath = parseWorkbookSheetPath(entries["xl/_rels/workbook.xml.rels"])
        ?: "xl/worksheets/sheet1.xml"
    debugImportLog("parseXlsxRows: resolved sheetPath=$sheetPath")

    if (!entries.containsKey(sheetPath) && sheetPath != "xl/worksheets/sheet1.xml") {
        debugImportLog("parseXlsxRows: sheet at $sheetPath not captured, re-scanning ZIP")
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null && !entries.containsKey(sheetPath)) {
                    if (!entry.isDirectory && entry.name == sheetPath) {
                        entries[sheetPath] = zip.readBytesWithLimit(MAX_XLSX_ENTRY_BYTES)
                        debugImportLog("parseXlsxRows: re-scan captured $sheetPath")
                    }
                    try { zip.closeEntry() } catch (e: java.util.zip.ZipException) {
                        warnImportLog("parseXlsxRows: re-scan closeEntry error: ${e.message}")
                    }
                    entry = try { zip.nextEntry } catch (e: java.util.zip.ZipException) { break }
                }
            }
        } catch (e: Exception) {
            warnImportLog("parseXlsxRows: re-scan failed: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    val sheet = entries[sheetPath]
        ?: error("Excelファイルの1枚目のシートを読み込めませんでした (パス: $sheetPath, 取得済みエントリ: ${entries.keys})")
    val sharedStrings = entries["xl/sharedStrings.xml"]?.let(::parseSharedStrings).orEmpty()
    debugImportLog("parseXlsxRows: sharedStrings.size=${sharedStrings.size}")
    return parseWorksheetRows(sheet, sharedStrings)
}

internal fun parseSharedStrings(bytes: ByteArray): List<String> {
    debugImportLog("parseSharedStrings: parsing ${bytes.size} bytes")
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
        errorImportLog("parseSharedStrings: parse error after ${values.size} entries: ${e.javaClass.simpleName}: ${e.message}")
    }
    debugImportLog("parseSharedStrings: parsed ${values.size} shared strings")
    return values
}

internal fun parseWorksheetRows(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
    debugImportLog("parseWorksheetRows: parsing ${bytes.size} bytes, sharedStrings=${sharedStrings.size}")
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
        errorImportLog("parseWorksheetRows: parse error after ${rows.size} rows: ${e.javaClass.simpleName}: ${e.message}")
    }
    debugImportLog("parseWorksheetRows: parsed ${rows.size} rows")
    return rows
}

internal fun resolveXlsxCellValue(rawValue: String, type: String, sharedStrings: List<String>): String {
    val resolved = when (type) {
        "s" -> {
            val idx = rawValue.toIntOrNull()
            if (idx == null) {
                warnImportLog("resolveXlsxCellValue: shared string index not an int: '$rawValue'")
                ""
            } else {
                sharedStrings.getOrNull(idx).also {
                    if (it == null) warnImportLog("resolveXlsxCellValue: shared string index $idx out of range (size=${sharedStrings.size})")
                }.orEmpty()
            }
        }
        "b" -> if (rawValue == "1") "TRUE" else "FALSE"
        "e" -> ""
        else -> rawValue
    }.trim()
    return resolved
}

internal fun xlsxColumnIndex(reference: String): Int {
    if (reference.isBlank()) {
        warnImportLog("xlsxColumnIndex: empty cell reference, defaulting to column 0")
        return 0
    }
    var result = 0
    val letters = reference.takeWhile { it.isLetter() }.uppercase(Locale.ROOT)
    if (letters.isEmpty()) {
        warnImportLog("xlsxColumnIndex: no letter prefix in reference '$reference', defaulting to column 0")
        return 0
    }
    letters.forEach { char ->
        result = result * 26 + (char - 'A' + 1)
    }
    return maxOf(result - 1, 0)
}

internal fun List<List<String>>.toCsvText(): String {
    debugImportLog("toCsvText: converting ${size} rows to CSV")
    return joinToString("\n") { row ->
        row.joinToString(",") { cell ->
            val normalized = cell.replace("\r\n", " ").replace("\r", " ").replace("\n", " ")
            val escaped = normalized.replace("\"", "\"\"")
            if (escaped.any { it == ',' || it == '"' }) "\"$escaped\"" else escaped
        }
    }.also { debugImportLog("toCsvText: result length=${it.length}") }
}

@Composable
internal fun WordImportScreen(navController: NavHostController, viewModel: WordImportViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val preview by viewModel.preview.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.showLoading()
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val result = runCatching { context.readImportFileAsCsv(uri) }
                result.onSuccess { csvText ->
                    viewModel.loadCsv(csvText)
                }.onFailure { error ->
                    errorImportLog("File read error: ${error.javaClass.simpleName}: ${error.message}", error)
                    viewModel.showMessage("ファイルの読み込みに失敗しました。形式とサイズを確認してください。")
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
internal fun ImportSummaryCard(title: String, totalRows: Int, newCount: Int, newIdiomCount: Int, duplicateCount: Int, errorCount: Int) {
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
internal fun SummaryChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(SoftBlue, RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun ImportWordRow(english: String, meaning: String, type: String) {
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
