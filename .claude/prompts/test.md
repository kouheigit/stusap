# Test Prompt

作業後は必ずテストとビルド確認を行ってください。

このプロジェクトは Kotlin + Jetpack Compose の Android ネイティブアプリです。
Room / Hilt / StateFlow / MVVM / UseCase / Repository 構成を前提にしてください。

Flutter / React Native / Web へ変換しないでください。

## 必須確認コマンド

基本的に以下を実行してください。

```bash
GRADLE_USER_HOME=.gradle ./gradlew assembleDebug testDebugUnitTest
```
