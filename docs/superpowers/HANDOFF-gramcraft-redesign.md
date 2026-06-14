# 引き継ぎ指示書 — GramCraft 全画面デザイン統一 + ロボット演出

> 中断日: 2026-06-13 / ブランチ: `passage-bulk-submit`
> 次回 Codex / Claude が **ここから続きを実装** するための指示書。
> 関連: 仕様 `.claude/specs/2026-06-13-gramcraft-redesign.md` / 実装計画 `docs/superpowers/plans/2026-06-13-gramcraft-redesign.md`

---

## 最新追記（Claude / 2026-06-14 コミット完了）— ★次回はここから読む

ユーザー依頼「続きを頼む」で再開。GramCraft 全画面統一は build+test グリーン・画面内 hardcoded color ゼロまで到達済みだったため、**未コミット差分をフェーズ別に6コミットへ確定した**（ユーザー選択）。コミット前に `assembleDebug testDebugUnitTest` = BUILD SUCCESSFUL（UP-TO-DATE）を確認。

### 確定したコミット（このセッション）
1. `0535782` foundation — theme tokens / mascot motion / confetti / GramComponents / 画像 / strings
2. `a90fa88` 共通 scaffold + quiz/result 共通部品
3. `66dafda` Phase A — home / lesson lists / settings
4. `963fad6` Phase B — 一覧 / 登録 / インポート
5. `cdca696` Phase C — quiz / result / passage / flashcard / review / training
6. （本コミット）docs — この HANDOFF と `stusap_fix.md`

### 状態
- ブランチ `passage-bulk-submit`、**push はしていない**（ローカルコミットのみ）。
- 目視確認はユーザー方針どおりユーザー側で実施する（このホストのエミュは SystemUI ANR で不可）。
- 機能・ナビ・保存ロジックは未変更。見た目のみ。

### 次回の候補
- ユーザー目視 OK なら main への PR / マージ（`superpowers:finishing-a-development-branch` 参照）。
- 目視で要調整が出たら該当画面のみ修正。

---

## 最新追記（Codex / 2026-06-14 hardcoded color 完了）

ユーザー依頼「続きをたのむよ」で再開。**コミットはしていない。** 前回最後に残っていた `PassagePracticeScreen.kt` の Article / Notice / Email 紙面再現用 `Color(0x...)` を theme token 化した。

### 今回進めたこと

- `VocabTheme.kt`
  - 長文紙面再現用 token を追加。
  - `PassagePaperInk`
  - `PassagePaperBorder`
  - `PassageEmailChrome`
  - `PassageEmailBorder`
  - `PassageEmailField`
  - `PassageEmailFieldBorder`
- `PassagePracticeScreen.kt`
  - Article / Notice / Email の `Color(0x...)` と `Color.Black` を上記 token へ置換。
  - 色値は維持しているため、紙面表現の見た目は変えずに画面側の direct hex を排除。

### 検証結果

- `rg -n "Color\\(0x" app/src/main/java/com/example/vocabapp/ui/screen -g '*.kt'` は **ヒットなし**。
- `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` は **BUILD SUCCESSFUL**（52 tasks、14 executed）。
- 既存の KAPT 警告は継続。今回変更によるビルド/テスト失敗はなし。
- エミュレータ目視は過去追記どおり SystemUI ANR 前提のため未実施。

### 次回の候補

- UI 変更が大きく未コミット差分も多いため、次は差分レビューと必要なら論理単位コミット。
- 目視はユーザー方針どおりユーザー側確認。こちらで続けるなら `git diff --stat` / `git diff` を見て不要 import や明らかな重複だけ整理し、再度 `assembleDebug testDebugUnitTest`。
- 画面配下の direct hex は消えているが、theme file には token 定義として `Color(0x...)` が残る。これは意図どおり。

---

## 最新追記（Codex / 2026-06-14 追加継続分）— ★次回はここから読む

ユーザー依頼「続きをたのむ」で再開。**コミットはしていない。** 前回残っていた長文結果レビュー系と共通部品の hardcoded color を追加で整理した。

### 今回進めたこと

- `PassageResultReviewScreen.kt`
  - 背景を `BrightBlue` から `SoftBlue` へ変更。
  - ヘッダーの紫 `Color(0xFF8C72E8)` を `BrightBlue` へ変更。
  - 区切り線を `DeepBlue` から `BrightBlue` へ変更。
  - 「やり直す」「問題文を確認/閉じる」「次へ/ホームへ」を `GramSecondaryButton` / `GramPrimaryButton` 化。
  - review index / next / home / document toggle のロジックは変更していない。
- `PassageReviewModels.kt`
  - `PassageReviewCorrect/Wrong/SectionFill/SectionLine/Muted` を `Success` / `Danger` / `SoftBlue` / `TextMuted` 由来に変更。
- `PassageReviewCard.kt`
  - 外枠を `GramCard` 化。
  - 不正解セクション背景 `Color(0xFFFCEBF6)` を `SoftBlue` へ変更。
- `PassageReviewSelector.kt`
  - チェック枠線 `Color(0xFFD0D8DE)` を `SoftBlue` へ変更。
- `CommonScaffold.kt`
  - TopAppBar `Color(0xFF0F7F45)` を `DeepBlue` へ変更。
- `HomeScreen.kt`
  - Home hero 背景 `Color(0xFFDDF7E5)` を `SoftBlue` へ変更。
- `CommonInputComponents.kt`
  - AndroidView の EditText stroke `Color(0xFFB0BEC5)` を `TextMuted` へ変更。

### 検証結果

- 変更後に `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を実行し、**BUILD SUCCESSFUL**（52 tasks、14 executed）。
- 既存の KAPT 警告は継続。今回変更によるビルド/テスト失敗はなし。
- エミュレータ目視は過去追記どおり SystemUI ANR 前提のため未実施。

### 残り

- `rg -n "Color\\(0x" app/src/main/java/com/example/vocabapp/ui/screen -g '*.kt'` の残りは `PassagePracticeScreen.kt` 内の Article / Notice / Email の紙面再現用カラーのみ。
- これらは長文問題の文書タイプ（記事/掲示/メール）を実物風に見せるための局所表現なので、最終ルールを厳密に満たすなら `PassagePaperInk` / `PassagePaperBorder` / `PassageEmailChrome` などの theme token を追加して置換する。

---

## 最新追記（Codex / 2026-06-14 継続分）— ★次回はここから読む

ユーザー依頼「続きをたのむ」で再開。**コミットはしていない。** 前回追記で残っていた `PassagePracticeScreen.kt`、フラッシュカード、復習/学習ログ、トレーニング一覧系を追加で GramCraft 寄せした。

### 今回進めたこと

- `PassagePracticeScreen.kt`
  - 外側背景を `SoftBlue` 化。
  - 旧 `PassageBlue` / `RuleGray` / `ChoiceGray` / `ProgressDotInactive` を theme token 由来へ変更。
  - 上部ラベルを `BrightBlue`、区切り線も `BrightBlue` に変更。
  - 問題本文エリアを `GramCard` 化。
  - 下部の「解答する」を `GramPrimaryButton` 化し、disabled 色も theme token へ変更。
  - ファイル内の旧 `PassageResultScreen` も `SoftBlue` 背景 + `AnimatedMascot(Cheer)` + `GramCard` + `GramPrimaryButton/GramSecondaryButton` へ変更。
  - 長文本文/Email/Notice の紙面表現は崩すリスクが高いため、文書内の黒/罫線/グレー系表現は一部残している。ロジックは未変更。
- `FlashcardScreen.kt`
  - `AnimatedMascot(Thinking/Cheer)` を追加。
  - 単語カードを `GramCard` 化。
  - 前へ/次へを `GramSecondaryButton` / `GramPrimaryButton` 化。
  - 自動読み上げ、タップで意味表示、前後移動の ViewModel 呼び出しは維持。
- `ReviewScreen.kt`
  - 復習画面背景を `SoftBlue` 化。
  - `AnimatedMascot(Thinking)` を追加。
  - 復習クイズ開始を `GramPrimaryButton` 化。
  - 学習ログ行を `GramCard` 化。
  - 復習対象削除 / WordDetail 遷移 / StudyLog 表示は維持。
- `TrainingListScreen.kt`
  - 背景を `SoftBlue` 化。
  - 既存 `TrainingCard` 導線は維持。
- `CustomTrainingListScreen.kt`
  - 一覧とブロック画面の背景を `SoftBlue` 化。
  - 登録 / 登録一覧 / インポート導線を `GramPrimaryButton` / `GramSecondaryButton` 化。
  - 100問ブロック / 10問セット / quiz/detail/flashcard 導線は維持。
- `RandomCustomQuizScreen.kt`
  - メニューと空状態の背景を `SoftBlue` 化。
- `SentenceResultContent.kt`
  - 背景を `SoftBlue` 化。
  - 結果カードを `GramCard` 化。
  - progress track の `Color(0xFFDDE5EC)` を `SoftBlue` へ変更。
  - もう一度 / メニュー / ホームを Gram ボタン化。
- `StudyCards.kt`
  - `LessonCard` / `TrainingCard` を `GramCard` 化。
  - 未挑戦 star の `Color(0xFFDDE5EC)` を `SoftBlue` へ変更。
  - 未挑戦時の開始ボタンを `GramPrimaryButton` 化。

### 検証結果

- 変更後に `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を実行し、**BUILD SUCCESSFUL**（52 tasks、14 executed）。
- 既存の KAPT 警告は継続。今回変更によるビルド/テスト失敗はなし。
- エミュレータ目視は過去追記どおり SystemUI ANR 前提のため未実施。

### 現在の残り

- `PassagePracticeScreen.kt` 内の文書再現用 hardcoded color は一部残っている。見た目の紙面表現を維持するため残したが、最終 Z の「画面内 hardcoded color 空」まで厳密にやるなら theme token を追加して置換する。
- `PassageResultReviewScreen.kt` / `PassageReviewCard.kt` / `PassageReviewSelector.kt` / `PassageReviewModels.kt` はまだ旧色が残る。長文結果レビュー系として次に対応。
- `CommonScaffold.kt` の TopAppBar `Color(0xFF0F7F45)`、`HomeScreen.kt` の一部 `Color(0x...)`、`CommonInputComponents.kt` の Android View stroke 色も残る。最終クリーンアップ対象。
- 次回は `rg -n "Color\\(0x" app/src/main/java/com/example/vocabapp/ui/screen -g '*.kt'` の残りを、theme token へ移す作業から入るのがよい。

---

## 最新追記（Codex / 2026-06-14 深夜）— ★次回はここから読む

ユーザー依頼「HANDOFF-gramcraft-redesign.md の続きをやってほしい」で再開。**コミットはしていない。** 既存の未コミット差分を壊さず、前回追記で「次に対応」とされていたカンプ3/4相当の画面を追加で GramCraft 化した。

### 今回進めたこと

- `CustomPassageRegistrationScreen.kt`
  - カンプ3「長文問題登録」寄せに変更。
  - 背景を `SoftBlue`、上部に `AnimatedMascot(Wave)` + 吹き出しを追加。
  - 貼り付け形式 / 手入力 / 設題設定 / 登録済み設題 / プレビュー / ステータスカードを `GramCard` 化。
  - プレビュー / 保存 / 設題追加 / 問題設定完了の既存 ViewModel 呼び出しは維持し、ボタンだけ `GramSecondaryButton` / `GramPrimaryButton` に差し替え。
- `WordImportScreen.kt`
  - 通常時 `AnimatedMascot(Wave)`、成功時 `AnimatedMascot(Cheer)` を追加。Cheer の紙吹雪は `AnimatedMascot` 側で自動。
  - ファイル選択 / 登録 / 成功カードの見た目を `GramPrimaryButton` / `GramCard` / `GramSecondaryButton` へ寄せた。
  - ファイル読み込み、プレビュー、登録処理は変更していない。
- `SentenceImportScreen.kt` / `SentenceImportCards.kt`
  - 通常時 `AnimatedMascot(Wave)`、成功時 `AnimatedMascot(Cheer)` を追加。
  - ファイル選択 / 登録ボタンを `GramPrimaryButton` 化。
  - 成功カードを白い `GramCard` に変更し、一覧/ホーム導線は既存のまま維持。
- `CommonQuizContent.kt`
  - 単語クイズ中のマスコットを未回答時 `Thinking`、正解時 `Cheer`、不正解時 `Thinking` に整理。
  - 問題カードを `GramCard` 化。
  - 選択肢と「わからない」を共通ボタンへ寄せつつ、正解/不正解の Success/Danger フィードバックと回答済み disabled 挙動は維持。
- `GramComponents.kt`
  - `GramPrimaryButton` に任意の `containerColor` / `contentColor` / disabled 色指定を追加。
  - `GramSecondaryButton` に `enabled` を追加。
- `SentenceQuizContent.kt`
  - カンプ4「文章並べ替え」寄せに変更。
  - `AnimatedMascot(Point)` を追加し、正解後は `Cheer`。
  - 文テンプレートカードを `GramCard` 化し、選択肢 / 戻す / 次へボタンを共通ボタン化。
  - 並べ替えロジック、Undo、Next 処理は変更していない。
- `ResultScreen.kt` / `CommonResultSections.kt` / `ResultActionBar.kt` / `ResultAccuracyCard.kt`
  - 結果画面背景を `SoftBlue` へ変更。
  - Result アクションバーを `GramSecondaryButton("再チャレンジ")` + `GramPrimaryButton("次へ")` に変更。
  - カスタム単語クイズ結果コンテンツにも `AnimatedMascot(Cheer)` と Gram ボタンを適用。
  - `Color(0xFFDDE5EC)` を `SoftBlue` へ置換。
- `CustomPassageListScreen.kt`
  - カンプ3「登録済み長文」寄せに変更。
  - 背景を `SoftBlue`、登録ボタンを `GramPrimaryButton`、行とメッセージを `GramCard` 化。
  - `viewModel.openSet(...)` と `PassagePracticeScreen` 遷移は維持。
- `CustomSentenceListScreen.kt`
  - カンプ3「登録一覧」寄せに変更。
  - 背景を `SoftBlue`、追加ボタン / 検索 / 空状態 / 行を Gram 系に整理。
  - 検索・削除ダイアログ・削除処理は変更していない。
- `WordDetailScreen.kt`
  - 詳細カードを `GramCard` 化。
  - お気に入り / 学習済み / 復習追加を Gram ボタンに差し替え。
  - 音声再生、favorite/learned/review の ViewModel 呼び出しは維持。

### 検証結果

- 今回の作業中に `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を3回実行し、すべて **BUILD SUCCESSFUL**。
  - 1回目: 長文登録 + インポート画面変更後。52 tasks、14 executed。
  - 2回目: クイズ / 結果画面変更後。52 tasks、14 executed。
  - 3回目: 登録済み長文 / 登録文章一覧 / 単語詳細変更後。52 tasks、14 executed。
- 既存の KAPT 警告は継続して出るが、今回変更によるビルド/テスト失敗はなし。
- エミュレータ目視は過去追記どおり SystemUI ANR が頻発する前提のため未実施。ユーザー方針どおり build + unit test を確認基準にした。

### 現在の進捗更新

- #1 B.1/B.2 一覧: 実装済み・build+test OK。
- #2 B.3/B.4 登録: `AddWord` / `AddIdiom` / `AddSentence` / `CustomPassageRegistration` は実装済み・build+test OK。
- #3 B.5 インポート: `BulkImportScreen` は土台済み、`WordImportScreen` / `SentenceImportScreen` は今回実装済み・build+test OK。細部のカード行はまだ旧 Card が一部残るが主要導線と成功演出は対応済み。
- #4 C.1/C.2 クイズ: `CommonQuizContent` / `SentenceQuizContent` は今回実装済み・build+test OK。
- #5 C.3 Result: 通常 Result / カスタム単語 Result とも今回実装済み・build+test OK。
- #6 C.4/C.5 長文練習/フラッシュカード/復習/残り画面: `CustomPassageList` / `CustomSentenceList` / `WordDetail` は今回対応済み。`PassagePracticeScreen.kt`、`FlashcardScreen.kt`、`ReviewScreen.kt`、`TrainingListScreen.kt`、`CustomTrainingListScreen.kt`、`RandomCustomQuizScreen.kt` はまだ旧寄り。
- #7 Z 最終検証: 未完了。

### 次回の最初にやること

1. `PassagePracticeScreen.kt` のハードコード色と旧レイアウトをカンプ4「長文問題クイズ」寄せにする。現状 `Color(0x...)` が多いので、theme token / `GramCard` / `GramPrimaryButton` へ段階的に差し替える。長文クイズロジックには触らない。
2. `FlashcardScreen.kt` / `ReviewScreen.kt` / `TrainingListScreen.kt` / `CustomTrainingListScreen.kt` / `RandomCustomQuizScreen.kt` を残り画面として GramCraft 化する。
3. `SentenceResultContent.kt` の `Color(0xFFDDE5EC)` など、残った画面内 hardcoded color を theme へ寄せる。
4. 各まとまりごとに `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を実行。コミットする場合は既存未コミット差分が多いので対象ファイルだけ `git add` する。

---

## 最新追記（Codex / 2026-06-14 夜）— ★次回はここから読む

ユーザー依頼「HANDOFF-gramcraft-redesign.md の続きをやってほしい」「デザインは提示カンプ4枚に忠実に」で再開。**コミットはしていない。** 既存の未コミット差分を壊さず、カンプ2/3相当の未変換画面を追加で GramCraft 化した。

### 今回進めたこと

- 再開直後に `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を実行し、既存の途中差分状態で **BUILD SUCCESSFUL** を確認。
- `CustomIdiomListScreen.kt`
  - タイトルをカンプに合わせて「英熟語」へ整理。
  - 背景を `SoftBlue`、行を `GramCard` 化。
  - 右下 `GramFab` を追加し、既存ルート `Route.AddIdiom.path` へ接続。
  - 英熟語には既存の favorite/learned 状態がないため、星/習得トグルは追加していない。削除ボタンは維持。
  - 同ファイル内の `AddIdiomScreen` も `AnimatedMascot(Wave)` + `GramCard` + `GramSecondaryButton("プレビュー")` + `GramPrimaryButton("保存")` へ変更。
- `AddWordScreen.kt`
  - カンプ2「新規単語登録」に寄せて、`AnimatedMascot(Wave)` + 吹き出し + `GramCard` フォームへ変更。
  - 既存の入力制限・`viewModel.save(english, meaning)` は維持。
  - 画面内プレビュー表示を `rememberSaveable` で追加し、保存処理とは分離。
- `SentenceMenuScreen.kt`
  - カンプ3「文章問題」に寄せて、背景を `SoftBlue`、上部 `AnimatedMascot(Idle)` + 吹き出し、主要導線を白い `GramCard` 行へ変更。
  - 文章登録 / 登録一覧 / 文章インポートのナビ先は既存 `Route` のまま。
  - 100問まとまりの進捗タイルを `GramCircularProgress` で追加。既存のブロック一覧導線も維持。
- `AddSentenceScreen.kt`
  - カンプ3「文章登録」に寄せて、`AnimatedMascot(Wave)` + `GramCard` フォーム + `GramPrimaryButton("保存")` に変更。
  - 既存の語数/[語句]バリデーション・保存処理は維持。
- `strings.xml` / `values-en/strings.xml`
  - `add_word_mascot`
  - `add_idiom_mascot`
  - `add_sentence_mascot`
  を追加。

### 検証結果

- 変更後に `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を実行し、**BUILD SUCCESSFUL**（52 tasks、20 executed）を確認。
- 既存の KAPT 警告は継続して出るが、今回の変更によるビルド/テスト失敗はなし。
- このホストのエミュレータ目視は過去追記どおり SystemUI ANR が頻発するため未実施。ユーザー方針に従い build + unit test を確認基準にしている。

### 現在の進捗更新

- #1 B.1/B.2 一覧: **B.1 は前回途中実装済み / B.2 は今回実装済み・build+test OK**。
- #2 B.3/B.4 登録: **AddWord / AddIdiom / AddSentence は今回実装済み・build+test OK**。`CustomPassageRegistrationScreen.kt` はまだ旧寄りなので次に対応。
- #3 B.5 インポート: 未完了。`BulkImportScreen.kt` は一部土台あり、`WordImportScreen.kt` / `SentenceImportScreen.kt` はカンプ2/3の成功・プレビュー見た目へ寄せる必要あり。
- #4 C.1/C.2 クイズ: 未完了。
- #5 C.3 Result: 未完了。
- #6 C.4/C.5 長文練習/フラッシュカード/復習/残り画面: 未完了。ただし `SentenceMenuScreen.kt` は今回一部対応済み。
- #7 Z 最終検証: 未完了。

### 次回の最初にやること

1. `CustomPassageRegistrationScreen.kt` をカンプ3「長文問題登録」寄せにする。既存の手入力/貼り付け/設題追加/保存ロジックは触らず、`SoftBlue` + `AnimatedMascot(Wave)` + `GramCard` + `GramSecondaryButton/GramPrimaryButton` へ差し替える。
2. `SentenceImportScreen.kt` / `WordImportScreen.kt` / `BulkImportScreen.kt` をカンプ2/3のインポート画面へ寄せる。成功時は `MascotMood.Cheer` を使う（紙吹雪は `AnimatedMascot` 側で自動）。
3. その後、カンプ4の `CommonQuizContent` / `SentenceQuizContent` / `ResultScreen` を優先。特に Result はまだ未変換なので目立つ。
4. 各まとまりごとに `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を実行。コミットする場合は、既存未コミット差分が多いので、対象ファイルだけ `git add` する。

---

## 最新追記（Claude / 2026-06-14 午後）— ★ここから読む

ユーザー依頼「HANDOFF の続きをやってほしい」で再開 → 途中でユーザーがカンプ画像4枚を提示し「ずいぶん変わってない? カンプ通りに作って」と指示 → 「一旦中断して現状と続きを HANDOFF に書いて」で停止。**コミットはしていない。**

### 今回判明した最重要事実（次回の前提）

1. **「変わって見える」原因 = ほとんどの画面が未変換だっただけ。**
   実際にカンプの GramCraft デザインに作り替え済みなのは **4画面のみ**:
   `HomeScreen` / `LessonListScreen`（英単語レッスン）/ `IdiomLessonListScreen`（英熟語レッスン）/ `SettingsScreen`。
   残り ~18画面（英単語/英熟語一覧・各登録・各インポート・クイズ・並べ替え・**Result結果画面**・文章メニュー・単語カード・復習 等）は**旧デザインのまま**。これは計画の **Phase B / C が未着手**だったため。デザインが壊れたのではなく、未実装。
   - 変換済み判定の確認コマンド:
     `grep -rl -e GramCard -e GramListRow -e GramFilterChips -e GramFab --include="*.kt" app/src/main/java/com/example/vocabapp/ui/screen`
2. **テーマ色は既に緑。** `ui/theme/VocabTheme.kt` のトークンは名前が "Blue" だが**値は緑**（`BrightBlue=0xFF58CC02` Duolingo緑 / `SoftBlue=0xFFEAF8EF` 淡緑 / `DeepBlue=0xFF123F34` 濃緑 / `AccentBlue=0xFF1CB0F6` は実際に水色 / `Gold=0xFFFFC943` / `Success=0xFF58CC02` / `Danger=0xFFE5395A` / `Teal=0xFF00C2A8`）。色は問題ない。リネームはしない（全画面が依存）。
3. **このホストのエミュは目視確認に使えない。** `com.android.systemui` が ANR を繰り返し黒画面/ダイアログで塞ぐ。`Pixel_7_API_35` も `Pixel_7_API_36` も、`-gpu host` も `-gpu swiftshader_indirect` もダメ。アプリ自体は健全（`dumpsys` で MainActivity が topResumedActivity）。メモリ `emulator-systemui-anr.md` 参照。→ **build + unit test を品質判断の基準**にする。

### ユーザーの決定（今回ヒアリング済み・厳守）

- **スコープ: 全画面を一気に**（Phase B/C 全部、カンプ通りに統一）。
- **検証: build + テストで進めてコミットまでやってよい。目視確認はユーザー自身が自分のマシンでやる。**
  - = エミュ目視はブロッカーにしない。各論理単位でビルド/テスト通過を確認したらコミットして良い。

### カンプ4枚の対応表（実装ターゲット）

- カンプ1: ホーム / レッスン(英単語) / レッスン(英熟語) … **実装済み**（細部の作り込み余地はあり: ステータスにアイコン、報酬ピルをクローバー/ジェム/ベル等。優先度低）。
- カンプ2: 新規単語登録 / 英熟語登録 / 英単語一覧 / 英熟語一覧 / 問題一括インポート / 単語インポート成功(紙吹雪) … **B.1〜B.5**。
- カンプ3: 文章問題メニュー / 文章登録 / 文章インポート / 登録一覧 / 長文登録 / 登録済み長文 … **B.4 + C.4/C.5**。
- カンプ4: 長文問題クイズ / 文章並べ替え / **Result結果画面** … **C.1/C.2/C.3**。

### 今回の作業（コミット前・作業ツリーのみ）

- **Task #1 (B.1) 着手・途中**: `app/.../ui/screen/custom/CustomWordListScreen.kt`
  - フィルタを `GramFilterChips` 化（ラベルを「習得済み/未習得」へ修正してカンプと一致）。
  - 行を `GramCard` ＋ お気に入り=星(Gold) ＋ 習得=緑チェック丸 ＋ 既存の削除ボタンは維持（CLAUDE.md「UI要素を勝手に消さない」）。
  - 右下に `GramFab`（→ `Route.AddWord`）をオーバーレイ。10件ごとのセクションヘッダ/プレビューは維持。検索バーは角丸14に。
  - **`GRADLE_USER_HOME=.gradle ./gradlew assembleDebug` で BUILD SUCCESSFUL 確認済み**（コンパイル通過。目視は未）。
- **Task #2 (B.2) 未着手**: `CustomIdiomListScreen.kt` は B.1 と同じ方針で。ただし英熟語は **fav/learned トグルが存在しない**（行は english+meaning+削除のみ）。検索/フィルタも現状なし。→ 行を `GramCard` 化し、必要なら `GramFab`(→ `Route.AddIdiom` 等、存在するルートのみ)。**存在しない機能（fav/learned/検索）を新規に足さない。**

### タスク進捗（TaskList と同期）

- #1 B.1/B.2 一覧 … **B.1 途中（compileOK）/ B.2 未**
- #2 B.3/B.4 登録（AddWord/AddSentence/CustomPassageRegistration、Waveマスコット＋吹き出し＋GramCardフォーム＋プレビュー/保存。新規strings: `add_word_mascot` 等） … 未
- #3 B.5 インポート（BulkImport/WordImport/SentenceImport、選択時 Idle/Wave・成功時 Cheer＋紙吹雪は自動） … 未
- #4 C.1/C.2 クイズ（CommonQuizContent+QuizScreen=Thinking / SentenceQuiz*=Point、選択肢カード Success/Danger） … 未
- #5 C.3 Result（CommonResultContent/Sections+ResultScreen、`gradeLabel(score)` の金バッジ＋スコア＋正解/不正解/正解率タイル＋詳細カード＋ボタン、Cheer紙吹雪） … 未
- #6 C.4/C.5 長文練習(ハードコード色22箇所をthemeへ)/フラッシュカード/復習/文章メニュー/CustomSentenceList/passage一覧/WordDetail/TrainingList … 未
- #7 Z 最終検証（全テスト＋clean build＋`grep -rn "Color(0x" .../ui/screen | grep -v theme` 空＋論理単位コミット） … 未

### 次回の最初にやること

1. `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` で現状（B.1途中）が通ることを再確認。
2. B.1 を仕上げ → B.2 → 以降タスク順に。各画面は**挙動・ナビ・保存を変えず見た目だけ**、`GramComponents.kt` の共通部品を使う。色は theme、リストは `key` 維持、マスコットは `graphicsLayer` のみ。
3. ユーザー方針により**目視はユーザーに任せ、build+testが通れば論理単位でコミットして良い**。コミットメッセージ末尾に Co-Authored-By トレーラ。
4. 作り込みの細部判断に迷ったら提示済みカンプ4枚（会話の画像）を正とする。

---

## 直近の追記（Codex / 2026-06-13 18:20頃）

ユーザー依頼「HANDOFF-gramcraft-redesign.md の続きをやってほしい」で一度再開し、ユーザー依頼で途中停止した。**コミットはしていない。**

### 最新追記（Codex / 2026-06-14 01:30頃）

ユーザー依頼「続きやってよ」→「実装を終えてくれ途中経過をHANDOFF-gramcraft-redesign.mdに記述してほしい」で再開し、ここで作業を区切った。**コミットはしていない。**

今回進めたこと:
- Phase A の途中まで実装した。
- Task A.1 相当: `app/src/main/java/com/example/vocabapp/ui/screen/lesson/LessonListScreen.kt`
  - 背景を `SoftBlue` に変更。
  - 既存の追加/登録一覧/カスタム単語クイズ導線は維持しつつ、ボタンを `GramSecondaryButton` / `GramPrimaryButton` に置換。
  - レッスン行を `GramLessonPathNode` + `GramPathConnector` + `GramCard` + `GramProgressBar` + `MasterBadge` / `GramPrimaryButton("スタート")` 構成へ変更。
  - `LazyColumn` は `itemsIndexed(..., key = { _, lesson -> lesson.id })` でキーを維持。
- Task A.2 相当: `app/src/main/java/com/example/vocabapp/ui/screen/lesson/IdiomLessonListScreen.kt`
  - 英単語レッスン一覧と同じ GramCraft 表現に置換。
  - 既存の英熟語登録/登録一覧/カスタム英熟語クイズ導線は維持。
  - 既存コードに locked 状態が見当たらなかったため、ロック表現は新規追加していない（計画の「既存 locked state がなければ invent しない」に従った）。
- Task A.3 相当: `app/src/main/java/com/example/vocabapp/ui/screen/settings/SettingsScreen.kt`
  - 既存の `BlueScaffold` と `AnimatedMascot(mood = MascotMood.Thinking)` は維持。
  - アプリ情報カードを `GramCard` 化。
  - データ管理行を `GramCard` 内の `SettingsDangerRow` に整理し、既存の削除/リセットダイアログ処理は維持。
- Task 0.5 補足: `app/src/main/java/com/example/vocabapp/ui/screen/common/GramComponents.kt`
  - 計画で参照されていたが未追加だった `GramPathConnector` を追加。

検証結果:
- `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を実行し、**BUILD SUCCESSFUL**（52 tasks、14 executed）。
- 既存の KAPT 警告は出るが、ビルド/テストは通過。
- 今回はユーザー指示によりここで区切ったため、追加の ADB 目視確認は実施していない。
- 直前の試行では `Pixel_7_API_35` で `System UI isn't responding` が再発しており、UI目視確認は依然として安定端末待ち。

現在の実装進捗:
- **Phase 0:** 実装済み。ビルド/テスト成功。目視確認とコミットは未完了。
- **Phase A:** A.1 / A.2 / A.3 相当まで実装済み。ビルド/テスト成功。目視確認とコミットは未完了。
- **Phase B / C / Z:** 未着手。

次回の最初にやること:
1. 安定した実機または別AVDで、Phase 0 と Phase A の対象画面を目視確認する。
   - Home: Wave マスコット表示。
   - 結果/成功系: Cheer + Confetti（該当画面に到達できる場合）。
   - レッスン一覧: 緑ノード + 白カード + progress + Master/スタート。
   - 英熟語レッスン一覧: 同上。
   - Settings: Thinking マスコット + 白カードの設定行。
2. 目視確認できたら、論理単位でコミットする。
   - `AnimatedMascot.kt` + `ConfettiOverlay.kt`
   - `GramComponents.kt`
   - `HomeScreen.kt`
   - `LessonListScreen.kt`
   - `IdiomLessonListScreen.kt`
   - `SettingsScreen.kt`
3. その後、計画ファイルの Phase B へ進む。
4. もし目視確認がまた SystemUI ANR で塞がれる場合は、ビルド/テスト済みであることを明記し、実機確認をユーザー側に依頼してからコミット可否を判断する。

### 追加追記（Codex / 2026-06-14 00:55頃）

ユーザー依頼「HANDOFF-gramcraft-redesign.md の続きをやってほしい」で再開した。**今回もコミットはしていない。**

今回進めたこと:
- `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を実行し、`GramComponents.kt` 追加後も **BUILD SUCCESSFUL**（52 tasks、14 executed）。
- `adb kill-server && adb start-server` 後、接続デバイスなしだったため `Pixel_7_API_35` を `-no-snapshot -gpu host` で起動し、`sys.boot_completed=1` を確認。
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` は **Success**。
- `adb shell am start -n com.example.vocabapp/.MainActivity` は発行でき、`dumpsys activity` 上は `MainActivity` が resumed / app window 作成済み。
- ただし今回も視覚確認用スクリーンショットは Android 側の `System UI isn't responding` ANR ダイアログで塞がれた。
  - `dumpsys window` では `Application Not Responding: com.android.systemui` が focus を奪っていた。
  - `am force-stop com.android.systemui` 後も ANR ダイアログが再発した。
  - そのため **ホームのマスコット描画、Cheer + 紙吹雪の目視確認は引き続き未完了**。
- Task 0.6 として `HomeScreen.kt` の旧デッドコードを削除し、統計/報酬ピルを `GramMiniStat` / `GramRewardPill` に置換した。
  - 削除: `HomeBottomNav`, `HomeBottomItem`, `HomePathCard`, `PathNode`, `PathConnector`, `HomeMiniStat`, `HomeRewardPill`。
  - `ContentType` import を戻す修正後、`GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` は **BUILD SUCCESSFUL**（52 tasks、14 executed）。

次回の最初にやること:
1. 安定した実機または別AVDで、保留中の Task 0.2 / 0.3 / 0.6 の目視確認を行う。
2. 目視確認できたら、まず `AnimatedMascot.kt` + `ConfettiOverlay.kt` だけをコミットする。
3. 次に `GramComponents.kt` を Task 0.5 として別コミットする。
4. 次に `HomeScreen.kt` を Task 0.6 として別コミットする。
5. その後、計画ファイルの Phase A へ進む。

今回進めたこと:
- `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を実行し、**Task 0.2 / 0.3 の未コミット状態では BUILD SUCCESSFUL**（52 tasks、6 executed）。
- `adb kill-server && adb start-server` → `adb devices` で `emulator-5554 device` を確認。
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` は **Success**。
- `adb shell am start -n com.example.vocabapp/.MainActivity` は発行でき、`dumpsys activity` 上は `MainActivity` が resumed / app window 作成済み。
- ただし視覚確認用スクリーンショットは、アプリではなく Android 側の ANR ダイアログで塞がれた。
  - 1回目: `Messages isn't responding`
  - 2回目: `Pixel Launcher isn't responding`
  - `dumpsys window` でも `Application Not Responding: com.google.android.apps.nexuslauncher` が focus を奪っていた。
  - そのため **ホームのマスコット描画、Cheer + 紙吹雪の目視確認は未完了**。
- Task 0.5 として `app/src/main/java/com/example/vocabapp/ui/screen/common/GramComponents.kt` を新規追加した。
  - 追加内容: `GramCard`, `GramRewardPill`, `GramMiniStat`, `GramProgressBar`, `GramCircularProgress`, `GramPrimaryButton`, `GramSecondaryButton`, `GramFilterChips`, `GramListRow`, `GramLessonPathNode`, `MasterBadge`, `GramFab`。
  - 既存の `ui/theme/VocabTheme.kt` の色トークンを使用。まだ既存画面には接続していない。
- `GramComponents.kt` 追加後に `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を開始したが、ユーザーから「一旦ここでストップ」指示があり、`pkill -f 'GradleDaemon|gradlew'` で停止した。
  - 停止時点では `:app:compileDebugKotlin` 付近まで進んでいた。
  - **Task 0.5 追加後のビルド/テスト完了確認は未実施**。

次回の最初にやること:
1. `GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest` を再実行し、`GramComponents.kt` 追加後のコンパイル結果を確認する。
2. 失敗した場合は `GramComponents.kt` の import / Material API / modifier 周りを最小修正する。
3. 安定した実機またはエミュレータで、保留中の Task 0.2 / 0.3 の目視確認を行う。
4. 目視確認できたら、まず `AnimatedMascot.kt` + `ConfettiOverlay.kt` だけをコミットする。
5. `GramComponents.kt` はビルド確認後、Task 0.5 として別コミットする。

現在の追加未コミット（この再開で増えたもの）:
- `?? app/src/main/java/com/example/vocabapp/ui/screen/common/GramComponents.kt`

---

## 0. これは何の作業か（1段落）

提示された GramCraft デザインカンプ（緑ヘッダー / 緑背景 / 白角丸カード / マスコット + 吹き出し / 下タブ）に **全 ~24 画面の見た目を統一** し、マスコットロボット (`robota_mascot.png`) に **画面ごとに異なるアニメーション**（Idle/Wave/Thinking/Cheer/Point + Cheer 時に紙吹雪）を持たせる。**見た目だけの変更**で、ナビ・ViewModel・Room・保存データ・機能挙動は一切変えない。マスコットは現状1枚絵だが、後でポーズ差分画像を1箇所のマップ変更で差し替えられる構造にしてある。

---

## 1. いまの状態（コミット済み / 未コミット）

**ブランチ:** `passage-bulk-submit`（main ではない。ここで作業継続して良い）

**このリファクタで追加したコミット:**
- `f04ba66` docs: 仕様書
- `e2485fd` docs: 実装計画
- `e7fbaf3` feat: Task 0.1 — `MascotAnimation.kt`（`MascotMood` enum 5値 / `MascotMotion` / `mascotMotionFor()` / `mascotDrawable()`）+ `AnimatedMascot.kt` から旧 enum 削除。ユニットテスト `MascotAnimationTest` PASS。
- `d101f75` feat: Task 0.4 — `GramFormat.kt` の `gradeLabel(score)`。テスト `GramFormatTest` PASS。

**未コミットの作業ツリー（このリファクタ分）= まず視覚確認してからコミットする:**
- `?? ConfettiOverlay.kt`（Task 0.2）— 新規。Cheer 時の紙吹雪オーバーレイ。
- `M AnimatedMascot.kt`（Task 0.3）— `mascotMotionFor()`/`mascotDrawable()` 駆動に書き換え、`showConfetti` 時に `ConfettiOverlay` を重ねる版に完了済み。
  → **ビルド成功・ユニットテスト PASS・クラッシュなし起動まで確認済み。残りは「絵が出ているか」の目視のみ。**
- `?? GramComponents.kt`（Task 0.5）— 新規。共通 GramCraft コンポーネントを追加済み。`GramPathConnector` も追加済み。**`assembleDebug testDebugUnitTest` 成功済み。残りは目視確認とコミット。**
- `M HomeScreen.kt`（Task 0.6）— 旧デッドコード削除 + `GramMiniStat` / `GramRewardPill` 利用済み。**`assembleDebug testDebugUnitTest` 成功済み。残りは目視確認とコミット。**
- `M LessonListScreen.kt`（Task A.1）— GramCraft レッスンパス化済み。**`assembleDebug testDebugUnitTest` 成功済み。残りは目視確認とコミット。**
- `M IdiomLessonListScreen.kt`（Task A.2）— GramCraft レッスンパス化済み。既存 locked state がないためロック表現は追加していない。**`assembleDebug testDebugUnitTest` 成功済み。残りは目視確認とコミット。**
- `M SettingsScreen.kt`（Task A.3）— `GramCard` ベースへ整理済み。既存ダイアログ/削除/リセット処理は維持。**`assembleDebug testDebugUnitTest` 成功済み。残りは目視確認とコミット。**

**未コミットの作業ツリー（このリファクタより前から存在する進行中の GramCraft 化。触らない／壊さない）:**
`MainActivity.kt`, `CommonDisplayComponents.kt`, `CommonQuizContent.kt`, `CommonScaffold.kt`, `ResultActionBar.kt`, `ResultMessageCard.kt`, `BulkImportScreen.kt`, `VocabTheme.kt`, `values/strings.xml`, `values-en/strings.xml`, `?? robota_mascot.png`, `?? stusap_fix.md`
→ これらは既に緑テーマ・GramCraft ヘッダー・下タブ・マスコット配線が入った「土台」。計画はこの土台の上に積む前提。

---

## 2. なぜ中断したか（次回これを解決すれば進む）

このホストの **Android エミュレータが不安定** で、視覚確認用のスクショが安定して撮れなかった（SystemUI が ANR / プロセス落ち / 真っ黒フレーム）。CLAUDE.md の「UI 変更はコミット前に必ず目視確認」を満たせなかったため、ユーザー判断で **UI コミットを保留** して中断した。
→ **再開条件: 安定して画面表示・スクショできる実機 or エミュレータ。**

確認できている事実（コード自体は健全）:
- `./gradlew assembleDebug` 成功（複数回）
- `./gradlew :app:testDebugUnitTest` の対象テスト PASS
- APK インストール後 `MainActivity` が topResumedActivity・プロセス生存・logcat に FATAL/AndroidRuntime なし

---

## 3. 次回の再開手順（この順でやる）

### Step A. 環境を整える
1. 実機接続 or 安定したエミュレータ起動。`adb devices` で `device` 表示を確認。
   - エミュレータ例: `~/Library/Android/sdk/emulator/emulator -avd <AVD> -no-snapshot` を起動し、`getprop sys.boot_completed` が `1` になるまで待つ。
   - 撮影が不安定なら `-gpu host`（ハード GPU）や、十分な起動待ち（60〜90秒）を確保する。本セッションでは software GPU だと SystemUI が ANR した。
2. `./gradlew assembleDebug`（CLI が `Could not load module <Error module>` で落ちる場合は `./gradlew clean assembleDebug -Pkapt.use.k2=true`）。
3. 「直したのに直らない」時は **古い APK 残存** を疑い `./gradlew clean assembleDebug` から入れ直す。

### Step B. 保留中の 0.2 / 0.3 を視覚確認 → コミット
1. インストール: `adb install -r app/build/outputs/apk/debug/app-debug.apk`、起動して **ホーム画面のマスコットが揺れている**ことを確認。
2. 結果画面 or インポート成功画面まで進み、**Cheer マスコット + 紙吹雪** が出ることを確認（紙吹雪は操作を妨げない＝タップ透過）。
3. 問題なければコミット（**この2ファイルだけ** ステージ）:
   ```
   git add app/src/main/java/com/example/vocabapp/ui/screen/common/ConfettiOverlay.kt \
           app/src/main/java/com/example/vocabapp/ui/screen/common/AnimatedMascot.kt
   git commit -m "feat: add confetti overlay and drive mascot from per-mood motion"
   ```
   ※他の進行中ファイルは巻き込まない。

### Step C. 計画の残りを順番に実装
`docs/superpowers/plans/2026-06-13-gramcraft-redesign.md` の **Task 0.5 の検証 → 0.6 → Phase A → B → C → 最終検証(Z)** をタスク単位で実装。各タスクは「ビルド成功 + 必要ならユニットテスト + 実機目視 + 単独コミット」で閉じる。計画には各タスクの完全なコード/対象ファイル/mood 割り当て/検証手順が書いてある。

主な残タスク概要:
- **0.5** 共通コンポーネント `GramComponents.kt`（GramCard/RewardPill/MiniStat/ProgressBar/CircularProgress/PrimaryButton/SecondaryButton/FilterChips/ListRow/LessonPathNode/MasterBadge/Fab）— **ファイル追加済み、検証未完了**
- **0.6** `HomeScreen.kt` のデッドコード削除（`HomeBottomNav`/`HomeBottomItem`/`HomePathCard`/`PathNode`/`PathConnector`）+ 共通部品へ置換
- **Phase A** レッスン一覧（番号パス + Master バッジ）/ 英熟語レッスン一覧 / 設定
- **Phase B** 英単語・英熟語一覧（検索+フィルタチップ+star/check 行+FAB）/ 各登録画面（Wave マスコット）/ 各インポート（成功時 Cheer+紙吹雪）
- **Phase C** クイズ(Thinking)/ 文章並べ替え(Point)/ 結果画面（grade バッジ+紙吹雪）/ 長文・フラッシュカード・復習 / 残り画面の一掃。`PassagePracticeScreen.kt` のハードコード色 22 箇所を theme 経由に直す。
- **Z** 全テスト + clean build + 全画面目視 + 最終レビュー。

### 共通ルール（CLAUDE.md 準拠・厳守）
- 挙動・ナビ・保存データを変えない（見た目のみ）。
- 色は `ui/theme/`、文字列は `res/values/strings.xml`(+`values-en`) 経由。ハードコード禁止。
- リスト `LazyColumn` は `key` 維持。マスコットは `graphicsLayer` のみで recomposition を増やさない。
- UI はコミット前に必ず目視確認。コミットは論理単位で分ける（数を勝手に減らさない）。

---

## 4. ツール別メモ

- **Claude で続ける場合:** `superpowers:subagent-driven-development` で計画をタスク単位実装。実機目視が要るタスクは、実装サブエージェントに「ビルド→install→screencap→Read で自己確認→コミット」まで行わせる（エミュレータが安定している前提）。安定しない場合は build+test で実装させ、目視はユーザー/コントローラ側でまとめて行う。
- **Codex で続ける場合:** 同じ計画ファイルをタスク単位で実装。各タスクのコードブロックはほぼそのまま使える。ビルド/テストコマンドは上記 Step A 参照。

---

## 5. 重要ファイル早見

| 目的 | パス |
|---|---|
| 仕様 | `.claude/specs/2026-06-13-gramcraft-redesign.md` |
| 実装計画（全コード入り） | `docs/superpowers/plans/2026-06-13-gramcraft-redesign.md` |
| この指示書 | `docs/superpowers/HANDOFF-gramcraft-redesign.md` |
| マスコット演出の中核 | `ui/screen/common/MascotAnimation.kt`, `AnimatedMascot.kt`, `ConfettiOverlay.kt`(未コミット) |
| 共通 GramCraft 部品 | `ui/screen/common/GramComponents.kt`(未コミット・検証未完了) |
| 共通枠/下タブ | `ui/screen/common/CommonScaffold.kt` |
| テーマ色 | `ui/theme/VocabTheme.kt` |
| ホーム（参照実装） | `ui/screen/home/HomeScreen.kt` |
| マスコット画像（1枚絵） | `res/drawable/robota_mascot.png`(未コミット) |
