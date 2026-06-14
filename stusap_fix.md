# stusap 完全リファクタリング計画

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 蓄積した重複・デッドコード・構造の歪みを、外部から見た挙動を一切変えずに段階的に除去し、保守可能な MVVM 構成へ戻す。

**Architecture:** 既存の Kotlin + Jetpack Compose + Room + Hilt + MVVM 構成は維持する。「フラット偽装パッケージの正常化 → データ層の責務分離 → UI 層の重複排除 → ガードレール導入」の順に、各フェーズが独立してビルド・テスト可能な状態で進める。

**Tech Stack:** Kotlin / Jetpack Compose / Material 3 / Room / Hilt / JUnit4 / MockK / Turbine / Gradle Kotlin DSL

**この文書の位置づけ:** CLAUDE.md の Spec-Driven Development ルールに基づく仕様書を兼ねる。フェーズ0で `.claude/specs/` にポインタを置く。

---

## 0. 調査で確定した「無駄」のインベントリ（証拠付き）

実コードを読んで確認済みの問題。推測ではない。

### A. パッケージ構造の偽装（最重要・32ファイル）

32 ファイルがディレクトリ上は `ui/screen/home/` 等に置かれているのに、パッケージ宣言は全て `package com.example.vocabapp`（ルート直下）になっている。実態は **巨大なフラットパッケージ** であり:

- `MainActivity.kt` が `HomeScreen` 等を import なしで参照できてしまう（同一パッケージのため）
- `internal` / private トップレベル宣言の衝突リスクが常にある
- IDE のパッケージ単位リファクタリング・解析が機能しない

該当: `ui/screen/{home,lesson,training,word,quiz,custom,settings,flashcard,review,imports}/` の全画面 26 ファイル + `data/import/` の 6 ファイル。

検出コマンド（フェーズ2完了判定にも使う）:

```bash
for f in $(find app/src/main/java -name "*.kt"); do
  pkg=$(grep -m1 "^package " "$f" | sed 's/package //;s/ .*//')
  expected=$(dirname "$f" | sed 's|app/src/main/java/||;s|/|.|g')
  [ "$pkg" != "$expected" ] && echo "$f : declared=$pkg expected=$expected"
done
```

注意: `data/import/` は `import` が Kotlin の予約語のためパッケージ名にできない。これがルートパッケージに逃げた原因と推定。ディレクトリごと `data/imports/` へリネームして解決する（UI 側は既に `ui/screen/imports/` と複数形を採用済みで一貫する）。

### B. デッドコード

- `ui/screen/passage/PassagePracticeScreen.kt:706-785` — `private fun PassageResultScreen` と専用ヘルパー `ResultMiniStat`。どこからも呼ばれていない（`PassageResultReviewScreen` に置き換え済みの残骸）。約 80 行 + 専用 import 7 件。

### C. データ層の歪み

- **神 DAO**: `AppDao` が 9 個の分割 DAO（WordDao, LessonDao, TrainingDao, QuizDao, ReviewDao, StudyLogDao, UserProgressDao, CustomContentDao, AppSettingsDao）を全部継承した窓口になっており、全リポジトリが `AppDao` に依存。分割 DAO は `AppDatabase` に abstract fun として公開されているが **利用箇所ゼロ**。
- **逆依存**: `data/local/dao/AppDao.kt:23` が `com.example.vocabapp.data.repository.IDIOM_LESSON_START_ID` を import。DAO 層 → リポジトリ層への依存逆転。
- **神クラス `QuizRepository`（401行）**: クイズ生成・採点保存・復習リスト更新・進捗更新・文章穴埋め問題の生成ロジック（純粋なドメインロジック）が同居。
- **重複**: `QuizRepository.finishQuiz`(57-102) / `finishCustomQuizInternal`(188-239) / `finishSentenceQuiz`(154-186) が `QuizAttemptEntity` + `StudyLogEntity` の組み立てをほぼ同一コードで3回繰り返している。
- **マジック定数の分散**: `listOf("①", "②", "③", "④")` が `QuizRepository.kt` 内に2回（371行・389行）+ UI 側 `SentenceQuizContent.kt` / `AddSentenceScreen.kt` にも存在。
- **要調査（挙動変更はしない）**: `QuizRepository.kt:163` で `trainingId = CUSTOM_SENTENCE_LESSON_ID`（レッスンIDをトレーニングID欄に格納）。意図的か事故か不明。フェーズ3で調査しコメントで結論を残す。

### D. UI 層の重複・ハードコード

- **ほぼ同一の画面ペア**: `CustomWordListScreen`(202行) / `CustomIdiomListScreen`(158行)、および `CustomWordListViewModel` / `CustomIdiomListViewModel`（diff は favorite/learned 操作の有無のみ）。
- **結果画面が4系統**: `ResultScreen` + `CustomWordQuizResultContent` / `SentenceResultContent` / `PassageResultReviewScreen` / 共通部品 `CommonResultContent` `CommonResultSections` — 共通部品があるのに各画面が独自実装を持つ。
- **ハードコード色**: `PassagePracticeScreen.kt` に `Color(0x...)` 直書きが 22 箇所、`PassageReviewModels.kt` に 5 箇所。テーマ（`ui/theme/`）が存在するのに迂回している。CLAUDE.md の Prohibited Patterns（ハードコード値の直接埋め込み）違反。
- **ハードコード日本語文字列**: `ui/` 配下に約 319 個の日本語リテラル。`res/values/strings.xml`(105件) と `values-en/strings.xml`(79件) が存在し `stringResource` も 126 箇所で使われており、**二重管理状態**。values-en があるのにハードコード箇所は翻訳不能。
- **本番コードにフィクスチャ**: `PassagePracticeScreen` の引数 `sets: List<PassageSet> = PassagePracticeFixtures.sets` がデフォルト引数でサンプルデータを注入。`QuizScreen.kt:28` はこのデフォルトに依存して本番でフィクスチャを表示している。
- **MainActivity に NavHost 全定義**（148行中110行がナビゲーション）。`ui/navigation/` パッケージがあるのに Activity に同居。

### E. アーキテクチャの不整合

- UseCase 層（`domain/usecase/` に14個）が存在するのに、ViewModel 21個中 **6個しか UseCase を使っていない**。残り18箇所は Repository 直依存。レイヤリング方針が二重基準。

---

## フェーズ構成と依存関係

| フェーズ | 内容 | リスク | 依存 |
|---|---|---|---|
| 0 | ベースライン確立・安全網 | なし | — |
| 1 | デッドコード削除 | 低 | 0 |
| 2 | パッケージ構造の正常化（32ファイル） | 低（機械的・コンパイラが検証） | 1 |
| 3 | データ層の分離・重複排除 | 中 | 2 |
| 4 | UI 層の重複排除・ハードコード除去 | 中 | 2（3と並行可） |
| 5 | アーキテクチャ方針統一 + 静的解析ガードレール | 低 | 3, 4 |
| 6 | 最終検証（実機/エミュレータ） | — | 全部 |

**全フェーズ共通ルール:**

- 挙動変更・UI 変更は一切禁止（Change Scope Rules 準拠）。リファクタリング前後で画面表示・遷移・保存データが同一であること。
- 各タスク完了ごとに `./gradlew :app:testDebugUnitTest` と `./gradlew assembleDebug` を通してからコミット。
- CLI ビルドが `Could not load module <Error module>` で落ちる場合は `./gradlew clean assembleDebug -Pkapt.use.k2=true` を使う（既知の kapt/K2 問題）。
- UI に触れるフェーズ（1, 4）はコミット前に `.claude/prompts/deploy.md` の手順でエミュレータ確認。「直したのに直らない」場合は古い APK 残存を疑い `clean assembleDebug` から入れ直す。

---

## フェーズ0: ベースライン確立

### Task 0.1: 作業ブランチと仕様ポインタの作成

**Files:**
- Create: `.claude/specs/2026-06-11-stusap-refactoring.md`

- [ ] **Step 1: 現在の作業状態を確認・退避**

```bash
git status
```

未追跡の `app/src/test/java/com/example/vocabapp/viewmodel/CustomPassageRegistrationViewModelTest.kt` は `passage-bulk-submit` ブランチの作業物。先にそちらのブランチでコミット（またはユーザーに確認）してから進める。リファクタリングと混ぜない。

- [ ] **Step 2: main からリファクタリング用ブランチを作成**

```bash
git checkout main && git pull
git checkout -b refactor/stusap-fix
```

- [ ] **Step 3: 仕様ポインタを作成**

`.claude/specs/2026-06-11-stusap-refactoring.md` を作成:

```markdown
# stusap リファクタリング仕様

本リファクタリングの仕様・受け入れ条件・タスク分解はリポジトリルートの
`stusap_fix.md` に定義する。挙動変更は一切含まない（受け入れ条件 = 全
既存テスト green + 画面表示/遷移/保存データの同一性）。
```

- [ ] **Step 4: コミット**

```bash
git add stusap_fix.md .claude/specs/2026-06-11-stusap-refactoring.md
git commit -m "docs: add full refactoring plan and spec pointer"
```

### Task 0.2: ベースライン計測

- [ ] **Step 1: 既存テストを全実行し、結果を記録**

```bash
./gradlew :app:testDebugUnitTest 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL。失敗するテストがある場合は **リファクタリング開始前に** 原因を調査して報告（壊れた状態の上に積まない）。

- [ ] **Step 2: ビルド確認**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 行数ベースラインを記録**

```bash
find app/src/main -name "*.kt" | xargs wc -l | tail -1
```

2026-06-11 時点の実測: main 配下 約 15,000 行（テスト含め 17,292 行）。フェーズ完了ごとに同コマンドで削減量を計測する。

---

## フェーズ1: デッドコード削除

### Task 1.1: 未使用の PassageResultScreen を削除

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/passage/PassagePracticeScreen.kt:706-785`

- [ ] **Step 1: 呼び出し元ゼロを再確認**

```bash
grep -rn "PassageResultScreen\b" app/src --include="*.kt" | grep -v PassageResultReviewScreen
grep -rn "ResultMiniStat" app/src --include="*.kt"
```

Expected: 定義行（PassagePracticeScreen.kt 内）のみがヒット。

- [ ] **Step 2: 706〜785行（`private fun PassageResultScreen` と `private fun ResultMiniStat` の全体）を削除**

`formatClock`（787行〜）は `PassageTimerBar` が使用中のため **残す**。

- [ ] **Step 3: 削除で不要になった import を除去**

このファイル内で死コードのみが使っていた以下を削除:

```kotlin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import com.example.vocabapp.ui.theme.Danger
import com.example.vocabapp.ui.theme.Success
```

- [ ] **Step 4: ビルド・テスト**

```bash
./gradlew :app:testDebugUnitTest assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 長文問題画面をエミュレータで目視確認（deploy.md 手順）し、コミット**

```bash
git add app/src/main/java/com/example/vocabapp/ui/screen/passage/PassagePracticeScreen.kt
git commit -m "refactor: remove unused PassageResultScreen dead code"
```

### Task 1.2: プロジェクト全体の未使用宣言スイープ

- [ ] **Step 1: internal/private トップレベル関数のうち参照ゼロのものを検出**

```bash
for sym in $(grep -rhoE "^(internal |private )?fun [A-Z][A-Za-z]+" app/src/main --include="*.kt" | awk '{print $NF}' | sort -u); do
  count=$(grep -rh "\b$sym\b" app/src --include="*.kt" | grep -vc "fun $sym")
  [ "$count" -eq 0 ] && echo "UNUSED: $sym"
done
```

注: フラットパッケージのため grep ベースで十分検出できる。`@Composable` の Preview 関数と Hilt のエントリポイントは除外判断する。

- [ ] **Step 2: 検出された各シンボルを個別に確認し、本当に未使用なら削除（1コミットにまとめてよいが、削除対象一覧をコミットメッセージに列挙する）**

- [ ] **Step 3: ビルド・テスト・コミット**

```bash
./gradlew :app:testDebugUnitTest assembleDebug
git add -A && git commit -m "refactor: remove unused top-level declarations"
```

---

## フェーズ2: パッケージ構造の正常化

**方針:** ディレクトリ単位のバッチで「パッケージ宣言を実ディレクトリに一致させる → コンパイルエラーになった参照元に import を足す」を繰り返す。コンパイラが全参照を検証するため安全。Android Studio がある環境なら「Move to package」リファクタリングで import 更新を自動化してよい（結果は同じ）。

**バッチ順序（依存の少ない順）:** ① `data/import` → ② `ui/screen/word` + `ui/screen/lesson` → ③ `ui/screen/custom` → ④ `ui/screen/quiz` → ⑤ 残り（home/training/settings/flashcard/review/imports）。

### Task 2.1: data/import → data/imports（予約語回避のためディレクトリごと移動）

**Files:**
- Move: `app/src/main/java/com/example/vocabapp/data/import/*.kt`（6ファイル）→ `app/src/main/java/com/example/vocabapp/data/imports/`
- Modify: 参照元（`data/repository/CustomImportSupport.kt`, `CustomImportRepository.kt` ほかコンパイルエラーになった全ファイル、`app/src/test/java/com/example/vocabapp/data/import/` のテスト）

- [ ] **Step 1: ディレクトリを git mv し、パッケージ宣言を書き換え**

```bash
git mv app/src/main/java/com/example/vocabapp/data/import app/src/main/java/com/example/vocabapp/data/imports
sed -i '' 's/^package com\.example\.vocabapp$/package com.example.vocabapp.data.imports/' \
  app/src/main/java/com/example/vocabapp/data/imports/*.kt
```

- [ ] **Step 2: ビルドして未解決参照を列挙**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep "Unresolved reference" | sort -u
```

- [ ] **Step 3: エラーになった各ファイルに import を追加**

例（`CustomImportSupport.kt` の場合）:

```kotlin
// 変更前
import com.example.vocabapp.MAX_IMPORT_COLUMNS
// 変更後
import com.example.vocabapp.data.imports.MAX_IMPORT_COLUMNS
```

`XlsxImportParser` / `CsvImportParser` / `ImportFileReader` 等を import なしで使っていた箇所には新規に import を追加する。テスト側ディレクトリ `app/src/test/java/com/example/vocabapp/data/import/` も同様に `imports/` へ git mv + パッケージ修正。

- [ ] **Step 4: テスト・ビルド・コミット**

```bash
./gradlew :app:testDebugUnitTest assembleDebug
git add -A && git commit -m "refactor: align data import parsers to data.imports package"
```

### Task 2.2〜2.5: ui/screen 配下の各バッチを同じ手順で正常化

**Files:**（バッチごとに）
- Modify: 各画面ファイルのパッケージ宣言（計26ファイル、`0. インベントリ A` の一覧どおり）
- Modify: `MainActivity.kt`（同一パッケージだったため import が無い。各バッチで import 追加が必要）

- [ ] **Step 1: バッチ対象のパッケージ宣言を一致させる**（例: ui/screen/word）

```bash
sed -i '' 's/^package com\.example\.vocabapp$/package com.example.vocabapp.ui.screen.word/' \
  app/src/main/java/com/example/vocabapp/ui/screen/word/*.kt
```

- [ ] **Step 2: ビルド → 未解決参照に import 追加**

`MainActivity.kt` には例えば:

```kotlin
import com.example.vocabapp.ui.screen.word.AddWordScreen
import com.example.vocabapp.ui.screen.word.WordDetailScreen
import com.example.vocabapp.ui.screen.word.WordImportScreen
```

画面間の相互参照（例: `QuizScreen` → `PassagePracticeScreen` は既に import 済みだが、共通部品 `ui/screen/common/` への参照は import 追加が必要になる場合がある）もコンパイラの指摘どおり追加。

- [ ] **Step 3: `internal` 可視性はそのまま維持**（同一モジュール内なのでパッケージ移動で壊れない）

- [ ] **Step 4: バッチごとにテスト・ビルド・コミット**

```bash
./gradlew :app:testDebugUnitTest assembleDebug
git add -A && git commit -m "refactor: align ui.screen.word package declarations"
# 以降 lesson / custom / quiz / 残り の各バッチで同様に1コミットずつ
```

- [ ] **Step 5: 完了判定**

インベントリ A の検出コマンドを再実行し、出力ゼロを確認。

```bash
# (0章の検出コマンド)
```

Expected: 出力なし

---

## フェーズ3: データ層の分離・重複排除

### Task 3.1: DAO 層 → リポジトリ層の逆依存を解消

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/domain/model/QuizConstants.kt`
- Modify: `app/src/main/java/com/example/vocabapp/data/local/dao/AppDao.kt:23`
- Modify: `app/src/main/java/com/example/vocabapp/data/repository/RepositorySupport.kt:12`

- [ ] **Step 1: `IDIOM_LESSON_START_ID` を `QuizConstants` へ移動**

`QuizConstants` に追加:

```kotlin
/** 熟語レッスンの先頭ID。これ未満のレッスンIDは単語レッスン。 */
const val IDIOM_LESSON_START_ID = 100
```

`RepositorySupport.kt` から定数定義を削除し、参照箇所を `QuizConstants.IDIOM_LESSON_START_ID` に置換。`AppDao.kt` の import を `com.example.vocabapp.domain.model.QuizConstants` に変更。

- [ ] **Step 2: data 層から repository パッケージへの import が他にないか確認**

```bash
grep -rn "import com.example.vocabapp.data.repository" app/src/main/java/com/example/vocabapp/data/local
```

Expected: 出力なし

- [ ] **Step 3: テスト・ビルド・コミット**

```bash
./gradlew :app:testDebugUnitTest assembleDebug
git add -A && git commit -m "refactor: move IDIOM_LESSON_START_ID to domain constants"
```

### Task 3.2: 文章穴埋め問題の生成ロジックを domain へ抽出（TDD）

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/data/repository/QuizRuntime.kt`（interface 化 — 現状は final class でテストダブルを作れない）
- Test: `app/src/test/java/com/example/vocabapp/domain/usecase/quiz/SentenceQuestionFactoryTest.kt`（新規）
- Create: `app/src/main/java/com/example/vocabapp/domain/usecase/quiz/SentenceQuestionFactory.kt`
- Modify: `app/src/main/java/com/example/vocabapp/data/repository/QuizRepository.kt:345-401`（移動元を削除し委譲）
- Modify: `app/src/main/java/com/example/vocabapp/di/AppModule.kt`（QuizRuntime のバインド追加）

- [ ] **Step 0: `QuizRuntime` を interface 化する**

現状の `QuizRuntime.kt`（final class）を以下に置き換える:

```kotlin
package com.example.vocabapp.data.repository

import javax.inject.Inject

interface QuizRuntime {
    fun nowMillis(): Long
    fun <T> shuffled(items: List<T>): List<T>
    fun randomInt(from: Int, to: Int): Int
}

class DefaultQuizRuntime @Inject constructor() : QuizRuntime {
    override fun nowMillis(): Long = System.currentTimeMillis()
    override fun <T> shuffled(items: List<T>): List<T> = items.shuffled()
    override fun randomInt(from: Int, to: Int): Int = (from..to).random()
}
```

`AppModule.kt`（または `@Binds` 用の abstract module）にバインドを追加:

```kotlin
@Provides
fun provideQuizRuntime(): QuizRuntime = DefaultQuizRuntime()
```

既存テストで `QuizRuntime` をインスタンス化している箇所があればコンパイラの指摘に従い `DefaultQuizRuntime` か Fake に置き換える。ここで一度ビルド・テストを通す。

- [ ] **Step 1: 失敗するテストを先に書く**

```kotlin
package com.example.vocabapp.domain.usecase.quiz

import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.repository.QuizRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SentenceQuestionFactoryTest {

    // シャッフル・乱数を固定し、決定的にテストする
    private val fixedRuntime = object : QuizRuntime {
        override fun nowMillis(): Long = 0L
        override fun <T> shuffled(items: List<T>): List<T> = items
        override fun randomInt(from: Int, to: Int): Int = from
    }
    private val factory = SentenceQuestionFactory(fixedRuntime)

    private fun entity(sentence: String) =
        CustomSentenceEntity(id = 1, sentence = sentence, meaning = "意味", addedAt = 0L)

    @Test
    fun `ブラケット4つ以上は明示指定を優先して問題化する`() {
        val q = factory.create(entity("I [want] to [go] to [the] [station] now."))
        assertEquals(listOf("want", "go", "the", "station"), q!!.answers)
        assertEquals("I ① to ② to ③ ④ now.", q.template)
    }

    @Test
    fun `ブラケットなしは連続4語をマーカー化する`() {
        val q = factory.create(entity("One two three four five six seven eight."))
        // fixedRuntime.randomInt は from を返すため、開始位置は先頭1語スキップ後に固定される
        assertEquals(4, q!!.answers.size)
        assertEquals(listOf("two", "three", "four", "five"), q.answers)
    }

    @Test
    fun `最小語数未満はnullを返す`() {
        assertNull(factory.create(entity("Too short sentence.")))
    }
}
```

注: `CustomSentenceEntity` のコンストラクタに `importedFromFile` 等の追加プロパティがある場合はデフォルト値に任せるか明示する（コンパイラに従う）。

- [ ] **Step 2: テスト実行 → 失敗(クラス未定義)を確認**

```bash
./gradlew :app:testDebugUnitTest --tests "*SentenceQuestionFactoryTest*"
```

Expected: FAIL（Unresolved reference: SentenceQuestionFactory）

- [ ] **Step 3: `QuizRepository` の private 3関数を移動して実装**

`SentenceQuestionFactory.kt`（本体は QuizRepository.kt:352-401 の移植。動作は同一）:

```kotlin
package com.example.vocabapp.domain.usecase.quiz

import com.example.vocabapp.data.local.entity.CustomSentenceEntity
import com.example.vocabapp.data.repository.QuizRuntime
import com.example.vocabapp.domain.model.QuizConstants
import com.example.vocabapp.domain.model.SentenceQuestion
import javax.inject.Inject

/**
 * 文章エンティティから穴埋めクイズ問題を生成する。
 *
 * 生成戦略は2種類あり、ブラケット方式を優先する:
 * 1. ブラケット方式: ユーザーが `[word]` 形式で答え箇所を明示している場合。
 * 2. スライス方式: ブラケットがない場合、先頭・末尾を除いた連続4単語を自動で空欄にする。
 */
class SentenceQuestionFactory @Inject constructor(
    private val runtime: QuizRuntime
) {
    fun create(entity: CustomSentenceEntity): SentenceQuestion? {
        val sentence = entity.sentence.trim()
        // `[word]` 形式にマッチする正規表現: [ から ] の間の1文字以上を捕捉グループで取得する
        val bracketPattern = Regex("\\[([^\\]]+)\\]")
        val matches = bracketPattern.findAll(sentence).toList()
        // ユーザーが答えを明示している場合はその意図を最優先で使用する
        if (matches.size >= QuizConstants.SENTENCE_ANSWER_COUNT) {
            return buildBracketQuestion(entity, sentence, matches)
        }
        return buildSliceQuestion(entity, sentence)
    }

    /** ユーザーが `[word]` で明示した箇所を空欄として問題を生成する。 */
    private fun buildBracketQuestion(
        entity: CustomSentenceEntity,
        sentence: String,
        matches: List<MatchResult>
    ): SentenceQuestion {
        val answers = matches.take(QuizConstants.SENTENCE_ANSWER_COUNT).map { it.groupValues[1] }
        var template = sentence
        matches.take(QuizConstants.SENTENCE_ANSWER_COUNT).forEachIndexed { index, match ->
            template = template.replace(match.value, QuizConstants.ANSWER_MARKERS[index])
        }
        return SentenceQuestion(entity.id, template, answers, runtime.shuffled(answers), entity.meaning)
    }

    /** ブラケットのない文章から連続4単語をランダムに空欄として問題を生成する。 */
    private fun buildSliceQuestion(entity: CustomSentenceEntity, sentence: String): SentenceQuestion? {
        val rawWords = sentence.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (rawWords.size < QuizConstants.SENTENCE_MIN_WORD_COUNT) return null
        val maxStart = rawWords.size - QuizConstants.SENTENCE_ANSWER_COUNT
        // 先頭1語（minStart=1）を除外することで文脈なしの空欄を防ぐ。単語数が少ない場合はやむを得ず先頭から取る
        val minStart = if (rawWords.size >= QuizConstants.SENTENCE_MIN_WORD_COUNT + 1) 1 else 0
        val start = if (maxStart > minStart) runtime.randomInt(minStart, maxStart) else minStart.coerceAtMost(maxStart)
        val answerSlice = rawWords.subList(start, start + QuizConstants.SENTENCE_ANSWER_COUNT)
        val answers = answerSlice.map { it.trimEnd('.', ',', '!', '?', ';', ':') }
        val templateWords = rawWords.mapIndexed { index, word ->
            if (index in start until start + QuizConstants.SENTENCE_ANSWER_COUNT) {
                val marker = QuizConstants.ANSWER_MARKERS[index - start]
                val trailing = word.drop(word.trimEnd('.', ',', '!', '?', ';', ':').length)
                if (trailing.isNotEmpty()) "$marker$trailing" else marker
            } else {
                word
            }
        }
        return SentenceQuestion(entity.id, templateWords.joinToString(" "), answers, runtime.shuffled(answers), entity.meaning)
    }
}
```

`QuizConstants.ANSWER_MARKERS` は Task 3.4 で定義する。**Task 3.4 を先に実施するか、本タスク内で同時に定義する**（重複定義を新たに作らないため）。

`QuizRepository` 側の変更:
- コンストラクタに `private val sentenceQuestionFactory: SentenceQuestionFactory` を追加
- `buildSentenceQuiz` 内の `buildSentenceQuestion(it)` を `sentenceQuestionFactory.create(it)` に置換
- private の `buildSentenceQuestion` / `buildBracketQuestion` / `buildSliceQuestion`（345-401行）を削除

- [ ] **Step 4: テスト green を確認**

```bash
./gradlew :app:testDebugUnitTest
```

Expected: PASS（新規テスト含む全件）

- [ ] **Step 5: コミット**

```bash
git add -A && git commit -m "refactor: extract SentenceQuestionFactory from QuizRepository"
```

### Task 3.3: クイズ結果保存の3重重複を排除

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/data/repository/QuizRepository.kt`

- [ ] **Step 1: 共通ヘルパーを追加**

```kotlin
/** 採点結果を quiz_attempts と study_logs に保存する共通処理。 */
private suspend fun recordAttempt(
    trainingId: Int?,
    lessonId: Int?,
    isReview: Boolean,
    startedAt: Long,
    finishedAt: Long,
    score: QuizScore
): Long {
    val attemptId = dao.insertQuizAttempt(
        QuizAttemptEntity(
            trainingId = trainingId,
            isReview = isReview,
            startedAt = startedAt,
            finishedAt = finishedAt,
            totalQuestions = score.total,
            correctCount = score.correct,
            wrongCount = score.wrong,
            accuracy = score.accuracy,
            studySeconds = score.studySeconds,
            starCount = score.starCount
        )
    )
    dao.insertStudyLog(
        StudyLogEntity(
            studiedAt = finishedAt,
            lessonId = lessonId,
            trainingId = trainingId,
            studySeconds = score.studySeconds,
            correctCount = score.correct,
            wrongCount = score.wrong
        )
    )
    return attemptId
}
```

import に `com.example.vocabapp.domain.usecase.quiz.QuizScore` を追加。

- [ ] **Step 2: `finishQuiz` / `finishCustomQuizInternal` / `finishSentenceQuiz` を `recordAttempt` 呼び出しに置換**

注意点（挙動を変えないため厳守）:
- `finishQuiz` は StudyLog の `trainingId = trainingId`、`finishCustomQuizInternal` も同じ。そのまま対応。
- `finishSentenceQuiz` は現状 attempt 側 `trainingId = CUSTOM_SENTENCE_LESSON_ID`、StudyLog 側 `lessonId = CUSTOM_SENTENCE_LESSON_ID, trainingId = null`。**この非対称は recordAttempt の引数では表現できないため、`finishSentenceQuiz` だけは attempt 用 trainingId と log 用 trainingId を分けた引数にするか、置換対象から外す。** 無理に統一しない（挙動維持が優先）。

- [ ] **Step 3: `QuizRepository.kt:163` の `trainingId = CUSTOM_SENTENCE_LESSON_ID` について調査**

git log でこの行の由来を確認:

```bash
git log -L163,163:app/src/main/java/com/example/vocabapp/data/repository/QuizRepository.kt | head -40
```

意図的（文章クイズには training 概念がなくレッスンIDを流用）なら WHY コメントを追記。事故なら **修正せず** ユーザーに報告する（挙動変更になるため）。

- [ ] **Step 4: テスト・ビルド・コミット**

```bash
./gradlew :app:testDebugUnitTest assembleDebug
git add -A && git commit -m "refactor: deduplicate quiz attempt recording in QuizRepository"
```

### Task 3.4: 解答マーカー定数の一元化

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/domain/model/QuizConstants.kt`
- Modify: `data/repository/QuizRepository.kt`（2箇所）、`ui/screen/quiz/SentenceQuizContent.kt`、`ui/screen/quiz/AddSentenceScreen.kt`

- [ ] **Step 1: `QuizConstants` に追加**

```kotlin
/** 穴埋め問題の空欄マーカー。要素数 = SENTENCE_ANSWER_COUNT。 */
val ANSWER_MARKERS = listOf("①", "②", "③", "④")
```

- [ ] **Step 2: 4ファイルのローカル定義を `QuizConstants.ANSWER_MARKERS` 参照へ置換**

各ファイルの `listOf("①", "②", "③", "④")` 相当を grep で特定して置換:

```bash
grep -rn "①" app/src/main --include="*.kt"
```

UI 側が「マーカー文字を表示テキストとして直接持っている」だけの箇所（リテラル単体）は対象外。リスト定義として重複している箇所のみ統一する。

- [ ] **Step 3: テスト・ビルド・コミット**

```bash
./gradlew :app:testDebugUnitTest assembleDebug
git add -A && git commit -m "refactor: centralize answer marker constants"
```

### Task 3.5: リポジトリの DAO 依存を分割 DAO へ移行（神 DAO の解体）

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/di/AppModule.kt`
- Modify: 各リポジトリ（`WordRepository.kt`, `LessonRepository.kt`, `ReviewRepository.kt`, `SettingsRepository.kt`, `CustomContentRepository.kt` など、`AppDao` 利用箇所すべて）
- Modify: `app/src/main/java/com/example/vocabapp/data/local/dao/AppDao.kt`

- [ ] **Step 1: AppModule に分割 DAO の @Provides を追加**

```kotlin
@Provides fun provideWordDao(db: AppDatabase): WordDao = db.wordDao()
@Provides fun provideLessonDao(db: AppDatabase): LessonDao = db.lessonDao()
@Provides fun provideTrainingDao(db: AppDatabase): TrainingDao = db.trainingDao()
@Provides fun provideQuizDao(db: AppDatabase): QuizDao = db.quizDao()
@Provides fun provideReviewDao(db: AppDatabase): ReviewDao = db.reviewDao()
@Provides fun provideStudyLogDao(db: AppDatabase): StudyLogDao = db.studyLogDao()
@Provides fun provideUserProgressDao(db: AppDatabase): UserProgressDao = db.userProgressDao()
@Provides fun provideCustomContentDao(db: AppDatabase): CustomContentDao = db.customContentDao()
@Provides fun provideAppSettingsDao(db: AppDatabase): AppSettingsDao = db.appSettingsDao()
```

- [ ] **Step 2: リポジトリを1つずつ、実際に使っているメソッドが属する分割 DAO への依存に書き換える**

例: `WordRepository` が `getWord` / `searchWords` 系しか使わないなら `AppDao` → `WordDao` に変更。1リポジトリ = 1コミット。複数 DAO にまたがるリポジトリ（`QuizRepository` など）は複数 DAO を注入してよい（それが本来の依存の姿）。

- [ ] **Step 3: `AppDao` に残す価値があるのは `@Transaction` 合成メソッド（`seedIfNeeded` / `resetLearningData` など）のみ**

全リポジトリ移行後、`AppDao` を `@Transaction` メソッド専用に縮小し、KDoc で「トランザクション合成専用。単発クエリは各 DAO を使うこと」と明記。

- [ ] **Step 4: 各コミット前にテスト・ビルド**

```bash
./gradlew :app:testDebugUnitTest assembleDebug
git commit -m "refactor: migrate WordRepository to WordDao"  # リポジトリごと
```

---

## フェーズ4: UI 層の重複排除・ハードコード除去

### Task 4.1: NavHost を MainActivity から抽出

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/ui/navigation/AppNavHost.kt`
- Modify: `app/src/main/java/com/example/vocabapp/MainActivity.kt`

- [ ] **Step 1: `MainActivity.kt` の `AppNav`（37-148行）を `ui/navigation/AppNavHost.kt` へ移動**

```kotlin
package com.example.vocabapp.ui.navigation

// (MainActivity にあった navigation/screen 系 import を全て移設。
//  フェーズ2完了後なので各画面の import が明示的に必要になる)

@Composable
internal fun AppNavHost(navController: NavHostController = rememberNavController()) {
    hiltViewModel<MainViewModel>()
    NavHost(navController = navController, startDestination = Route.Home.path) {
        // 既存の composable(...) 定義を一字一句そのまま移動
    }
}
```

`MainActivity` は以下だけになる:

```kotlin
setContent { VocabTheme { AppNavHost() } }
```

- [ ] **Step 2: テスト・ビルド・エミュレータで主要遷移を確認・コミット**

```bash
./gradlew :app:testDebugUnitTest assembleDebug
git add -A && git commit -m "refactor: extract AppNavHost from MainActivity"
```

### Task 4.2: PassagePracticeScreen のフィクスチャ既定値を廃止

**Files:**
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/passage/PassagePracticeScreen.kt:95`
- Modify: `app/src/main/java/com/example/vocabapp/ui/screen/quiz/QuizScreen.kt:28`

- [ ] **Step 1: デフォルト引数を外し、必須引数にする**

```kotlin
internal fun PassagePracticeScreen(
    navController: NavHostController,
    sets: List<PassageSet>,
    modifier: Modifier = Modifier
)
```

- [ ] **Step 2: `QuizScreen.kt:28` で明示的に渡す（現挙動を維持）**

```kotlin
// 長文モードの正式なデータソース接続までは登録済みフィクスチャを表示する（従来挙動）
PassagePracticeScreen(navController, sets = PassagePracticeFixtures.sets)
```

`CustomPassageListScreen` は既に sets を渡しているので変更不要。`PassagePracticeFixtures` に「本番データソース接続後に debug ソースセットへ移動する」旨の KDoc を追記。

- [ ] **Step 3: テスト・ビルド・両画面の目視確認・コミット**

```bash
git add -A && git commit -m "refactor: make passage sets an explicit parameter"
```

### Task 4.3: 長文系画面のハードコード色をテーマへ集約

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/ui/theme/PassageColors.kt`
- Modify: `ui/screen/passage/PassagePracticeScreen.kt`（`Color(0x...)` 22箇所）
- Modify: `ui/screen/passage/PassageReviewModels.kt`（5箇所）ほか passage 配下

- [ ] **Step 1: 色の棚卸し**

```bash
grep -rhoE "Color\(0x[0-9A-Fa-f]+\)" app/src/main/java/com/example/vocabapp/ui/screen/passage | sort | uniq -c | sort -rn
```

- [ ] **Step 2: `PassageColors.kt` に意味名で定義**

```kotlin
package com.example.vocabapp.ui.theme

import androidx.compose.ui.graphics.Color

// 長文問題（TOEIC 風紙面）専用の配色。Material テーマ外の意匠のためトークン化して管理する。
val PassageBlue = Color(0xFF168BEF)
val PassageRuleGray = Color(0xFFD4DEE5)
val PassageChoiceGray = Color(0xFF8797A1)
val PassageDotInactive = Color(0xFFEAF0F2)
val PassageInkBlack = Color(0xFF202020)
val PassageLabelPurple = Color(0xFF8C72E8)
// 棚卸しで出た残りの色も同様に意味名を付けて定義する
```

- [ ] **Step 3: 各画面のローカル `private val` と `Color(0x...)` 直書きを置換**（同一 hex は同一トークンへ）

- [ ] **Step 4: ビルド・**エミュレータで長文画面のレンダリング比較**・コミット**

```bash
git add -A && git commit -m "refactor: tokenize passage screen colors into theme"
```

### Task 4.4: CustomWord / CustomIdiom の一覧画面・VM 重複を統合

**Files:**
- Create: `app/src/main/java/com/example/vocabapp/ui/screen/custom/CustomEntryListContent.kt`（共通リスト UI）
- Modify: `CustomWordListScreen.kt` / `CustomIdiomListScreen.kt`（薄いラッパー化）
- Modify: `viewmodel/CustomIdiomListViewModel.kt` / `CustomWordListViewModel.kt`

- [ ] **Step 1: 2画面の diff を取り、共通部分と差分（favorite/learned トグルの有無）を確定**

```bash
diff app/src/main/java/com/example/vocabapp/ui/screen/custom/CustomWordListScreen.kt \
     app/src/main/java/com/example/vocabapp/ui/screen/custom/CustomIdiomListScreen.kt
```

- [ ] **Step 2: 共通 UI を抽出**

```kotlin
@Composable
internal fun CustomEntryListContent(
    title: String,
    entries: List<CustomEntryUi>,        // english / meaning / id を持つ共通表示モデル
    onDelete: (Int) -> Unit,
    // 単語のみが持つ操作は nullable コールバックで表現し、null なら UI を出さない
    onToggleFavorite: ((Int, Boolean) -> Unit)? = null,
    onToggleLearned: ((Int, Boolean) -> Unit)? = null,
    onBack: () -> Unit
) { /* 既存2画面の共通レイアウトを移植。LazyColumn には key = entry.id を必ず指定 */ }
```

既存2画面は state 収集と画面固有文言だけを持つ薄いラッパーにする。VM は無理に1本化せず、共通ロジック（一覧購読 + delete）を基底クラスまたは委譲に寄せる（favorite/learned は Word 側のみ）。

- [ ] **Step 3: テスト・ビルド・両画面の目視確認・コミット**

```bash
git add -A && git commit -m "refactor: share custom entry list UI between word and idiom screens"
```

### Task 4.5: 結果画面4系統を共通部品（CommonResultContent / ResultContentBody）へ収束

**Files:**
- Modify: `ui/screen/quiz/ResultScreen.kt`(265行) / `SentenceResultContent.kt`(253行)
- Reference: `ui/screen/common/CommonResultContent.kt` / `CommonResultSections.kt` / `ResultAccuracyCard.kt`

- [ ] **Step 1: 4実装を並べて「スコアカード・正解/不正解統計・リトライ/ホームボタン」の共通骨格と画面固有部（間違い単語一覧、文章レビュー等）を仕分けする**

- [ ] **Step 2: 共通骨格を `CommonResultSections` の既存部品で置き換え、画面固有部は slot（`content: @Composable () -> Unit`）として注入する形に統一**

- [ ] **Step 3: `PassageResultReviewScreen` は意匠が大きく異なる場合は対象外とし、判断理由をコミットメッセージに残す（無理な共通化はしない）**

- [ ] **Step 4: テスト・ビルド・各結果画面の目視確認・コミット**

```bash
git add -A && git commit -m "refactor: converge quiz result screens onto common result sections"
```

### Task 4.6: ハードコード日本語文字列の strings.xml 移行（画面単位で段階実施）

**Files:**
- Modify: `app/src/main/res/values/strings.xml` / `app/src/main/res/values-en/strings.xml`
- Modify: `ui/` 配下の各画面（約319リテラル）

**方針:** 1画面 = 1コミット。優先順: ① passage 系（最多） ② quiz 系 ③ custom 系 ④ 残り。

- [ ] **Step 1: 対象画面のリテラルを列挙**（例: passage）

```bash
grep -rnoE '"[^"]*[ぁ-んァ-ヶ一-龯][^"]*"' \
  app/src/main/java/com/example/vocabapp/ui/screen/passage --include="*.kt"
```

- [ ] **Step 2: strings.xml にキー追加 → `stringResource` 置換**（変換例）

```xml
<!-- values/strings.xml -->
<string name="passage_submit_all">解答する</string>
<string name="passage_document_collapsed">本文を閉じています</string>
<string name="passage_close_question">問題を閉じる</string>
<string name="passage_open_question">問題を開く</string>
```

```kotlin
// 変更前
Text("解答する", fontSize = 20.sp, fontWeight = FontWeight.Black)
// 変更後
Text(stringResource(R.string.passage_submit_all), fontSize = 20.sp, fontWeight = FontWeight.Black)
```

values-en にも対応する英訳を同時に追加する（既存の values-en 運用を維持）。`%d` 等のプレースホルダが必要な文字列（`"セット $setPosition/$setCount"` など）は `<string name="...">セット %1$d/%2$d</string>` + `stringResource(R.string.x, a, b)` 形式にする。Composable 外（ViewModel 等）の文字列は `UiText`/resource id 化が必要になり影響が大きいため、**このタスクでは Composable 内のリテラルのみ対象** とし、VM 内文言はフェーズ5の方針決定に委ねる。

- [ ] **Step 3: 画面ごとにビルド・目視確認・コミット**

```bash
git commit -m "refactor: extract passage screen strings to resources"
```

- [ ] **Step 4: 完了判定**

```bash
grep -rhoE '"[^"]*[ぁ-んァ-ヶ一-龯][^"]*"' app/src/main/java/com/example/vocabapp/ui --include="*.kt" | wc -l
```

目標: 0（contentDescription 含む）。テスト用 fixture 文言は対象外。

---

## フェーズ5: アーキテクチャ方針統一 + ガードレール

### Task 5.1: ViewModel ↔ UseCase / Repository の依存方針を決めて文書化

**Files:**
- Modify: `CLAUDE.md`（Architecture 節に追記）
- Modify: 該当 ViewModel（監査結果に応じて）

- [ ] **Step 1: 現状監査** — 21 VM それぞれについて「UseCase 経由 / Repository 直 / 混在」を一覧化

```bash
grep -l "UseCase" app/src/main/java/com/example/vocabapp/viewmodel/*.kt
grep -l "Repository" app/src/main/java/com/example/vocabapp/viewmodel/*.kt
```

- [ ] **Step 2: ルールを採択し CLAUDE.md に明記**

推奨ルール（実利優先・全面 UseCase 化はしない）:

```markdown
## Layering Rule
- 複数 Repository をまたぐ処理・採点等のドメインロジックは UseCase に置き、ViewModel は UseCase に依存する。
- 単一 Repository への単純な委譲（CRUD・Flow購読のみ）は ViewModel → Repository 直依存を許可する。
- 中身が1行委譲だけのパススルー UseCase は作らない・既存のものは削除する。
```

- [ ] **Step 3: ルールに照らして逸脱を是正**
  - パススルーだけの UseCase（監査で特定）→ 削除し VM を Repository 直依存へ。
  - ドメインロジックが VM/Repository に漏れている箇所（例: 採点・進捗判定）→ 既存 UseCase へ移動。
  - 1是正 = 1コミット。各コミット前にテスト・ビルド。

### Task 5.2: 静的解析ガードレールの導入（再発防止）

**Files:**
- Modify: `build.gradle.kts` / `app/build.gradle.kts` / `gradle/libs.versions.toml`
- Create: `config/detekt/detekt.yml`

- [ ] **Step 1: detekt を導入**

`gradle/libs.versions.toml`:

```toml
[versions]
detekt = "1.23.8"
[plugins]
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

`app/build.gradle.kts` にプラグイン適用と設定:

```kotlin
plugins {
    alias(libs.plugins.detekt)
}
detekt {
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    baseline = file("$rootDir/config/detekt/baseline.xml")
}
```

- [ ] **Step 2: 再発防止に効くルールを有効化**

`config/detekt/detekt.yml`（要点のみ。残りはデフォルト）:

```yaml
naming:
  InvalidPackageDeclaration:   # パッケージ偽装の再発防止（フェーズ2の成果を守る）
    active: true
complexity:
  LargeClass:
    active: true
    threshold: 350             # QuizRepository 級の神クラス再発防止
  LongMethod:
    active: true
style:
  UnusedPrivateMember:
    active: true               # デッドコード再発防止
  MagicNumber:
    active: false              # Compose の dp/sp で誤検知が多いため off
```

- [ ] **Step 3: 既存違反は baseline に固定し、新規違反のみ fail にする**

```bash
./gradlew :app:detektBaseline
./gradlew :app:detekt
```

Expected: BUILD SUCCESSFUL（baseline 適用後）

- [ ] **Step 4: コミット**

```bash
git add -A && git commit -m "build: add detekt with package and dead-code guards"
```

---

## フェーズ6: 最終検証

- [ ] **Step 1: クリーンビルド + 全テスト**

```bash
./gradlew clean :app:testDebugUnitTest assembleDebug
```

Expected: BUILD SUCCESSFUL / 全テスト PASS

- [ ] **Step 2: 実機/エミュレータ検証（`.claude/prompts/deploy.md` の手順厳守）**

```bash
adb kill-server && adb start-server
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

スモーク対象（変更が及んだ全フロー）:
1. ホーム → レッスン → クイズ → 結果 → リトライ
2. 復習クイズ
3. カスタム単語一覧 / カスタム熟語一覧（追加・削除・お気に入り）
4. 文章クイズ（ブラケットあり/なしの問題生成）
5. 長文問題（QuizScreen 経由のフィクスチャ表示・カスタム長文の登録→出題→結果）
6. 設定 → 学習データリセット
7. CSV / XLSX インポート

- [ ] **Step 3: 成果の計測と報告**

```bash
find app/src/main -name "*.kt" | xargs wc -l | tail -1   # フェーズ0比の削減行数
# インベントリ A の検出コマンド → 出力ゼロ
# インベントリ D の日本語リテラル grep → 0
```

- [ ] **Step 4: ブランチ統合**

superpowers:finishing-a-development-branch に従い、PR 作成（base: main）またはマージ方針をユーザーに確認する。

---

## やらないこと（明示的スコープ外）

- 機能追加・画面追加・挙動変更（`QuizRepository.kt:163` の trainingId 問題も**調査と報告のみ**）
- Room スキーマ変更・マイグレーション追加（現13本のマイグレーションは正しく管理されており触らない）
- パッケージ名 `com.example.vocabapp` 自体の変更（CLAUDE.md 記載の `teppenenglish` への改名は別案件）
- マルチモジュール化（単一モジュールで問題ない規模）
- Compose のパフォーマンスチューニング（recomposition 計測で問題が出た場合のみ別タスク化）

---

# 付録: AI エージェント実行指示書（Claude Code / Codex 用）

この付録は、本計画の各フェーズを AI コーディングエージェントに実行させるための指示書である。**エージェントに作業を依頼するときは、後述の「フェーズ別実行プロンプト」をそのままコピーして渡すこと。**

## A. 全エージェント共通プロトコル（必読・違反禁止）

### A-1. 作業の流れ

1. 作業開始前に、リポジトリルートの `stusap_fix.md`（この文書）と `CLAUDE.md` を**全文読む**。
2. 指示されたタスク番号（例: Task 3.2）のセクションを読み、**Step を上から順に1つずつ**実行する。Step の飛ばし・並べ替え・まとめ実行は禁止。
3. Step を完了するたびに、この文書内の該当チェックボックス `- [ ]` を `- [x]` に書き換えてよい（進捗の永続化のため推奨）。
4. タスク完了の定義は「全 Step のチェック + 検証ゲート通過 + コミット済み」。これを満たすまで完了報告をしてはならない。

### A-2. 検証ゲート（コミット前に毎回必須）

```bash
./gradlew :app:testDebugUnitTest assembleDebug
```

- Expected: `BUILD SUCCESSFUL`。これが出力されたログを確認してからコミットする。**ログを確認せずに「成功したはず」でコミットすることを禁止する。**
- ビルドが `error: cannot find symbol` や `Could not load module <Error module>` で落ちる場合は、先に以下を試す（既知の kapt/K2 問題）:

```bash
./gradlew clean assembleDebug -Pkapt.use.k2=true
```

- UI に触れたタスク（フェーズ1・4・6）は、加えて `.claude/prompts/deploy.md` の手順でエミュレータにインストールし、変更画面を起動して目視確認する。「修正したのに画面が変わらない」場合は古い APK の残存を疑い、`./gradlew clean assembleDebug` からインストールし直す。
- エミュレータ/adb が使えない環境（Codex のサンドボックス等）では、UI 目視確認 Step を**スキップ完了扱いにせず**、完了報告に「未実施: エミュレータ確認（環境制約）。ユーザーによる確認が必要」と明記する。

### A-3. 禁止事項

- **挙動変更**。本計画は全タスクが behavior-preserving リファクタリングである。出力・画面表示・保存データ・画面遷移が1ビットでも変わる変更に気づいたら、手を止めてユーザーに報告する。
- テストを**変更・削除して green にする**こと。テストが落ちたら直すのはプロダクションコード側か、自分が今書いた新規テストのみ。既存テストの期待値変更が必要になった時点でそれは挙動変更なので停止・報告。
- `@Suppress` / `TODO` / `FIXME` を残したままタスクを完了扱いにすること。
- 指示されたタスクの範囲外のファイルを「ついでに」修正すること（気づいた問題は報告に書く。直さない）。
- `git push --force`、`git reset --hard`、main ブランチへの直接コミット。
- 同じビルドエラー・テスト失敗に対して**2回**修正を試みて解決しなかった場合、3回目の修正を試みること。CLAUDE.md の「Bug Investigation Rules」に従い、エラーメッセージ全文・試した仮説・検証結果を添えて停止・報告する。

### A-4. Git 規約

- 作業ブランチ: `refactor/stusap-fix`（フェーズ0で作成済みのはず。なければ Task 0.1 から実行）。
- 1タスク内で指示されたコミット粒度を守る（複数コミット指定のタスクを1コミットに潰さない）。
- コミットメッセージは各タスクの Step に記載のものをそのまま使う。記載がない場合は `refactor: <変更内容を英語で>` 形式。
- コミット前に `git status` と `git diff --stat` を実行し、**意図しないファイルが混ざっていないこと**を確認する。

### A-5. 完了報告フォーマット（タスクごと）

```text
## Task <番号> 完了報告
- 実施した Step: <チェックリストの状態>
- 変更ファイル: <git diff --stat の出力>
- 検証結果: testDebugUnitTest = PASS/FAIL(全文), assembleDebug = SUCCESS/FAIL
- エミュレータ確認: 実施済み(確認した画面と操作) / 未実施(理由)
- コミット: <ハッシュとメッセージ>
- スコープ外で気づいた問題（あれば。修正はしていないこと）:
- 判断に迷った点・ユーザー確認が必要な点（あれば）:
```

## B. エージェント別の注意

### B-1. Claude Code に依頼する場合

- `superpowers:executing-plans`（このセッションで順次実行）または `superpowers:subagent-driven-development`（タスクごとにサブエージェント）スキルを使って本計画を実行すること。
- コミット前検証は `superpowers:verification-before-completion` に従う。
- エミュレータ確認は `.claude/prompts/deploy.md` の手順をそのまま使える。

### B-2. Codex に依頼する場合

- 本計画書のコマンドはすべて zsh/bash 前提。サンドボックスでネットワーク・adb が使えない場合があるため、**Gradle 依存解決が必要なタスク（Task 5.2 の detekt 導入）はネットワークアクセスを許可した状態で実行**すること。
- Codex には Skill ツールがないため、A 節のプロトコルがスキルの代替である。A 節を逸脱しないこと。
- エミュレータ確認ができない場合は A-2 の最終項に従い「未実施」として明示報告する。

### B-3. 並行実行の可否（2エージェントに同時に振る場合）

| 同時実行 | 可否 | 理由 |
|---|---|---|
| フェーズ2の複数バッチ | **不可** | 全バッチが MainActivity / 画面間 import に触れるため衝突する |
| Task 3.1〜3.5 同士 | **不可** | すべて QuizRepository / QuizConstants / AppModule を共有 |
| フェーズ3 と フェーズ4 | **条件付き可** | git worktree でブランチを分ければ可。ただし Task 3.4（QuizConstants）と Task 4 系で `SentenceQuizContent.kt` / `AddSentenceScreen.kt` が重なるため、Task 3.4 完了後に分岐すること |
| Task 4.6 の画面別文字列移行 | **可** | 画面ディレクトリが異なれば衝突しない（strings.xml は追記競合に注意。1画面ずつ rebase） |

並行させる場合は各エージェントに `git worktree add` で独立した作業ツリーを与え、統合は人間（またはリードエージェント1体）が rebase で行う。迷ったら**直列実行**を選ぶこと。

## C. フェーズ別実行プロンプト（コピペ用）

以下の各ブロックを、そのままエージェントへの依頼文として貼り付ける。`<...>` は貼り付け時に置き換える。

### C-0. フェーズ0（ベースライン確立）

```text
リポジトリ /Users/user/Desktop/pgfile/stusap で作業してください。
まずルートの stusap_fix.md と CLAUDE.md を全文読んでください。

stusap_fix.md の「フェーズ0」(Task 0.1, Task 0.2) を Step どおりに実行してください。
- 付録A「全エージェント共通プロトコル」を厳守すること。
- Task 0.1 Step 1 で未追跡ファイル app/src/test/java/com/example/vocabapp/viewmodel/CustomPassageRegistrationViewModelTest.kt
  を見つけた場合は、勝手にコミット・削除せず、扱いを私（ユーザー）に確認すること。
- Task 0.2 Step 1 で既存テストが1件でも落ちた場合は、修正せずに失敗ログ全文を報告して停止すること。
完了したら付録 A-5 のフォーマットで報告してください。
```

### C-1. フェーズ1（デッドコード削除）

```text
リポジトリ /Users/user/Desktop/pgfile/stusap、ブランチ refactor/stusap-fix で作業してください。
stusap_fix.md と CLAUDE.md を全文読み、stusap_fix.md の Task 1.1 と Task 1.2 を Step どおりに実行してください。

厳守事項:
- 削除してよいのは「grep で参照ゼロを確認できた宣言」のみ。確認コマンドの出力を報告に含めること。
- PassagePracticeScreen.kt の formatClock は使用中なので削除しないこと。
- @Preview 付き Composable と Hilt エントリポイント（@HiltViewModel, @AndroidEntryPoint 等）は
  参照ゼロに見えても削除対象外。
- 1つでも判断に迷う宣言があれば、削除せずリストアップして報告すること。
- 各タスク完了ごとに ./gradlew :app:testDebugUnitTest assembleDebug を通してからコミット。
完了したら付録 A-5 のフォーマットで報告してください。
```

### C-2. フェーズ2（パッケージ構造の正常化）

```text
リポジトリ /Users/user/Desktop/pgfile/stusap、ブランチ refactor/stusap-fix で作業してください。
stusap_fix.md と CLAUDE.md を全文読み、stusap_fix.md の Task 2.1 → Task 2.2〜2.5 をバッチ順
（data/imports → ui/screen/word + lesson → custom → quiz → 残り）に実行してください。

厳守事項:
- 1バッチ = 1コミット。バッチをまたいで一括変更しないこと（コンパイルエラーの切り分けができなくなる）。
- ファイルの移動は必ず git mv を使うこと（履歴を切らない）。
- パッケージ宣言の変更とそれに伴う import 追加・修正以外のコード変更は一切禁止
  （フォーマット修正・名前変更・ロジック変更を混ぜない）。
- data/import → data/imports はテストディレクトリ app/src/test/java/com/example/vocabapp/data/import も
  同時に git mv + パッケージ修正すること。
- 各バッチ後に ./gradlew :app:compileDebugKotlin で未解決参照を列挙し、import を足して解決すること。
  推測で import を書かず、コンパイラのエラーメッセージに従うこと。
- 最終 Step として stusap_fix.md の「0. インベントリ A」記載の検出コマンドを実行し、
  出力ゼロのログを報告に含めること。
完了したら付録 A-5 のフォーマットで報告してください。
```

### C-3. フェーズ3（データ層の分離・重複排除）

```text
リポジトリ /Users/user/Desktop/pgfile/stusap、ブランチ refactor/stusap-fix で作業してください。
前提: フェーズ2が完了していること（インベントリAの検出コマンドが出力ゼロ）。未完了なら停止して報告。

stusap_fix.md と CLAUDE.md を全文読み、Task 3.1 → 3.4 → 3.2 → 3.3 → 3.5 の順に実行してください
（3.4 を 3.2 より先にやるのは、SentenceQuestionFactory が QuizConstants.ANSWER_MARKERS を参照するため）。

厳守事項:
- Task 3.2 は TDD。Step 0（QuizRuntime の interface 化）→ Step 1（テスト作成）→ Step 2（FAIL 確認）
  → Step 3（実装移植）→ Step 4（PASS 確認）の順を崩さないこと。FAIL 確認をスキップしない。
- Task 3.2 の実装は stusap_fix.md に全文が書いてある。ロジックを「改善」せず一字一句移植すること。
  シャッフル順・乱数の使い方が1箇所でも変わると挙動変更になる。
- Task 3.3 Step 2 の注意書き（finishSentenceQuiz の trainingId/lessonId 非対称）を必ず読むこと。
  recordAttempt で表現できない場合は finishSentenceQuiz を置換対象から外してよい。
- Task 3.3 Step 3（QuizRepository.kt:163 の調査）は git log で調査して結論を報告するだけ。修正禁止。
- Task 3.5 は 1リポジトリ = 1コミット。すべてのコミット前に検証ゲートを通すこと。
完了したら付録 A-5 のフォーマットで、タスクごとに分けて報告してください。
```

### C-4. フェーズ4（UI 層の重複排除・ハードコード除去）

```text
リポジトリ /Users/user/Desktop/pgfile/stusap、ブランチ refactor/stusap-fix で作業してください。
前提: フェーズ2が完了していること。未完了なら停止して報告。

stusap_fix.md と CLAUDE.md を全文読み、Task 4.1 → 4.2 → 4.3 → 4.4 → 4.5 → 4.6 の順に実行してください。

厳守事項:
- 全タスクが UI 変更を伴うため、各タスクのコミット前に .claude/prompts/deploy.md の手順で
  エミュレータにインストールし、変更画面を目視確認すること（CLAUDE.md の UI Verification Rules）。
  adb が使えない環境なら、その旨を報告に明記し「未確認」としてコミットメッセージにも (unverified UI) を付けること。
- レイアウト・色・文言の見た目を1pxも変えないこと。Task 4.3 は同一 hex 値を同一トークンに
  置き換えるだけ。「ついでに色を整理」することを禁止する。
- Task 4.4 で CustomWordListScreen と CustomIdiomListScreen の差分（favorite/learned の有無）を
  消さないこと。共通化するのは同一部分のみ。LazyColumn には必ず key を指定すること。
- Task 4.5 で PassageResultReviewScreen の共通化が不自然になる場合は対象外とし、理由を報告すること。
  無理な共通化より重複の方がまし。
- Task 4.6 は 1画面 = 1コミット。values/strings.xml と values-en/strings.xml を必ずセットで更新すること。
  英訳は既存 values-en の文体に合わせる。プレースホルダは %1$d 形式を使うこと。
  ViewModel 内の文字列は対象外（Composable 内のリテラルのみ）。
完了したら付録 A-5 のフォーマットで、タスクごとに分けて報告してください。
```

### C-5. フェーズ5（方針統一 + detekt 導入）

```text
リポジトリ /Users/user/Desktop/pgfile/stusap、ブランチ refactor/stusap-fix で作業してください。
前提: フェーズ3・4が完了していること。未完了なら停止して報告。

stusap_fix.md と CLAUDE.md を全文読み、Task 5.1 と Task 5.2 を実行してください。

厳守事項:
- Task 5.1 Step 1 の監査結果は「VM名 / 依存先 / 分類（UseCase経由・Repository直・混在）/
  パススルーUseCaseか否か」の表にして、是正に着手する前に私（ユーザー）に提示し承認を得ること。
  承認前に ViewModel や UseCase を削除・変更してはならない。
- Task 5.2 の detekt 導入はネットワークアクセスが必要。依存解決に失敗したらプロキシ設定等を
  推測でいじらず停止して報告すること。
- detekt の baseline 生成後、./gradlew :app:detekt が BUILD SUCCESSFUL になるログを報告に含めること。
- detekt の指摘を理由に baseline 外のコードを「ついでに」修正しないこと。
完了したら付録 A-5 のフォーマットで報告してください。
```

### C-6. フェーズ6（最終検証）

```text
リポジトリ /Users/user/Desktop/pgfile/stusap、ブランチ refactor/stusap-fix で作業してください。
前提: フェーズ0〜5の全タスクが完了済み（stusap_fix.md のチェックボックスで確認）。

stusap_fix.md のフェーズ6を Step どおりに実行してください。

厳守事項:
- スモークテストは記載の7フローすべてを実機/エミュレータで実施し、フローごとに OK/NG を記録すること。
- NG が1件でもあれば、修正に着手する前に再現手順とログを報告すること
  （CLAUDE.md の Bug Investigation Rules に従う。場当たり修正禁止）。
- Step 3 の計測結果（削減行数・インベントリ検出コマンドの出力ゼロ確認）を報告に含めること。
- Step 4 のブランチ統合（PR作成 or マージ）は必ず私（ユーザー）に方針を確認してから実行すること。
完了したら付録 A-5 のフォーマットで報告してください。
```

## D. リードエージェント（オーケストレーター）への指示

1人のエージェントに全フェーズを管理させる場合は、以下を渡す:

```text
stusap_fix.md の計画全体のオーケストレーターを務めてください。
- フェーズ0から順に、付録Cの該当プロンプトの内容でフェーズを1つずつ実行（またはサブエージェントに委任）する。
- フェーズ間で必ず停止し、A-5 フォーマットの報告を私に提示して続行承認を得る。
- 付録 B-3 の並行実行マトリクスに違反する並行化をしない。
- 任意のフェーズで停止条件（A-3 最終項）に達したら、以降のフェーズに進まず全体を停止する。
- stusap_fix.md のチェックボックスを進捗に合わせて更新し、常に文書と実態を一致させる。
```

