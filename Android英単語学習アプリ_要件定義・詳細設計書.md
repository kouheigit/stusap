# Android英単語学習アプリ 要件定義・詳細設計書

## 1. 開発目的

Android向けに、TOEIC学習者向けの英単語学習アプリを開発する。

アプリの目的は、目標スコア別に分類された英単語を、4択クイズ形式で効率よく学習できるようにすることである。

TEPPAN英単語のように、以下の学習体験を実現する。

- 目標スコア別の単語学習
- レッスン単位の学習
- 10問単位のトレーニング
- 4択クイズ
- 正解・不正解の即時フィードバック
- 不正解単語の復習
- 学習回数・学習時間・学習日の記録
- 結果画面の表示
- バッジや進捗表示による学習継続支援

## 2. 開発環境

### Androidアプリ側

- IDE: Android Studio
- 言語: Kotlin
- UI: Jetpack Compose
- アーキテクチャ: MVVM
- 非同期処理: Kotlin Coroutines
- 状態管理: StateFlow / MutableStateFlow
- ローカルDB: Room
- 設定保存: DataStore
- 音声再生: MediaPlayer または ExoPlayer
- 依存性注入: Hilt
- 画面遷移: Navigation Compose

### 初期開発方針

初期版ではLaravel API接続は行わず、Androidアプリ内のRoom DBまたは固定JSONデータで動作させる。

後続拡張としてLaravel API + MySQL連携を想定する。

## 3. 対象ユーザー

- TOEIC 600点以上を目指す学習者
- スキマ時間に英単語を学びたいユーザー
- 4択形式でテンポよく単語を覚えたいユーザー
- 間違えた単語を繰り返し復習したいユーザー

## 4. 機能要件

### 4.1 トップ画面

トップ画面では、学習全体の進捗を表示する。

#### 表示項目

- アプリタイトル
- 累計学習時間
- 今週の学習時間
- マスター済みレッスン数
- 全レッスン数
- 目標スコア別レッスン一覧
- 復習トレーニングボタン

#### 操作

- レッスンを選択するとレッスン詳細画面へ遷移する
- 復習トレーニングを押すと復習画面へ遷移する

### 4.2 レッスン一覧画面

目標スコア別にレッスンを表示する。

#### 目標スコア区分

- 600点
- 730点
- 860点
- 990点

#### レッスン構成

- 1レッスン = 100語
- 1レッスン内に10トレーニング
- 1トレーニング = 10語

#### 表示項目

- レッスン名
- 対象単語範囲
- 学習状態
- 最終学習日
- Masterバッジ
- 進捗率

#### 学習状態

- 未学習
- 学習中
- 完了
- Master

### 4.3 トレーニング一覧画面

選択したレッスン内の10個のトレーニングを表示する。

#### 表示項目

- トレーニング番号
- 対象単語範囲
- 学習回数
- 正解率
- 最終学習日
- 星バッジ
- 進捗ゲージ

#### 操作

- トレーニングを押すとクイズ画面へ遷移する
- 前のレッスンへ移動できる
- 次のレッスンへ移動できる

### 4.4 クイズ画面

10問単位で英単語クイズを出題する。

#### 出題形式

英単語を表示し、日本語訳を4択から選択する。

#### 表示項目

- 問題番号
- 全問題数
- 進捗バー
- 残り時間タイマー
- 英単語
- 発音記号
- 音声再生ボタン
- 4つの選択肢
- わからないボタン

#### 操作

- 選択肢をタップして回答
- わからないをタップして不正解扱い
- 音声ボタンで単語音声を再生
- 回答後、正解・不正解を表示
- 一定時間後に次の問題へ進む

#### 正解時フィードバック

- 画面中央に○表示
- 正解選択肢を緑色でハイライト
- 効果音を再生してもよい

#### 不正解時フィードバック

- 画面中央に×表示
- 選択した誤答をピンクまたは赤色でハイライト
- 正解選択肢を緑色でハイライト
- 対象単語を復習候補に追加する

### 4.5 結果画面

10問終了後に結果を表示する。

#### 表示項目

- 正解数
- 全問題数
- 正解率
- 今回の学習時間
- 今週の学習時間
- 獲得星数
- メッセージ

#### 星判定

- 正解率 90%以上: 星3
- 正解率 70%以上: 星2
- 正解率 50%以上: 星1
- 50%未満: 星0

#### メッセージ例

- 90%以上: Excellent!
- 70%以上: Good job!
- 50%以上: Nice try!
- 50%未満: Keep going!

#### 操作

- 再チャレンジ
- トレーニング一覧へ戻る
- 次のトレーニングへ進む

### 4.6 復習トレーニング機能

不正解だった単語、またはユーザーがチェックした単語を復習できる。

#### 復習対象

- クイズで不正解だった単語
- わからないを選んだ単語
- ユーザーがチェックした単語

#### 表示項目

- 復習対象単語一覧
- 英単語
- 日本語訳
- 発音記号
- 品詞
- 最終不正解日
- 復習チェック状態

#### 操作

- 復習クイズを開始
- 単語詳細を見る
- 復習対象から外す

### 4.7 単語詳細画面

単語の詳細情報を表示する。

#### 表示項目

- 英単語
- 日本語訳
- 発音記号
- 品詞
- 例文
- 例文の日本語訳
- 関連語
- 音声再生ボタン
- 復習チェックボタン

### 4.8 学習ログ機能

ユーザーの学習履歴を保存する。

#### 保存項目

- 学習日
- 学習開始時間
- 学習終了時間
- 学習秒数
- 対象レッスン
- 対象トレーニング
- 正解数
- 不正解数
- 正解率

### 4.9 バッジ機能

学習状況に応じてバッジを表示する。

#### トレーニングバッジ

- 星0
- 星1
- 星2
- 星3

#### レッスンバッジ

- 未学習
- 学習中
- Complete
- Master

#### Master条件

レッスン内の全10トレーニングで星3を獲得した場合、LessonをMasterにする。

## 5. 非機能要件

### 5.1 パフォーマンス

- 画面遷移は1秒以内を目標とする
- クイズ回答後のフィードバックは即時表示する
- Room DBからの読み込みは非同期で行う

### 5.2 保守性

- UI、ViewModel、Repository、Databaseを分離する
- クイズ判定ロジックはViewModelに直接書きすぎず、UseCaseまたはService相当のクラスに分離する
- 単語データ構造は後からAPI連携に置き換えやすいようにする

### 5.3 拡張性

将来的に以下を追加できる設計にする。

- Laravel API連携
- ログイン機能
- 複数端末同期
- 音声ファイルダウンロード
- オフライン対応
- 日別学習カレンダー
- ランキング機能

## 6. 画面一覧

### 6.1 画面構成

1. SplashScreen
2. HomeScreen
3. LessonListScreen
4. TrainingListScreen
5. QuizScreen
6. ResultScreen
7. ReviewScreen
8. WordDetailScreen
9. StudyLogScreen
10. SettingsScreen

## 7. 画面遷移

### 7.1 基本遷移

```text
SplashScreen
→ HomeScreen

HomeScreen
→ LessonListScreen
→ TrainingListScreen
→ QuizScreen
→ ResultScreen

HomeScreen
→ ReviewScreen
→ QuizScreen
→ ResultScreen

TrainingListScreen
→ WordDetailScreen

ReviewScreen
→ WordDetailScreen

HomeScreen
→ StudyLogScreen

HomeScreen
→ SettingsScreen
```

## 8. データ設計

Room DBを利用する。

### 8.1 lessons テーブル

レッスン情報を管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| scoreTarget | Int | 目標スコア |
| title | String | レッスン名 |
| wordStartNumber | Int | 開始単語番号 |
| wordEndNumber | Int | 終了単語番号 |
| displayOrder | Int | 表示順 |

### 8.2 trainings テーブル

トレーニング情報を管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| lessonId | Int | レッスンID |
| title | String | トレーニング名 |
| wordStartNumber | Int | 開始単語番号 |
| wordEndNumber | Int | 終了単語番号 |
| displayOrder | Int | 表示順 |

### 8.3 words テーブル

単語情報を管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| trainingId | Int | トレーニングID |
| english | String | 英単語 |
| meaning | String | 日本語訳 |
| phonetic | String | 発音記号 |
| partOfSpeech | String | 品詞 |
| exampleSentence | String | 例文 |
| exampleTranslation | String | 例文日本語訳 |
| audioUrl | String? | 単語音声パス |
| exampleAudioUrl | String? | 例文音声パス |
| displayOrder | Int | 表示順 |

### 8.4 word_choices テーブル

4択選択肢を管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| wordId | Int | 単語ID |
| choiceText | String | 選択肢 |
| isCorrect | Boolean | 正解フラグ |
| displayOrder | Int | 表示順 |

### 8.5 word_relations テーブル

関連語を管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| wordId | Int | 単語ID |
| relatedWord | String | 関連語 |
| relatedMeaning | String | 関連語の意味 |

### 8.6 quiz_attempts テーブル

1回分のクイズ結果を管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| trainingId | Int? | トレーニングID |
| isReview | Boolean | 復習クイズかどうか |
| startedAt | Long | 開始日時 |
| finishedAt | Long | 終了日時 |
| totalQuestions | Int | 問題数 |
| correctCount | Int | 正解数 |
| wrongCount | Int | 不正解数 |
| accuracy | Float | 正解率 |
| studySeconds | Int | 学習秒数 |
| starCount | Int | 星数 |

### 8.7 quiz_attempt_answers テーブル

クイズ各問の回答結果を管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| quizAttemptId | Int | クイズ結果ID |
| wordId | Int | 単語ID |
| selectedChoiceId | Int? | 選択肢ID |
| isCorrect | Boolean | 正解フラグ |
| answeredAt | Long | 回答日時 |
| responseMillis | Int | 回答にかかった時間 |
| selectedUnknown | Boolean | わからないを選んだか |

### 8.8 review_words テーブル

復習対象単語を管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| wordId | Int | 単語ID |
| addedReason | String | wrong / unknown / checked |
| isActive | Boolean | 復習対象か |
| addedAt | Long | 追加日時 |
| lastReviewedAt | Long? | 最終復習日時 |
| wrongCount | Int | 不正解回数 |
| correctCount | Int | 復習での正解回数 |

### 8.9 study_logs テーブル

学習ログを管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| studiedAt | Long | 学習日 |
| lessonId | Int? | レッスンID |
| trainingId | Int? | トレーニングID |
| studySeconds | Int | 学習秒数 |
| correctCount | Int | 正解数 |
| wrongCount | Int | 不正解数 |

### 8.10 user_progress テーブル

学習進捗を管理する。

| カラム名 | 型 | 説明 |
|---|---|---|
| id | Int | 主キー |
| lessonId | Int | レッスンID |
| trainingId | Int? | トレーニングID |
| studyCount | Int | 学習回数 |
| bestAccuracy | Float | 最高正解率 |
| bestStarCount | Int | 最高星数 |
| lastStudiedAt | Long? | 最終学習日時 |
| isMastered | Boolean | マスター済みか |

## 9. Kotlinデータクラス設計

### 9.1 Word

```kotlin
data class Word(
    val id: Int,
    val trainingId: Int,
    val english: String,
    val meaning: String,
    val phonetic: String,
    val partOfSpeech: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val audioUrl: String?,
    val exampleAudioUrl: String?,
    val displayOrder: Int
)
```

### 9.2 WordChoice

```kotlin
data class WordChoice(
    val id: Int,
    val wordId: Int,
    val choiceText: String,
    val isCorrect: Boolean,
    val displayOrder: Int
)
```

### 9.3 QuizQuestion

```kotlin
data class QuizQuestion(
    val word: Word,
    val choices: List<WordChoice>
)
```

### 9.4 QuizState

```kotlin
data class QuizState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedChoiceId: Int? = null,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean? = null,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val remainingMillis: Long = 3000L,
    val startedAt: Long = 0L
)
```

## 10. アーキテクチャ設計

### 10.1 パッケージ構成

```text
com.example.vocabapp
├── MainActivity.kt
├── app
│   └── VocabApp.kt
├── data
│   ├── local
│   │   ├── AppDatabase.kt
│   │   ├── dao
│   │   └── entity
│   ├── repository
│   └── seed
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── ui
│   ├── navigation
│   ├── theme
│   ├── home
│   ├── lesson
│   ├── training
│   ├── quiz
│   ├── result
│   ├── review
│   ├── worddetail
│   └── components
└── util
```

### 10.2 Repository

- LessonRepository
- TrainingRepository
- WordRepository
- QuizRepository
- ReviewRepository
- StudyLogRepository
- ProgressRepository

### 10.3 UseCase

- GetLessonsUseCase
- GetTrainingsUseCase
- StartQuizUseCase
- SubmitAnswerUseCase
- FinishQuizUseCase
- AddReviewWordUseCase
- GetReviewWordsUseCase
- CalculateProgressUseCase
- CalculateStudyTimeUseCase

## 11. クイズ処理詳細

### 11.1 クイズ開始

1. trainingIdを受け取る
2. 対象trainingに紐づく10語を取得
3. 各単語に紐づく選択肢を取得
4. 選択肢の表示順をランダム化する
5. QuizStateを初期化する
6. 1問目を表示する

### 11.2 回答処理

1. ユーザーが選択肢をタップ
2. selectedChoiceIdを保存
3. 正解選択肢と比較
4. 正解ならcorrectCountを加算
5. 不正解ならwrongCountを加算
6. 不正解または「わからない」ならreview_wordsへ登録
7. quiz_attempt_answers用の回答データを一時保持
8. UIに○または×を表示
9. 1秒後に次の問題へ進む

### 11.3 クイズ終了

1. 全10問が終了
2. quiz_attemptsに結果を保存
3. quiz_attempt_answersに各回答を保存
4. study_logsに学習時間を保存
5. user_progressを更新
6. ResultScreenへ遷移

## 12. UIデザイン方針

### 12.1 全体

- 白背景
- アクセントカラーは青系
- 正解は緑
- 不正解は赤またはピンク
- カード型UIを基本にする
- 文字は大きめで視認性を重視する

### 12.2 クイズ画面

- 上部に進捗バー
- 中央に英単語
- 英単語の下に発音記号
- 音声ボタンを単語横に配置
- 下部に4択ボタン
- 最下部に「わからない」ボタン
- 回答後に中央へ○/×の大きなアニメーション表示

### 12.3 結果画面

- 正解率を大きく表示
- 星を3つ表示
- 今回の学習時間を表示
- 再チャレンジボタンを大きく配置
- 次へボタンを配置

## 13. サンプル初期データ

初期実装では以下のような単語をseedデータとして入れる。

### Lesson

- 目標600点 Lesson 1
- 1〜100語

### Training

- Training 1: 1〜10語
- Training 2: 11〜20語

### Word例

1. department
   - 意味: 部門
   - 発音記号: [dɪpɑːrtmənt]
   - 品詞: 名詞
   - 例文: The sales department is on the second floor.
   - 日本語訳: 営業部は2階にあります。

2. against
   - 意味: 〜に対して
   - 発音記号: [əɡénst]
   - 品詞: 前置詞
   - 例文: We are against the new policy.
   - 日本語訳: 私たちはその新しい方針に反対しています。

3. material
   - 意味: 材料、資料
   - 発音記号: [mətíəriəl]
   - 品詞: 名詞
   - 例文: Please prepare the meeting materials.
   - 日本語訳: 会議資料を準備してください。

## 14. Codexへの実装指示

以下の条件でAndroidアプリを実装してください。

- Kotlin + Jetpack Composeで実装する
- Android Studioで開けるGradleプロジェクトにする
- MVVM構成にする
- Room DBを使用する
- Hiltを使用する
- Navigation Composeを使用する
- まずはログイン機能なしで実装する
- API通信は不要
- 初期データはアプリ起動時にRoomへseedする
- レッスン一覧、トレーニング一覧、クイズ、結果、復習、単語詳細を実装する
- クイズは10問単位で出題する
- 回答後に正解・不正解を表示する
- 不正解単語は復習対象に追加する
- 結果画面で正解率、星、学習時間を表示する
- UIはTEPPAN英単語風に、シンプルで見やすいデザインにする
- ただし、既存アプリの画像・ロゴ・文言をそのままコピーしない
- 独自アプリとして実装する

## 15. 実装順序

### Step 1

- Androidプロジェクト作成
- Jetpack Compose設定
- Navigation Compose設定
- 画面の空実装

### Step 2

- Room DB作成
- Entity作成
- DAO作成
- 初期データseed作成

### Step 3

- HomeScreen実装
- LessonListScreen実装
- TrainingListScreen実装

### Step 4

- QuizScreen実装
- QuizViewModel実装
- 回答判定実装
- ○×フィードバック実装

### Step 5

- ResultScreen実装
- quiz_attempts保存
- study_logs保存
- user_progress更新

### Step 6

- ReviewScreen実装
- 不正解単語一覧
- 復習クイズ開始

### Step 7

- WordDetailScreen実装
- 例文、関連語、発音記号、品詞表示

### Step 8

- UI調整
- バグ修正
- 実機確認

## 16. 完成条件

以下が実機またはエミュレーターで動作すること。

- レッスン一覧が表示される
- トレーニング一覧が表示される
- クイズが開始できる
- 4択回答ができる
- 正解・不正解が表示される
- 10問終了後に結果画面へ遷移する
- 不正解単語が復習画面に表示される
- 復習トレーニングができる
- 単語詳細画面が表示できる
- 学習回数、学習日、正解率が保存される

## 17. 注意事項

- TEPPAN英単語の完全コピーではなく、英単語学習アプリとして独自実装する
- 既存アプリのロゴ、画像、音声、単語データを無断利用しない
- UIは参考にするが、配色やレイアウトは独自に調整する
- 単語データはサンプルデータとして独自に作成する
