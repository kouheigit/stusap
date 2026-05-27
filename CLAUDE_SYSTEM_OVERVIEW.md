# システム概要ドキュメント

このドキュメントは、Claude など別の AI/開発者にこの Android アプリの全体像を説明するためのものです。
使っている技術、アーキテクチャ、主要機能、データ構造、画面遷移、保守上の注意点をまとめています。

## 1. このシステムは何か

このプロジェクトは、TEPPEN 風の英単語学習を目的としたネイティブ Android アプリです。

ユーザーは Android 端末上で、英単語・熟語・例文を学習し、クイズ、復習、成績確認、カスタム単語登録、CSV/XLSX インポートなどを行えます。
基本方針はローカルファーストで、現時点ではバックエンド API に依存しない構成です。

アプリの主な目的は次の通りです。

- 英単語・熟語をレッスン単位で学習する
- 4択クイズで理解度を確認する
- 間違えた単語を復習リストへ回す
- 学習履歴、正答率、星評価、進捗を端末内に保存する
- ユーザー独自の単語・熟語・文章を追加して学習できるようにする
- CSV/XLSX から単語データを取り込む
- 文章並び替えクイズやフラッシュカードで学習方法を増やす

## 2. 使用技術

### Android / Kotlin

- Kotlin
- Android ネイティブアプリ
- Gradle Kotlin DSL
- Java 17 / Kotlin JVM target 17
- minSdk 26
- targetSdk 36
- compileSdk 36
- applicationId: `com.example.vocabapp`
- namespace: `com.example.vocabapp`

### UI

- Jetpack Compose
- Material 3
- Material Icons Extended
- Activity Compose
- Navigation Compose
- Lifecycle Runtime Compose
- ViewModel Compose

Compose UI は画面単位で分割され、`ui/screen/` 配下に Home、Lesson、Training、Quiz、Review、Word、Settings、Custom、Flashcard などの画面が配置されています。

### アーキテクチャ / DI

- MVVM
- Hilt
- StateFlow / Flow
- Kotlin Coroutines
- UseCase 層
- Repository 層

基本的な依存方向は以下です。

```text
Compose UI
  ↓
ViewModel
  ↓
UseCase
  ↓
Repository
  ↓
Room DAO
  ↓
Room Database
```

一部の UseCase は Repository への薄い委譲ですが、画面側からデータ層を直接触らないための境界として存在しています。

### ローカル永続化

- Room
- SQLCipher for Android
- Android Keystore
- Room Migration
- Flow ベースの DAO
- 端末内暗号化 SQLite DB
- DB 名: `toeic_vocab.db`

Room の schema export は有効です。

```kotlin
@Database(
    version = 10,
    exportSchema = true
)
```

バックアップ設定は Manifest 上で `android:allowBackup="false"` になっています。

DB は SQLCipher for Android を通して暗号化して開きます。暗号化パスフレーズはランダム生成し、
Android Keystore の AES-GCM 鍵で暗号化したうえでアプリ private SharedPreferences に保存します。
既存インストールに平文の `toeic_vocab.db` が存在する場合は、Room を開く前に SQLCipher 形式へ移行します。

学習履歴、復習状態、クイズ結果、カスタム単語・熟語・文章は、氏名や認証トークンほど高リスクではありませんが、
ユーザーの学習傾向や入力内容を含むプライバシーデータとして扱います。バックアップ除外設定は維持し、
DB、SharedPreferences、files、root domain は backup rules / data extraction rules で除外します。

### インポート

- CSV import
- XLSX import
- Android のファイル選択
- OpenDocument 系のファイル取得
- ローカルパース

外部サーバーへアップロードせず、端末上でファイルを読み取り、プレビュー後に Room DB へ保存する流れです。

### テスト

- JUnit
- AndroidX Test
- Espresso
- Compose UI Test

現在確認できるテストの中心は、CSV/XLSX パーサー、Repository サポート処理、クイズスコア計算、MainActivity のスモークテストです。

## 3. プロジェクト構成

主要なディレクトリ構成は以下です。

```text
app/src/main/java/com/example/vocabapp/
├── MainActivity.kt
├── app/
│   └── VocabApplication.kt
├── data/
│   ├── import/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   └── entity/
│   ├── repository/
│   └── seed/
├── di/
│   └── AppModule.kt
├── domain/
│   ├── model/
│   └── usecase/
├── ui/
│   ├── audio/
│   ├── navigation/
│   ├── screen/
│   ├── state/
│   └── theme/
├── util/
└── viewmodel/
```

### `MainActivity.kt`

アプリのエントリーポイントです。

主な責務は次の通りです。

- `ComponentActivity` の定義
- Hilt エントリーポイント
- Edge-to-edge 設定
- `VocabTheme` の適用
- `NavHost` の構築
- 各画面 Composable の呼び出し

現在の `MainActivity.kt` は、以前のように大量の画面 UI を直接持つ構成ではなく、ナビゲーション定義中心の薄いファイルになっています。

### `app/VocabApplication.kt`

Hilt を有効化する Application クラスです。

### `di/AppModule.kt`

Hilt の DI モジュールです。

現在は主に Room Database と `AppDao` を提供しています。
Repository や UseCase は `@Inject constructor` により解決される構成です。

### `ui/`

Compose UI 関連です。

```text
ui/
├── audio/
├── navigation/
├── screen/
├── state/
└── theme/
```

`ui/screen/` 配下には画面単位の Composable が置かれています。

```text
ui/screen/
├── common/
├── custom/
├── flashcard/
├── home/
├── lesson/
├── quiz/
├── review/
├── settings/
├── training/
└── word/
```

`common/` には、クイズ共通 UI、結果画面部品、入力部品、音声/TTS 周辺の共通部品が置かれています。

### `viewmodel/`

画面または機能ごとの ViewModel です。

例:

- `MainViewModel`
- `LessonViewModel`
- `TrainingViewModel`
- `QuizViewModel`
- `ResultViewModel`
- `ReviewViewModel`
- `WordDetailViewModel`
- `SettingsViewModel`
- `FlashcardViewModel`
- `SentenceQuizViewModel`
- `CustomWordViewModel`
- `CustomIdiomViewModel`
- `CustomTrainingViewModel`
- `RandomCustomQuizViewModel`

クイズ系では `QuizSession` がタイマー、回答、次問題、終了などのセッション状態を扱います。

### `domain/`

アプリ内で使うドメインモデルと UseCase です。

```text
domain/
├── model/
└── usecase/
    ├── custom/
    ├── lesson/
    ├── quiz/
    ├── review/
    ├── settings/
    └── word/
```

`Models.kt` には以下のような主要モデルがあります。

- `Lesson`
- `Training`
- `Word`
- `WordChoice`
- `WordRelation`
- `QuizQuestion`
- `AnswerRecord`
- `QuizState`
- `QuizResult`
- `HomeSummary`
- `ImportedWord`
- `WordImportPreview`
- `WordImportResult`
- `SentenceQuestion`
- `SentenceQuizState`
- `SentenceQuizResult`

### `data/local/`

Room の Database、DAO、Entity です。

```text
data/local/
├── AppDatabase.kt
├── dao/
└── entity/
```

Entity は機能ごとに分割されています。

主な Entity:

- `WordEntity`
- `WordChoiceEntity`
- `WordRelationEntity`
- `LessonEntity`
- `TrainingEntity`
- `QuizAttemptEntity`
- `QuizAttemptAnswerEntity`
- `ReviewWordEntity`
- `StudyLogEntity`
- `UserProgressEntity`
- `CustomWordEntity`
- `CustomIdiomEntity`
- `CustomSentenceEntity`
- `AppSettingsEntity`

DAO も機能ごとに分割されています。

主な DAO:

- `WordDao`
- `LessonDao`
- `TrainingDao`
- `QuizDao`
- `ReviewDao`
- `StudyLogDao`
- `UserProgressDao`
- `CustomContentDao`
- `AppSettingsDao`

### `data/repository/`

Room DAO と UseCase/ViewModel の間をつなぐデータ操作層です。

主な Repository:

- `WordRepository`
- `LessonRepository`
- `TrainingRepository`
- `QuizRepository`
- `ReviewRepository`
- `CustomContentRepository`
- `SettingsRepository`
- `SeedRepository`

また、補助的に以下があります。

- `EntityMappers`
- `RepositorySupport`
- `QuizRuntime`

Repository は Room Entity と domain model の変換、複数 DAO をまたぐ処理、クイズ生成、インポート処理などを担当します。

### `data/import/`

CSV/XLSX の読み取りとパースを担当します。

主なファイル:

- `CsvImportParser.kt`
- `CsvRowParser.kt`
- `ImportFileReader.kt`
- `ImportLimits.kt`
- `XlsxImportParser.kt`
- `XlsxWorksheetParser.kt`

役割:

- ファイルのバイト列を読み取る
- CSV 行を解釈する
- XLSX の worksheet を解釈する
- インポート件数やプレビュー件数を制限する
- 不正行や重複行を検出する

### `data/seed/`

初期単語・熟語データを保持します。

主なファイル:

- `SeedData.kt`
- `IdiomSeedData.kt`

初回起動時に `SeedRepository` 経由で Room に登録されます。

## 4. 画面構成

このアプリは Navigation Compose で画面遷移を管理しています。
ルートは `Route` sealed class に集約されています。

主な画面:

- Home
- Lesson list
- Idiom lesson list
- Training list
- Quiz
- Result
- Review
- Word detail
- Study log
- Settings
- Add word
- Word import
- Custom word list
- Add idiom
- Custom idiom list
- Custom training list
- Custom training block
- Custom training quiz
- Random custom menu
- Random custom quiz
- Flashcard
- Sentence menu
- Add sentence
- Custom sentence list
- Sentence quiz

## 5. ナビゲーション構成

`Route` は sealed class です。

静的ルート例:

```kotlin
object Home : Route("home")
object Lessons : Route("lessons")
object IdiomLessons : Route("idiom-lessons")
object Review : Route("review")
object Settings : Route("settings")
object WordImport : Route("word-import")
```

引数付きルート例:

```kotlin
data class Training(val lessonId: String) : Route("training/$lessonId")
data class Quiz(val trainingId: String?, val isReview: Boolean) : Route(...)
data class Result(val attemptId: String) : Route("result/$attemptId")
data class WordDetail(val wordId: String) : Route("word/$wordId")
data class Flashcard(val trainingId: String) : Route("flashcard/$trainingId")
```

`Route.training(...)` や `Route.quiz(...)` のようなヘルパーを通して、画面側から文字列を直接組み立てないようにしています。

## 6. データベース構成

Room Database は `AppDatabase.kt` で定義されています。

DB version は 10 です。
Migration は `MIGRATION_1_2` から `MIGRATION_9_10` まで定義されています。

主な保存対象:

- レッスン
- トレーニング
- 単語
- 単語の選択肢
- 関連語
- クイズ実施履歴
- クイズ回答履歴
- 復習対象単語
- 学習ログ
- ユーザー進捗
- カスタム単語
- カスタム熟語
- カスタム文章
- アプリ設定

Room の役割は単なる保存だけではなく、以下のような学習状態の永続化も担います。

- お気に入り
- 学習済み
- 正答率
- 星評価
- 学習回数
- 最終学習日時
- 復習対象
- カスタムデータ
- 設定値

## 7. 主要機能の処理フロー

### 7.1 アプリ起動

```text
MainActivity
  ↓
VocabTheme
  ↓
AppNav
  ↓
MainViewModel
  ↓
SeedRepository
  ↓
Room Database
```

起動時に Hilt が依存関係を解決し、Room Database を構築します。
初期データが未投入であれば seed データが登録されます。

### 7.2 ホーム表示

```text
HomeScreen
  ↓
MainViewModel / Home summary 系 UseCase
  ↓
LessonRepository
  ↓
Room DAO
```

ホームでは総学習時間、週間学習時間、レッスン進捗、復習数、連続学習日数などを表示します。

### 7.3 レッスン学習

```text
LessonListScreen
  ↓
LessonViewModel
  ↓
GetLessonsUseCase
  ↓
LessonRepository
  ↓
Room
```

レッスン一覧からトレーニング一覧へ遷移し、選択したトレーニングの範囲でクイズやフラッシュカードを開始します。

### 7.4 クイズ

```text
QuizScreen
  ↓
QuizViewModel
  ↓
BuildQuizUseCase
  ↓
QuizRepository
  ↓
Room DAO
```

クイズ開始時に対象 trainingId または review フラグから問題を生成します。
`QuizSession` が現在問題、選択肢、正誤、タイマー、回答履歴を管理します。

回答完了後は以下の流れで結果が保存されます。

```text
QuizSession
  ↓
FinishQuizUseCase
  ↓
QuizRepository.finishQuiz()
  ↓
quiz_attempts / quiz_attempt_answers / study_logs / progress / review_words
```

### 7.5 結果画面

```text
ResultScreen
  ↓
ResultViewModel
  ↓
GetResultUseCase
  ↓
QuizRepository
  ↓
Room
```

結果画面では以下を表示します。

- 正答数
- 誤答数
- 正答率
- 星評価
- 学習時間
- 間違えた単語
- 次の学習導線

結果画面の UI は `CommonResultContent` や `ResultAccuracyCard` などに分割されています。

### 7.6 復習

```text
ReviewScreen
  ↓
ReviewViewModel
  ↓
ReviewRepository
  ↓
Room
```

クイズで間違えた単語は復習対象として保存されます。
復習モードでは復習対象から問題を生成します。

### 7.7 単語詳細

```text
WordDetailScreen
  ↓
WordDetailViewModel
  ↓
WordRepository
  ↓
Room
```

単語詳細では以下を表示・操作します。

- 英単語
- 日本語意味
- 品詞
- 発音記号
- 例文
- 例文訳
- お気に入り
- 学習済み
- 関連語

### 7.8 カスタム単語・熟語・文章

```text
Custom screen
  ↓
Custom 系 ViewModel
  ↓
CustomContentRepository
  ↓
Room
```

ユーザーが独自に追加した単語、熟語、文章を保存し、それらを使ってクイズを作成できます。

対応する主な機能:

- カスタム単語登録
- カスタム熟語登録
- カスタム文章登録
- カスタム一覧表示
- カスタムトレーニング
- ランダムカスタムクイズ
- 文章並び替えクイズ

### 7.9 CSV/XLSX インポート

```text
WordImportScreen
  ↓
ImportFileReader
  ↓
CsvImportParser / XlsxImportParser
  ↓
WordDetailViewModel / CustomContentRepository
  ↓
Room
```

インポートはプレビューと確定登録の 2 段階です。
重複、エラー、不正行はプレビューで分類されます。

### 7.10 フラッシュカード

```text
FlashcardScreen
  ↓
FlashcardViewModel
  ↓
WordRepository / TrainingRepository
  ↓
Room
```

トレーニング単位で単語カードを表示し、単語と意味を切り替えながら学習できます。

### 7.11 音声・TTS

音声関連は `ui/audio/` と `ui/screen/common/` 周辺にあります。

主な要素:

- 効果音
- 結果画面の音
- TTS 読み上げ
- AudioFocus
- MediaPlayer

音声処理は UI の副作用と関係が深いため、Compose のライフサイクルに注意が必要です。

## 8. 状態管理

状態管理の中心は ViewModel と StateFlow です。

典型的な流れ:

```text
Repository Flow
  ↓
UseCase
  ↓
ViewModel StateFlow
  ↓
Compose collectAsStateWithLifecycle()
  ↓
UI 描画
```

画面側は `collectAsStateWithLifecycle()` を使って Flow を購読します。
これにより、Compose のライフサイクルに合わせて状態収集が行われます。

クイズのように一連のセッションを持つ機能では、`QuizSession` が `QuizState` を保持します。

## 9. セキュリティと通信

現時点のシステムはローカルファーストで、バックエンド API 通信は前提になっていません。

確認できる特徴:

- 認証トークン管理は存在しない
- API レスポンス DTO は存在しない
- INTERNET 権限を前提とした通信機能は中心ではない
- 学習データは Room に保存
- `android:allowBackup="false"`
- release build は minify 有効

そのため、Web API のエラー共通処理、認証更新、リフレッシュトークン、HTTP レイヤーなどは現在の主要構成には含まれていません。

## 10. テスト構成

主なテスト対象:

- CSV import parser
- XLSX worksheet parser
- Repository support logic
- Quiz score calculator
- MainActivity smoke test

テストファイルは `app/src/test/` と `app/src/androidTest/` にあります。

今後増やすと効果が大きいテスト:

- Navigation route の引数生成/解析
- QuizSession のタイマー・回答・終了条件
- Repository の transaction 境界
- CSV/XLSX の異常系
- ViewModel の UI state 遷移
- Compose UI の主要導線
- Room migration test

## 11. このシステムの強み

- Android ネイティブで、端末内完結の学習アプリとして作られている
- Compose / Material 3 / Navigation Compose を使った現代的な UI 構成
- Room によるローカル永続化が中心で、オフライン利用に向いている
- 画面・ViewModel・Repository・UseCase が段階的に分割されている
- Route が集中管理されており、文字列ルートの散在が少ない
- Entity / DAO が機能別に分かれている
- CSV/XLSX インポートまで含む実用的な学習データ運用がある
- Seed data により初回起動後すぐ学習できる
- クイズ、復習、成績、フラッシュカード、文章問題など学習体験が広い

## 12. 保守上の注意点

### Repository の責務

Repository は分割されていますが、`QuizRepository` と `CustomContentRepository` はまだ比較的大きな責務を持ちやすい領域です。

特に以下は変更時の影響範囲が広くなりやすいです。

- クイズ生成
- クイズ結果保存
- 進捗更新
- 復習リスト更新
- カスタムデータ import
- カスタムクイズ生成

これらを変更すると、結果画面、復習、学習履歴、進捗表示へ波及する可能性があります。

### UI と副作用

Compose UI の中には、音声、TTS、アニメーション、タイマーなど副作用を伴う処理があります。
見た目の変更が動作に影響しないよう、`LaunchedEffect`、`DisposableEffect`、ViewModel の責務分離を意識する必要があります。

### Package 境界

フォルダは細かく分かれていますが、一部ファイルでは package が `com.example.vocabapp` のままになっています。
将来的に保守性をさらに上げるなら、フォルダ構成と package 構成を揃えると依存境界が明確になります。

### Migration

Room DB は version 10 まで進んでいます。
Entity を変更する場合は Migration と schema export の確認が必須です。

### テスト

ビジネスロジックの一部はテストされていますが、アプリ全体の機能量に比べるとまだ追加余地があります。
特にクイズ、インポート、Room migration、ViewModel、Navigation はテスト効果が大きいです。

## 13. Claude に依頼するときの前提

Claude にこのプロジェクトの作業を依頼する場合は、次の前提を伝えると安全です。

- Flutter / React Native / Web へ変換しない
- Kotlin + Jetpack Compose の Android ネイティブアプリとして扱う
- 既存の MVVM / Hilt / Room / Compose 構成に合わせる
- バックエンド API は現時点で存在しない前提にする
- データは Room に保存する
- DB Entity を変える場合は必ず Migration を追加する
- 画面の状態は ViewModel と StateFlow を中心に扱う
- Compose では `collectAsStateWithLifecycle()` を優先する
- 既存の Route sealed class を使って遷移する
- 動作変更を伴うリファクタリングは避け、まずテストを通す
- CSV/XLSX import はローカル処理として扱う
- 音声/TTS/MediaPlayer 周辺はライフサイクル管理に注意する

## 14. 開発時によく使う確認コマンド

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew assembleDebug testDebugUnitTest
./gradlew assembleDebugAndroidTest
```

この環境では Gradle のキャッシュ場所を固定するため、必要に応じて次のように実行します。

```bash
GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest
```

## 15. まとめ

このシステムは、英単語・熟語・文章学習をローカルで完結させる Android ネイティブ学習アプリです。

技術的には、Kotlin、Jetpack Compose、Material 3、Navigation Compose、Hilt、Room、StateFlow、UseCase/Repository 層を使った MVVM 構成です。

ユーザー体験としては、レッスン、トレーニング、クイズ、結果、復習、単語詳細、カスタム登録、CSV/XLSX インポート、フラッシュカード、文章問題を提供します。

保守面では、以前より画面や ViewModel はかなり分割されています。
今後さらに改善するなら、Repository の責務分離、UI 副作用の整理、package 境界の整備、クイズ/インポート/Navigation/Room migration のテスト追加が重要です。
