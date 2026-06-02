# AGENTS.md

## Project Overview

This is a native Android application project.

The goal is to build a TEPPEN-style English vocabulary learning app for Android.

Use:

- Kotlin
- Android Studio
- Jetpack Compose
- Material 3
- MVVM architecture
- Gradle Kotlin DSL

---

## Project Claude Rules

このプロジェクトでは、以下の補助ファイルを必ず参照してください。

- `.claude/prompts/review.md`
- `.claude/prompts/test.md`
- `.claude/prompts/deploy.md`
- `.claude/agents/reviewer.md`
- `.claude/commands/scorereview.md`

作業後は必ず `.claude/prompts/test.md` に従って、必要なテストとビルド確認を行ってください。

保守性レビューを依頼された場合は、`.claude/prompts/review.md` または `.claude/agents/reviewer.md` に従ってください。

`/prompts:scorereview`、`/scorereview`、`scorereview`、`スコアレビュー`、点数付きの保守性レビューを依頼された場合は、Codex でも `.claude/commands/scorereview.md` をコマンド定義として読み、未コミット差分または指定されたコミット範囲をレビューしてください。

Android のビルド、APK インストール、ADB 起動確認が必要な場合は `.claude/prompts/deploy.md` に従ってください。

このプロジェクトは Kotlin + Jetpack Compose + Room + Hilt + MVVM 構成の Android ネイティブアプリです。
Flutter / React Native / Web へ変換しないでください。

---

## Required Skills

このプロジェクトで作業する場合は、以下のスキルを必須として扱ってください。

- spec-driven-development
- test-driven-development
- code-review-and-quality
- security-threat-modeling
- performance-analysis

各スキルの具体的な適用方針は `Development Practices` に従ってください。

---

## App Concept

Build an English vocabulary learning app inspired by TEPPEN-style word training.

The app should allow users to study English words using:

- Word list
- Word detail screen
- Favorite words
- Learned status
- Quiz mode
- Multiple-choice questions
- Result screen
- Progress tracking
- Local data storage

The app should be installable and usable on a local Android smartphone.

---

## Required Screens

Create and maintain these screens:

1. Home screen
   - Learning progress
   - Start quiz button
   - Word list navigation
   - Settings navigation

2. Word list screen
   - English word list
   - Japanese meanings
   - Search function
   - Favorite filter
   - Learned / unlearned filter
   - Detail screen navigation

3. Word detail screen
   - English word
   - Japanese meaning
   - Example sentence
   - Japanese translation
   - Favorite toggle
   - Learned toggle

4. Quiz screen
   - Multiple-choice question
   - 4 answer choices
   - Correct / incorrect judgment
   - Next question button

5. Result screen
   - Score
   - Correct count
   - Wrong count
   - Retry button
   - Back to home button

6. Settings screen
   - Reset progress
   - App information
   - Simple settings if needed

---

## Data Design

Use local persistence first.

Preferred data choices:

- Room database for vocabulary and progress
- DataStore for settings
- JSON seed data for initial word data

Do not add a backend API unless explicitly requested.

Example word data should include:

- id
- english word
- Japanese meaning
- example sentence
- Japanese translation
- category
- level
- favorite flag
- learned flag

---

## Architecture

Use MVVM.

Recommended directory structure:

```text
app/src/main/java/com/example/teppenenglish/
├── MainActivity.kt
├── ui/
│   ├── screens/
│   ├── components/
│   └── theme/
├── viewmodel/
├── data/
│   ├── local/
│   ├── repository/
│   └── model/
└── domain/
```

---

## Development Practices

このプロジェクトでは、以下の開発プラクティスを組み込んで作業してください。

### spec-driven-development

- 実装前に、対象機能の仕様・画面要件・データ要件・受け入れ条件を確認してください。
- 仕様が曖昧な場合は、既存コード・README・`.claude` 配下の補助ファイルから判断し、危険な仮定がある場合だけユーザーに確認してください。
- 仕様外の機能追加や挙動変更は避け、必要最小限の変更にしてください。
- 画面・状態・データ永続化・Navigation・Room Migration への影響範囲を実装前に把握してください。

### test-driven-development

- 不具合修正やロジック追加では、可能な範囲で先に失敗するテスト、または再現手順を用意してください。
- UseCase / Repository / ViewModel / Room DAO など、UI から切り離せる処理は単体テストを優先してください。
- Compose UI 変更では、ビルドに加えて実機またはエミュレータで表示と主要操作を確認してください。
- 作業後は `.claude/prompts/test.md` に従い、必要なテストとビルド確認を実行してください。

### code-review-and-quality

- 変更後は、可読性・責務分離・MVVM の依存方向・Compose の状態管理・Room/Hilt/Navigation の整合性を確認してください。
- 保守性レビューを依頼された場合は、`.claude/prompts/review.md` または `.claude/agents/reviewer.md` に従ってください。
- `scorereview` 系の依頼では、`.claude/commands/scorereview.md` を読み、未コミット差分または指定コミット範囲を点数付きでレビューしてください。
- Critical / Important な指摘は、修正するか、対応しない理由を明確にしてください。

### security-threat-modeling

- 個人データ、学習履歴、進捗、設定、インポートデータなど、ユーザーに紐づく情報の扱いを確認してください。
- Room / DataStore / ファイル入出力 / ログ出力 / Android Manifest / exported component / backup 設定のリスクを確認してください。
- 不要な権限、外部送信、機密情報のログ出力、平文保存、入力値未検証を避けてください。
- バックエンド API は明示要求がない限り追加しないでください。

### performance-analysis

- 大量の単語データ、検索、フィルタ、クイズ生成、Room クエリ、Flow/StateFlow の再購読、Compose 再コンポーズの負荷を意識してください。
- UI スレッドをブロックする処理を避け、必要に応じて Repository / UseCase / ViewModel 側で非同期処理に分離してください。
- リスト表示は LazyColumn など既存の Compose パターンを使い、不要な全件再計算や過剰な状態更新を避けてください。
- パフォーマンス改善は、推測だけで大きく変えず、計測・ログ・再現条件に基づいて最小限に行ってください。

---

# Workflow Rules

## Git Workflow

- When the user specifies a number of commits/pushes, follow that exact number.
- Do not reduce or consolidate commit counts unless explicitly requested.
- Always verify before commit.
- Never commit unverified UI changes.
- Keep commits logically separated whenever possible.

---

## UI Verification Rules

- After any UI/CSS/layout change, always rebuild the project.
- Verify the UI visually before committing.
- Do not assume UI changes are correct without checking rendering.
- Do not modify unrelated UI components.
- Make only the minimum necessary UI change.

---

## Android Workflow

Before Android install/deploy:

1. Run:
   adb kill-server && adb start-server

2. Build project:
   ./gradlew assembleDebug

3. Install APK:
   adb install -r app/build/outputs/apk/debug/app-debug.apk

4. Verify installation succeeded.

5. Launch app after install.

If ADB fails, retry up to 3 times before stopping.

---

## Bug Investigation Rules

- If the same bug is not fixed after 2 attempts:
  - stop making surface-level fixes
  - investigate root cause
  - list assumptions
  - verify assumptions before editing more code

- Avoid repeated trial-and-error fixes without diagnosis.

---

## Change Scope Rules

- Do not introduce behavior changes not requested by the user.
- Do not remove or hide UI elements unless explicitly requested.
- Avoid unnecessary refactors during bug fixes.
- Preserve existing behavior whenever possible.

---

## Verification Rules

Before completing a task:

- Build must succeed
- UI must render correctly
- Emulator/browser verification should be completed
- Errors must be checked before commit/push

Never mark work as complete before verification.

---

## Claude Configuration Files

- `touch .claude/config.toml`
  - `.claude/config.toml` を作成、または更新日時を変更する
  - Claude 用の設定ファイル置き場として使う想定
- `touch .claude/prompts/review.md`
  - review 用のプロンプトファイルを作成、または更新する
  - レビュー用の指示文を入れる場所
- `touch .claude/prompts/test.md`
  - test 用のプロンプトファイルを作成、または更新する
  - テスト実行用の指示文を入れる場所
- `touch .claude/prompts/deploy.md`
  - deploy 用のプロンプトファイルを作成、または更新する
  - デプロイ用の指示文を入れる場所
- `touch .claude/agents/reviewer.md`
  - reviewer エージェント定義ファイルを作成、または更新する
  - 自動レビュー担当のエージェント設定を書く場所

注意点として、`touch` は中身は書きません。空ファイルを作るだけなので、実際に使えるようにするには後で各ファイルへ内容を入れる必要があります。
