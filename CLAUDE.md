# CLAUDE.md

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

- `.claude/agents/reviewer.md`
- `.claude/prompts/test.md`
- `.claude/prompts/deploy.md`

作業後は必ず `.claude/prompts/test.md` に従って、必要なテストとビルド確認を行ってください。

保守性レビューを依頼された場合は、`.claude/agents/reviewer.md` に従ってください。

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

### Attempt Limit

- If the same bug is **not fixed after 2 attempts**, immediately stop making surface-level fixes.
- Do not attempt a 3rd fix until the root cause investigation below is complete.

### Root Cause Investigation Steps

1. **Describe the symptom precisely**
   - Copy the exact error message and stack trace.
   - Identify reproduction conditions (always / conditional / intermittent).

2. **List all assumptions and hypotheses**
   - Write down every assumption about why the code should work.
   - For each hypothesis, define a concrete verification method.

3. **Verify hypotheses one by one — before changing code**
   - Use logs, debugger, or unit tests to confirm or deny each hypothesis.
   - Do not modify code until at least one hypothesis is verified.

4. **Check the impact scope**
   - Confirm the fix does not break other modules, screens, or states.
   - Check for side effects on Room Migration / StateFlow / LaunchedEffect / DisposableEffect.

5. **Decide on a fix strategy, then implement**
   - "Let me just try changing this" is prohibited.
   - Only implement a fix when you can state the reason it will work.

### Prohibited Behaviors

- Continuing to modify code while the root cause is unknown (trial-and-error).
- Repeating the same fix with superficial wording changes.
- Fixing code without checking logs or stack traces first.
- Stacking new changes on top of unresolved build errors.

### Escalation Criteria

- **2 failed attempts** → switch to root cause investigation.
- **Root cause still unknown after investigation** → report current status to the user and request additional information.
- **All hypotheses ruled out** → suspect an architecture-level design issue and propose a structural review.

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

---

## Spec-Driven Development

実装を始める前に仕様を確定する。

### Rules

- 新機能・画面・ViewModel を追加する前に、必ず仕様書（spec）を作成または確認すること。
- 仕様は `.claude/specs/` ディレクトリに Markdown 形式で保存する。
- 仕様に含めるべき項目：
  - 機能の目的・スコープ
  - 入力・出力・状態の定義
  - UI の振る舞いと状態遷移
  - エラーケース・エッジケース
  - 受け入れ条件（Acceptance Criteria）
- 仕様が存在しない機能への実装着手は禁止。
- 仕様変更が生じた場合は、コード変更前に仕様ファイルを更新すること。

---

## Test-Driven Development

コードより先にテストを書く。

### Rules

- 新しいロジック（ViewModel・Repository・UseCase・Utility）を実装する前に、対応するユニットテストを先に作成する。
- テストが失敗することを確認してから実装に入る（Red → Green → Refactor サイクル）。
- テスト対象：
  - ViewModel の状態変化（StateFlow / UiState）
  - Repository のデータ取得・保存ロジック
  - ドメインロジック（クイズ採点、進捗計算など）
- テストフレームワーク：JUnit4 / MockK / Turbine（Flow テスト）
- UI テストは Compose Testing API を使用する。
- テストなしで実装を完了したとみなすことを禁止する。
- テストファイルの配置：`app/src/test/` および `app/src/androidTest/`

---

## Code Review and Quality

すべてのコード変更はレビュー基準を満たすこと。

### Review Checklist

- **可読性**: 変数名・関数名が意図を明確に表しているか。
- **単一責任**: 1 つのクラス・関数が複数の責務を持っていないか。
- **重複排除**: 同じロジックが複数箇所に散在していないか。
- **MVVM 遵守**: UI ロジックが ViewModel に、データ操作が Repository に正しく分離されているか。
- **Compose ベストプラクティス**: 不要な recomposition が発生していないか。`remember` / `derivedStateOf` が適切に使われているか。
- **Room**: Migration が定義されているか。クエリに適切なインデックスがあるか。
- **Hilt**: DI スコープが適切か（`@Singleton` の乱用がないか）。
- **エラーハンドリング**: Result 型や try-catch が適切に使われているか。
- **コメント**: WHY が非自明な箇所にのみコメントが存在するか（WHAT コメントは不要）。

### Prohibited Patterns

- `TODO` や `FIXME` をコミットに含めたまま完了扱いにすること。
- `suppress` アノテーションで警告を黙認すること（根本対処が必要）。
- ハードコードされた文字列・数値をコードに直接埋め込むこと。

---

## Security Threat Modeling

実装前にセキュリティリスクを洗い出す。

### Threat Modeling Steps

1. **アセットの特定**: 保護すべきデータを列挙する（単語データ、学習進捗、設定値など）。
2. **脅威の列挙**: STRIDE モデル（なりすまし・改ざん・否認・情報漏洩・DoS・権限昇格）で脅威を検討する。
3. **リスク評価**: 各脅威の発生可能性と影響度を評価する。
4. **対策の決定**: リスクに対応する実装方針を決める。

### Android-Specific Security Rules

- Room データベースには機密情報を平文で保存しない（必要なら暗号化を検討）。
- `SharedPreferences` への機密データ保存を禁止する（DataStore + 暗号化を使用）。
- ログ出力（`Log.d` など）に個人情報・機密情報を含めない。
- バックアップ設定（`android:allowBackup`）を意図的に制御する。
- 外部入力（ユーザー入力・Intent）はバリデーションしてから使用する。
- WebView を使う場合は JavaScript の有効化範囲を最小化する。

### Security Review Trigger

- 新しいデータ永続化処理を追加するとき。
- 外部入力を処理するロジックを追加するとき。
- パーミッション要求を変更するとき。

---

## Performance Analysis

パフォーマンスを計測・検証してから完了とする。

### Rules

- UI 変更後は Compose の recomposition が不必要に増えていないことを確認する。
- リスト表示（`LazyColumn` / `LazyRow`）では `key` を必ず指定する。
- ViewModel から大量データを一括ロードすることを禁止する（ページング / Flow の遅延評価を使用）。
- Room クエリは `Flow` で購読し、UI スレッドでの同期クエリを禁止する。
- 画像・アセットのサイズを最適化する（不必要に大きなリソースを追加しない）。

### Measurement Points

- **起動時間**: Cold start で 2 秒以内を目安とする。
- **画面遷移**: 300ms 以内に次画面が表示されることを確認する。
- **クイズ応答**: 回答選択から次問題表示まで 100ms 以内。
- **DB クエリ**: 単一クエリが 50ms を超える場合は最適化を検討する。

### Tools

- Android Studio Profiler（CPU / Memory / Network）
- Compose Layout Inspector（recomposition 回数の確認）
- `./gradlew assembleRelease` でリリースビルドのサイズを確認する。
