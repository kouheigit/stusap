# 引き継ぎ指示書 — GramCraft 全画面デザイン統一 + ロボット演出

> 中断日: 2026-06-13 / ブランチ: `passage-bulk-submit`
> 次回 Codex / Claude が **ここから続きを実装** するための指示書。
> 関連: 仕様 `.claude/specs/2026-06-13-gramcraft-redesign.md` / 実装計画 `docs/superpowers/plans/2026-06-13-gramcraft-redesign.md`

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

**未コミットの作業ツリー（このリファクタより前から存在する進行中の GramCraft 化。触らない／壊さない）:**
`MainActivity.kt`, `CommonDisplayComponents.kt`, `CommonQuizContent.kt`, `CommonScaffold.kt`, `ResultActionBar.kt`, `ResultMessageCard.kt`, `HomeScreen.kt`, `BulkImportScreen.kt`, `SettingsScreen.kt`, `VocabTheme.kt`, `values/strings.xml`, `values-en/strings.xml`, `?? robota_mascot.png`, `?? stusap_fix.md`
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
`docs/superpowers/plans/2026-06-13-gramcraft-redesign.md` の **Task 0.5 → 0.6 → Phase A → B → C → 最終検証(Z)** をタスク単位で実装。各タスクは「ビルド成功 + 必要ならユニットテスト + 実機目視 + 単独コミット」で閉じる。計画には各タスクの完全なコード/対象ファイル/mood 割り当て/検証手順が書いてある。

主な残タスク概要:
- **0.5** 共通コンポーネント `GramComponents.kt`（GramCard/RewardPill/MiniStat/ProgressBar/CircularProgress/PrimaryButton/SecondaryButton/FilterChips/ListRow/LessonPathNode/MasterBadge/Fab）
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
| 共通枠/下タブ | `ui/screen/common/CommonScaffold.kt` |
| テーマ色 | `ui/theme/VocabTheme.kt` |
| ホーム（参照実装） | `ui/screen/home/HomeScreen.kt` |
| マスコット画像（1枚絵） | `res/drawable/robota_mascot.png`(未コミット) |
