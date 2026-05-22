# Score Review Command

このコマンドは、Claude だけでなく Codex からも利用できる保守性レビュー用コマンドです。

## Codex での使い方

ユーザーが以下のように依頼した場合は、このファイルをレビュー指示として扱ってください。

- `/prompts:scorereview`
- `/scorereview`
- `scorereview`
- `スコアレビュー`
- `保守性レビューを点数付きでして`

Codex はこのコマンドを受けたら、`.claude/prompts/review.md` と `.claude/agents/reviewer.md` を参照し、変更差分と影響範囲のある周辺コードをレビューしてください。

Codex CLI のカスタム slash prompt として使う場合は、この内容を `$CODEX_HOME/prompts/scorereview.md`（通常は `~/.codex/prompts/scorereview.md`）にも配置してください。

## レビュー前に確認するファイル

- `.claude/prompts/review.md`
- `.claude/agents/reviewer.md`
- `.claude/prompts/test.md`
- `AGENTS.md`

## レビュー前の準備

以下を確認してください。

```bash
git status --short
git log --oneline -5
git diff --stat
git diff
```

特定のコミット範囲をユーザーが指定している場合は、その範囲を優先してください。
指定がない場合は、未コミット差分を中心にレビューしてください。

## レビュー対象

このプロジェクトは Kotlin + Jetpack Compose + Room + Hilt + MVVM 構成の Android ネイティブアプリです。
Flutter / React Native / Web への変換は行わないでください。

レビュー対象は以下です。

- 今回変更されたコード
- 変更の影響範囲にある周辺コード
- Room Entity / DAO / AppDatabase / Migration に関わる変更
- ViewModel / UseCase / Repository / Compose UI の責務分離
- Navigation / StateFlow / Flow / Hilt の整合性

## 採点観点

各項目を 10 点満点で評価してください。

1. 可読性 / コード品質
   - 変数名・関数名・クラス名は適切か
   - Kotlin / Compose の一般的な書き方に沿っているか
   - 条件分岐や状態管理が読みやすいか

2. 構造 / アーキテクチャ
   - MVVM の責務分離が守られているか
   - UI に業務ロジックを書きすぎていないか
   - DAO / Repository / UseCase / ViewModel / UI の依存方向が適切か

3. ドキュメント
   - 複雑な処理や一時対応に理由が書かれているか
   - コメントが不足または過剰になっていないか
   - README / CLAUDE.md / AGENTS.md と矛盾していないか

4. テスタビリティ
   - UseCase / ViewModel / Repository をテストしやすいか
   - Compose UI にロジックが入りすぎていないか
   - Room Migration や異常系を検証しやすいか

5. 拡張性
   - 新しいクイズ形式や学習モードを追加しやすいか
   - カスタム単語・熟語・文章の追加に耐えられるか
   - 変更範囲が予測しやすい構造か

## 重大度分類

`.claude/agents/reviewer.md` の分類に従ってください。

- Critical: 即時修正必須
- Important: マージ前に解決
- Minor: 次の機会に対応可

指摘には、可能な限り `ファイル:行番号` を付けてください。

## 出力形式

```markdown
# 保守性レビュー結果

## 総合評価

XX / 50 点

## 重大度サマリー

Critical: X 件
Important: X 件
Minor: X 件

## 1. 可読性 / コード品質

点数: X / 10

良い点:
- 

問題点（重大度付き）:
- 

改善案:
- 

## 2. 構造 / アーキテクチャ

点数: X / 10

良い点:
- 

問題点（重大度付き）:
- 

改善案:
- 

## 3. ドキュメント

点数: X / 10

良い点:
- 

問題点（重大度付き）:
- 

改善案:
- 

## 4. テスタビリティ

点数: X / 10

良い点:
- 

問題点（重大度付き）:
- 

改善案:
- 

## 5. 拡張性

点数: X / 10

良い点:
- 

問題点（重大度付き）:
- 

改善案:
- 

## 優先して直すべきポイント

1. 
2. 
3. 

## マージ判定

- マージ可 / マージ不可
```

## レビュー後の確認

レビューだけを依頼された場合は、原則としてコード変更は行わず、レビュー結果を返してください。
レビューに加えて修正も依頼された場合は、修正後に `.claude/prompts/test.md` に従って必要なビルドとテストを実行してください。
