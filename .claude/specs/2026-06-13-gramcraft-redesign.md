# GramCraft 全画面デザイン統一 + ロボット演出 仕様書

> 作成日: 2026-06-13 / ブランチ: `passage-bulk-submit`
> 位置づけ: CLAUDE.md の Spec-Driven Development ルールに基づく仕様書。

## 1. 目的・スコープ

提示された GramCraft デザインカンプ（全4枚・約24画面）に、アプリの**全画面の見た目を統一**する。
あわせてマスコットロボット (`robota_mascot.png`) に**画面ごとに異なるアクション（動き）**を持たせる。

- **In scope:** UI レイヤー（Compose の見た目・配色・コンポーネント・マスコット演出）のみ。
- **Out of scope:** ナビゲーション構造、ViewModel/Repository、Room、保存データ、機能挙動の変更。
  リファクタリング前後で画面遷移・保存データ・機能は同一であること（CLAUDE.md Change Scope Rules 準拠）。

## 2. 現状（ベースライン）

`passage-bulk-submit` ブランチで GramCraft 化が一部着手済み:

- `ui/theme/VocabTheme.kt` — GramCraft グリーン系カラー定義済み（`BrightBlue=#58CC02` 等）。
- `ui/screen/common/CommonScaffold.kt` — `BlueScaffold`（緑ヘッダー "GramCraft"）+ `GramBottomNavigation`（ホーム/レッスン/復習/学習ログ/設定）+ `StatCard`/`CardButton`/`BottomAction` 済み。
- `ui/screen/common/AnimatedMascot.kt` — `MascotMood{Idle,Wave,Thinking,Cheer}` + 揺れ/回転/拡縮ループ済み。
- `ui/screen/home/HomeScreen.kt` — Hero / StatsGrid / FeatureGrid に再構成済み。
- `robota_mascot.png`（384×512, 1枚絵）追加済み。

未統一: Lesson/IdiomLesson 一覧、Word/Idiom 一覧、各登録画面、各インポート画面、Quiz/SentenceQuiz/Passage、各結果画面、Flashcard、Review、Settings 詳細 など。

### 既知のデッドコード（本仕様で除去する）
`HomeScreen.kt` 内の未使用トップレベル: `HomeBottomNav`, `HomeBottomItem`, `HomePathCard`, `PathNode`, `PathConnector`（呼び出し元なし。`GramBottomNavigation` に置換済みのため不要）。

## 3. デザイン仕様

### 3.1 共通ビジュアル言語
- 背景: `SoftBlue (#EAF8EF)`。ヘッダー: 濃緑 `#0F7F45`、白文字 "GramCraft" + サブタイトル。
- カード: 白・角丸 12〜20dp・elevation 2〜3dp。
- アクセント: `BrightBlue (#58CC02)` 主要、`Gold (#FFC943)` 報酬/Master、`AccentBlue (#1CB0F6)` 補助、`Danger (#E5395A)` 不正解。
- フォント: 見出し `FontWeight.Black`、本文 Bold。
- すべての色は `ui/theme/`、文字列は `res/values/strings.xml`(+`values-en`) を経由（ハードコード禁止 / CLAUDE.md Prohibited Patterns）。

### 3.2 共通コンポーネント（`ui/screen/common/` に集約）
既存を活かしつつ不足分を追加。各コンポーネントは「何をするか/使い方/依存」が単体で分かる粒度にする。

| コンポーネント | 用途 | 状態 |
|---|---|---|
| `BlueScaffold` | 緑ヘッダー + 下タブの共通枠 | 既存（流用） |
| `GramBottomNavigation` | 下部ナビ | 既存（流用） |
| `GramCard` | 白角丸カード基底 | 追加 |
| `GramMiniStat` / `GramRewardPill` | 統計タイル / 報酬ピル | Home から共通化 |
| `GramProgressBar` | 横長進捗バー | 追加（Home の実装を抽出） |
| `GramCircularProgress` | リング進捗（92%/65% 等） | 追加 |
| `GramPrimaryButton` / `GramSecondaryButton` | 保存/答え合わせ・プレビュー等 | 追加 |
| `GramFilterChips` | すべて/お気に入り/習得済み/未習得 | 追加 |
| `GramListRow` | 英単語/熟語行（star + 習得チェック） | 追加 |
| `GramFab` | 右下＋ボタン | 追加 |
| `GramLessonPathNode` / `GramPathConnector` | レッスン番号ノード + 縦線 | Home から抽出・再利用 |
| `MasterBadge` | "Master ○語" 金バッジ | 追加 |

### 3.3 マスコット演出システム（1枚絵で実装、ポーズ画像は後差し替え可能に）
`AnimatedMascot` を拡張し、各画面が `MascotMood` を渡す。各 mood が transform ベースの固有挙動を持つ。

- `Idle`: ゆるい上下バウンス（一覧/設定）。
- `Wave`: 腕振り風の左右ティルト揺れ（ホーム/各登録画面）。
- `Thinking`: ゆっくりティルト＋低速バウンス（長文読解/クイズ）。
- `Cheer`: 弾みバウンス＋スカッシュ&ストレッチ＋**紙吹雪オーバーレイ**（結果画面/インポート成功）。
- `Point`: 前傾の小刻み揺れ（文章並べ替えの「並べよう！」）。

実装方針:
- すべて `rememberInfiniteTransition` + `graphicsLayer`（recomposition を発生させない）。
- 紙吹雪は軽量な `Canvas`/オーバーレイで、`Cheer` 時のみ描画。
- `MascotMood → @DrawableRes` のマップを用意し、初期は全て `robota_mascot` を指す。
  → 後でポーズ差分画像が用意できたら、マップ1箇所の変更のみで反映（呼び出し側・引数は不変）。
- 任意の吹き出し（speech bubble）はカンプ準拠スタイル。既存 `message` 引数を流用。

### 3.4 画面別の mood 割り当て（代表例）
- ホーム: `Wave` +「今日も一緒にがんばりましょう！」
- 単語/熟語登録: `Wave` +「新しい単語を登録して…」
- インポート（選択時）: `Idle` /（成功時）: `Cheer` + 紙吹雪 +「成功しました！」
- 長文読解クイズ: `Thinking` +「しっかり読んで正解を選ぼう！」
- 文章並べ替え: `Point` +「正しい語順に並べよう！」
- 結果画面: `Cheer` + 紙吹雪 +「おつかれさまでした！」
- 一覧/設定: `Idle`

## 4. 実装フェーズ（各フェーズ独立してビルド・実機確認可能）

| フェーズ | 内容 | 確認 |
|---|---|---|
| 0 | 共通コンポーネント拡充 + マスコット演出システム拡張 + Home デッドコード除去 | build + Home 表示確認 |
| A. Core | Home 微調整 / Lesson・IdiomLesson 一覧（番号パス + Master バッジ）/ Settings | build + 実機 |
| B. Lists & 登録 | Word/Idiom 一覧（検索+フィルタ+star/check 行+FAB）/ AddWord・AddSentence・Passage 登録 / 各インポート（成功時 Cheer+紙吹雪） | build + 実機 |
| C. Quiz & 結果 | Quiz / SentenceQuiz（並べ替え）/ Passage practice / 全結果画面（スコアリング+グレードバッジ）/ Flashcard / Review | build + 実機 |

## 5. エラーケース / エッジケース
- 1枚絵のため手振り等は擬似演出（transform）。違和感が出る mood は揺れ幅を抑える。
- 長いラベル（機能タイル/リスト行）は `maxLines` + ellipsis で崩れ防止。
- 紙吹雪オーバーレイは結果画面の操作（再チャレンジ/次へ）を妨げない（タップ透過）。
- 進捗 0/total 時の割り算（progress=0f）を保証。

## 6. パフォーマンス（CLAUDE.md Performance Analysis 準拠）
- マスコットアニメは `graphicsLayer` のみ操作し recomposition を増やさない。
- 一覧は `LazyColumn` + `key` 指定を維持。
- 画面遷移 300ms 以内・追加アセットは最小（追加画像なし、当面は既存PNGのみ）。

## 7. 受け入れ条件 (Acceptance Criteria)
1. 全対象画面が GramCraft デザイン言語（緑ヘッダー/緑背景/白角丸カード/下タブ）で統一されている。
2. マスコットが画面ごとに異なる mood で動作し、結果/成功画面では紙吹雪が出る。
3. ポーズ画像の後差し替えがマップ1箇所で可能な構造になっている。
4. ナビゲーション・保存データ・機能挙動がリファクタ前後で同一。
5. ハードコード色/文字列を新規に増やしていない（theme/strings 経由）。
6. `./gradlew :app:testDebugUnitTest` と `assembleDebug` が通り、各フェーズで実機表示を確認済み。
7. 未使用デッドコード（Home の旧 BottomNav/PathCard 等）が除去されている。
