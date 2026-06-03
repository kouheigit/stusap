# Spec: 効果音/メディア制御の MVVM 分離

## 目的・スコープ

結果画面の効果音再生で、UI 層（`@Composable`）が `AudioManager` /
`AudioFocusRequest` / `MediaPlayer` の低レベル制御を直接保持していた
（`CommonResultContent.kt` の `ResultContent`、`ResultScreen.kt` の
`CustomWordQuizResultContent` L185-199、`CommonAudio.kt` の `rememberSpeaker`）。

MVVM 分離違反のため、低レベルメディア制御を `ui/audio` 配下の専用プレイヤークラスへ
集約し、Composable は高レベル API のみを呼ぶ構成に修正する。

対象は既存の効果音/読み上げの再生制御のみ。再生する音・タイミング・UI 表示は変更しない
（挙動保存）。

## 入力・出力・状態

- `MediaSoundPlayer(context)`
  - `play(resId: Int, requestFocus: Boolean = true)`: raw リソース音源を再生。
    `requestFocus` が true のときのみオーディオフォーカスを取得し、完了時に手放す。
  - `dispose()`: 再生中の `MediaPlayer` を停止・解放し、フォーカスを手放す。
- `SpeechPlayer(context)`
  - `isReady: StateFlow<Boolean>`: TTS 初期化完了状態。
  - `speak(text)`: 直近 800ms の同一テキスト重複を抑止しつつ読み上げ。
  - `dispose()`: TTS を停止・shutdown し、フォーカスを手放す。
- Composable 側は `rememberMediaSoundPlayer()` / `rememberSpeaker()` で生成し、
  `DisposableEffect` で `dispose()` を呼ぶ（ライフサイクル管理のみが UI の責務）。

## UI の振る舞い

- 結果表示時にメダル効果音（フォーカスあり）を再生。満点時はさらに掛け声
  （フォーカスなし）を再生。挙動は従来と同一。
- 単語クイズ結果でもメダル効果音（フォーカスあり）を再生。

## エラーケース・エッジケース

- `MediaPlayer.create` が null / 例外 → 取得済みフォーカスを必ず手放す。
- 画面破棄（`onDispose`）時に再生中音源を確実に解放する（リーク防止）。
- TTS が初期化前に `speak` された場合は pending に退避し、初期化完了後に発話。

## 受け入れ条件

- `@Composable` 関数内に `AudioManager` / `AudioFocusRequest` / `MediaPlayer` /
  `TextToSpeech` の直接生成・制御コードが存在しないこと。
- `./gradlew assembleDebug` が成功すること。
- 効果音・読み上げの再生挙動が従来と変わらないこと。
