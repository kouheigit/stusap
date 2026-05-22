# Deploy Prompt

作業後はデプロイ前確認を行ってください。

このプロジェクトは Kotlin + Jetpack Compose の Android ネイティブアプリです。
Room / Hilt / StateFlow / MVVM / UseCase / Repository 構成を前提にしてください。
Flutter / React Native / Web へ変換しないでください。

---

## Step 1: テスト確認（必須）

デプロイ前に必ずテストを実行してください。失敗があれば即座に停止し、報告すること。

```bash
GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest
```

テストが通った場合のみ Step 2 に進む。

---

## Step 2: ビルド成果物の確認

```bash
ls -la app/build/outputs/apk/debug/app-debug.apk
```

APK が存在し、タイムスタンプが新しいことを確認する。

---

## Step 3: ADB 接続確認

```bash
adb kill-server && adb start-server
adb devices
```

接続済みデバイスが表示されることを確認する。
表示されない場合は最大 3 回リトライ後に停止する。

---

## Step 4: インストールと起動確認

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

成功出力を確認後、アプリを起動する。

```bash
adb shell am start -n com.example.vocabapp/.MainActivity
```

---

## Step 5: ブランチ完了の選択肢

デプロイ確認が取れたら、以下のいずれかを選択して実行する。

**通常リポジトリの場合:**

1. `main` にローカルマージして cleanup
2. PR を作成して main へマージ（レビュー必要時）
3. ブランチをそのまま保持
4. 作業を破棄（要：明示的な確認）

**選択肢 4 を選んだ場合は必ずユーザーに確認を取ること。破棄は取り消せない。**

---

## 確認方針

- ビルドが通ることを確認してください
- 既存の Android ネイティブ構成を崩さないでください
- 破壊的な変更が必要な場合は理由を明記してください
- Room Migration が必要な Entity 変更を含む場合は、migration ファイルの存在を確認してください
