<!--
Sync Impact Report

- Version change: (template, unratified) → 1.0.0
- Rationale: Initial ratification. Prior file contained only unfilled template
  placeholders; this is the first concrete constitution for this project.

- Modified principles: n/a (initial adoption)
- Added sections:
  - Core Principles: I. レイヤー責務分離, II. 共通レスポンス/エラーハンドリング統一,
    III. 文字列リテラル排除（定数化の徹底）, IV. ロール定義の一貫性,
    V. axios経由のHTTP通信統一, VI. テスト規律, VII. 仕様書優先の原則,
    VIII. ドキュメント同時更新義務
  - 技術スタック制約
  - 開発ワークフロー・変更時チェックリスト
  - Governance

- Removed sections: none
- Templates requiring follow-up: none — plan/spec/tasks templates read this
  file at runtime and require no structural changes for this ratification.

- Deferred placeholders: none. RATIFICATION_DATE set to the date of this
  initial ratification (not the repository's first commit date, which
  predates any constitution document).
-->

# MonthlyReport Constitution

## Core Principles

### I. レイヤー責務分離

Controller → Service → Repository（`InMemoryDataStore`）の責務分離を維持しなければならない
（MUST）。Controller は `@PostMapping` で `@Valid DTO` を受け取り、サービス戻り値を
`ApiResponse.ok(...)` で返すことに限定し、業務ロジックを書かない。業務判断・認可・整形は
Service が担う。1 メソッド 1 責務を維持し、複雑な分岐は private メソッドへ抽出する。

- 理由: 層をまたいだ責務の混在はテスト容易性と可読性を損ない、既存の単体テスト戦略
  （Controller/Service/Repository を層ごとに検証する方針）を破壊する。

### II. 共通レスポンス/エラーハンドリング統一

API レスポンスは `ApiResponse` を使用しなければならない（MUST）。例外は
`BusinessException` + `ErrorCodes` + `MessageKeys`/`messages.properties` で統一し、
Service 層で文字列メッセージを直接 throw してはならない（MUST NOT）。

- 理由: レスポンス形式とエラー表現を一元化することで、フロントエンドのレスポンス
  インターセプター（`resultStatus` 判定・`params` アンラップ）が全 API で一貫して機能する。

### III. 文字列リテラル排除（定数化の徹底）

レスポンスの Map キーは `ResponseKeys`、バリデーション定数は `ValidationConstants`、
メッセージキーは `MessageKeys` を使用し、新規の文字列リテラルを追加してはならない
（MUST NOT）。プロジェクト全体で再利用されない値に限り、同一クラス内の
`private static final` 定数として定義することを許容する。

- 理由: マジックストリングの散在は、キー名の不整合やタイポによる契約違反を検出困難にする。

### IV. ロール定義の一貫性

ロールは `UserRole`（`NG/TM/TL/GL/OM/SP/SM/SA` の8種）のみを使用しなければならない
（MUST）。旧称（`REPORTER` など）をコード・設計書・モックデータのいずれにも新規に
追加してはならない（MUST NOT）。詳細は `doc/システム要件定義/業務仕様書.md` を正とする。

- 理由: ロールは可視範囲・業務制約・画面制御の判定基盤であり、表記揺れは認可バグに
  直結する。

### V. axios経由のHTTP通信統一

フロントエンド（プロトタイプ含む）の API 通信は axios インスタンス経由でなければならず
（MUST）、ネイティブ `fetch` / `XMLHttpRequest` を直接使用してはならない（MUST NOT）。
axios インスタンスは `baseURL: /api/v1` を持ち、リクエストインターセプターで
`Authorization: Bearer <token>` を付与し、レスポンスインターセプターで `ApiResponse` を
アンラップ（`resultStatus` 判定・`params` の resolve・401時の `handleLogout`）しなければ
ならない。サービス層は `resultStatus` を直接参照してはならない（MUST NOT）。

- 理由: 通信経路を一元化することで、認証トークン付与・エラー変換・ログアウト処理を
  全画面で漏れなく適用できる。

### VI. テスト規律

新規/変更 API には対応するテストを追加しなければならない（MUST）。Controller は
MockMvc による契約テストを優先し、Service は権限分岐・境界値・重複チェックの失敗系を
重点的に検証する。最低カバレッジ基準（Controller: C0/C1 100%、Service: C0/C1 100%、
Repository: C0/C1 100%、DTO: C0 100%、Utils/Constants: C0 100%）を満たさなければ
ならない。到達不可能な例外ケースに限り C1 免除を許容する。

- 理由: 月報データの権限制御と業務制約はテストでしか担保できず、カバレッジ基準の
  形骸化は認可・業務ルールの回帰を見逃す。

### VII. 仕様書優先の原則

仕様変更を伴う実装時は `doc/システム要件定義/` 配下の仕様書群および
`doc/画面要件/画面要件.html` との整合を確認しなければならない（MUST）。現時点の実装は
暫定版として扱い、実装と仕様書が矛盾する場合は仕様書を正とする。実装由来の制約を
仕様書に明記のない新仕様として逆輸入してはならない（MUST NOT）。

- 理由: プロトタイプ実装が先行して積み上がった経緯があり、実装を仕様の代わりに参照
  すると、意図しない暫定挙動が正式仕様として固定化される。

### VIII. ドキュメント同時更新義務

新規 API・画面を追加した場合は `API仕様書.md` / `UI設計書.md` / 各詳細設計書を同時に
更新しなければならない（MUST）。ロール・業務制約を変更した場合は `業務仕様書.md`
（および `.github/instructions/business-spec.instructions.md`）も同時に更新しなければ
ならない。API詳細設計・UI設計（画面別）の見出し採番は整数のみの `x`/`x.x`/`x.x.x`/
`x.x.x.x`（最大4階層）を維持する。

- 理由: コードと設計書の更新タイミングがずれると、原則VII（仕様書優先）の前提が
  崩れ、次の実装者が古い仕様書を正として参照してしまう。

## 技術スタック制約

- バックエンドは Java 21 + Spring Boot 3.2、パッケージングは `war` を使用する。
- API は原則 POST、ベースパスは `/api/v1` とする。RESTful なエンドポイント命名
  （例: `POST /api/v1/reports/search`）を用いる。

- フロントエンド（プロトタイプ）は Vue 3 Composition API を CDN 読み込み・単一ファイル
  構成で使用する。

- テストデータは Testcontainers（MariaDB）上に Flyway（`V1__init_schema.sql` /
  `V2__seed_data.sql`）が適用する初期データを前提とし、この前提を崩さない。

## 開発ワークフロー・変更時チェックリスト

- ビルド・テストは `./mvnw compile` と `./mvnw test` で実行する。
- 変更時は以下を確認する。
  - メッセージ・レスポンスキー・制約値の文字列リテラルを新規追加していないか。
  - 例外が `BusinessException` と `ErrorCodes` で統一されているか。
  - 仕様書・API詳細設計との齟齬がないか。
  - API詳細設計の見出し採番が規定形式・階層数に収まっているか。
  - 新規 API・画面を追加した場合、関連する設計書一式を同時に更新したか。
  - ロール・業務制約を変更した場合、`業務仕様書.md` も更新したか。
  - 新規/変更 API に対するテストを追加したか。
  - `./mvnw compile` と関連テストが通るか。
  - フロントエンドで `fetch` / `XMLHttpRequest` を直接使用していないか。
  - レスポンスインターセプターが `ApiResponse` をアンラップしているか。

## Governance

この憲法（constitution）は、リポジトリ直下の `CLAUDE.md` 群（ルート
[CLAUDE.md](../../CLAUDE.md) およびディレクトリ別 CLAUDE.md）を正典とする詳細ルールの
上位governanceドキュメントであり、両者は矛盾してはならない。CLAUDE.md の記述が変更
された場合、本ファイルの該当原則も同時に見直す。GitHub Copilot 向けの
`.github/copilot-instructions.md` と `.github/instructions/*.md` も同様に、ロール・
業務制約など仕様側の変更時は本ファイルと整合させる。

**改訂手続き**: 原則・制約の変更は、変更理由と影響範囲を明示した上で本ファイルを
更新し、Sync Impact Report をファイル冒頭のコメントとして残す。

**バージョニング**: セマンティックバージョニングに従う。

- MAJOR: 原則の後方非互換な削除・再定義。
- MINOR: 新規原則・セクションの追加、既存ガイダンスの実質的拡張。
- PATCH: 文言修正・誤字修正・非意味的な明確化。

**コンプライアンスレビュー**: `/speckit-plan` および `/speckit-analyze` の実行時、
本ファイルの原則との整合性を確認する。新規/変更 PR は「開発ワークフロー・変更時
チェックリスト」を満たすことをレビュー条件とする。複雑さの導入（新規抽象化・新規
依存関係）は本ファイルの原則で正当化できない場合、却下する。

**Version**: 1.0.0 | **Ratified**: 2026-08-22 | **Last Amended**: 2026-08-22
