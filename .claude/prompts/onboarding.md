# Onboarding Prompt

このプロジェクトへようこそ。作業を始める前にこのファイルを読んでください。

---

## このプロジェクトについて

**アプリ**: TEPPEN スタイルの英単語学習 Android アプリ
**スタック**: Kotlin + Jetpack Compose + Room + Hilt + MVVM
**対象**: Android ネイティブアプリ（Flutter / React Native / Web への変換禁止）

---

## 作業の進め方

### 1. 作業前に必ず確認するファイル

| ファイル | 目的 |
|---------|------|
| `CLAUDE.md` | プロジェクト全体のルールと構成 |
| `CLAUDE_SYSTEM_OVERVIEW.md` | システム構成の概要 |
| `.claude/prompts/review.md` | レビュー手順 |
| `.claude/prompts/test.md` | テスト・ビルド確認手順 |
| `.claude/prompts/deploy.md` | デプロイ手順 |
| `.claude/agents/reviewer.md` | レビューエージェントの定義 |
| `.claude/agents/verifier.md` | 検証エージェントの定義 |

### 2. 作業フロー

```
要件確認 → 仕様合意 → 実装計画 → 実装 → 検証 → レビュー → デプロイ
```

**要件確認**: 曖昧な点があれば作業前に質問してください。思い込みで進めないこと。

**仕様合意**: 実装内容をユーザーと合意してから書き始めること。

**実装**: 変更スコープを最小限に保ってください。要求されていない変更は加えないこと。

**検証**: `.claude/agents/verifier.md` のゲートを全て通過してから完了を宣言すること。

**レビュー**: `.claude/agents/reviewer.md` の重大度分類に従ってレビューすること。

**デプロイ**: `.claude/prompts/deploy.md` の Step 1〜5 を順番に実行すること。

---

## 絶対に守るルール

- **検証なき完了宣言禁止**: 「たぶん動く」は完了ではない
- **スコープ外変更禁止**: 頼まれていないコードは書かない
- **同じバグを 3 回直さない**: 2 回失敗したら原因調査に切り替える
- **Room Migration 確認**: Entity を変更したら必ず Migration を確認する
- **UI 変更後は必ずビルド**: レンダリング確認なしにコミットしない

---

## ディレクトリ構成

```
app/src/main/java/com/example/vocabapp/
├── MainActivity.kt
├── ui/
│   ├── screen/          # 各画面の Composable
│   ├── components/      # 共通 UI コンポーネント
│   └── theme/           # テーマ設定
├── viewmodel/           # ViewModel
├── data/
│   ├── local/           # Room DAO / Entity / Database
│   ├── repository/      # Repository 実装
│   └── model/           # データモデル
└── domain/              # UseCase
```

---

## よくある質問

**Q: バックエンド API を追加してよいか？**
A: 明示的に依頼された場合のみ。デフォルトはローカルファーストです。

**Q: Flutter や Web に変換してよいか？**
A: 絶対に禁止です。

**Q: テストを省略してよいか？**
A: 禁止です。`.claude/prompts/test.md` を参照してください。

**Q: UI 変更をコミットする前に確認は必要か？**
A: 必須です。ビルドと画面確認なしにコミットしないでください。
