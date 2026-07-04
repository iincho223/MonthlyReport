# MonthlyReport CLAUDE.md

月報管理システム（Java 21 + Spring Boot / プロトタイプ Vue 3）の開発ガイド。

## 前提アーキテクチャ

- Java 21 + Spring Boot 3.2、パッケージング `war`。
- API は原則 POST、ベースパスは `/api/v1`。
- Controller -> Service -> Repository（`InMemoryDataStore`）の責務分離を維持する。
- 共通レスポンスは `ApiResponse` を使用する。
- エラーは `BusinessException + ErrorCodes + MessageKeys/messages.properties` を使用する。
- レスポンスの Map キーは `ResponseKeys` を使用し、文字列リテラルを新規追加しない。
- バリデーション定数は `ValidationConstants` を使用する。
- ロールは `UserRole`（`NG/TM/TL/GL/OM/SP/SM/SA` の8種）を使用する。詳細は [業務仕様書](doc/システム要件定義/業務仕様書.md) を参照。
- フロントエンド（プロトタイプ）の API 通信には **axios** を使用する（ネイティブ `fetch` / `XMLHttpRequest` は使用しない）。

## ディレクトリ別ルール

作業対象のディレクトリに応じて、以下の `CLAUDE.md` が自動的に適用される（このルート CLAUDE.md に加えて読み込まれる）。

| ディレクトリ | ルール | 対象 |
|---|---|---|
| [src/main/java/CLAUDE.md](src/main/java/CLAUDE.md) | 設計ルール・バックエンド製造ルール | Controller/Service/DTO/Constants 等 |
| [src/main/resources/static/CLAUDE.md](src/main/resources/static/CLAUDE.md) | フロントエンド製造ルール | Vue 3 / CSS / axios |
| [doc/prototype/CLAUDE.md](doc/prototype/CLAUDE.md) | 上記フロントエンドルールを参照 | プロトタイプ実装 |
| [src/test/CLAUDE.md](src/test/CLAUDE.md) | 単体テストルール | MockMvc / Service / Repository テスト |
| [doc/システム要件定義/CLAUDE.md](doc/システム要件定義/CLAUDE.md) | 設計書ルール | API詳細設計・UI設計（画面別）等の設計書 |

## 仕様確認

仕様変更を伴う実装時は、次の文書との整合を必ず確認する。

- [doc/システム要件定義/業務仕様書.md](doc/システム要件定義/業務仕様書.md) — ロール定義・可視範囲・業務制約
- `doc/システム要件定義/API設計方針.md`
- `doc/システム要件定義/API仕様書.md`
- `doc/システム要件定義/API詳細設計/*.md`
- `doc/システム要件定義/UI設計書.md`
- `doc/システム要件定義/UI設計(画面別)/*.md`
- `doc/画面要件/画面要件.html`（UI外観・操作フローの一次基準）

現時点の実装は暫定版として扱い、仕様書と矛盾する場合は実装より仕様を正とする。実装由来の制約を新仕様として逆輸入しない（仕様書に明記された内容のみを採用する）。

## ビルド・テスト

```bash
./mvnw compile
./mvnw test
```

## 変更時チェックリスト

- メッセージ・レスポンスキー・制約値の文字列リテラルを新規追加していないか。
- 例外が `BusinessException` と `ErrorCodes` で統一されているか。
- 仕様書・API詳細設計との齟齬がないか。
- API詳細設計の見出し採番が `x` / `x.x` / `x.x.x` / `x.x.x.x`（最大4階層）になっているか。
- 新規 API・画面を追加した場合、`API仕様書.md` / `UI設計書.md` / 各詳細設計書も同時に更新したか。
- ロール・業務制約を変更した場合、`業務仕様書.md` も更新したか。
- 新規/変更 API に対するテストを追加したか。
- `./mvnw compile` と関連テストが通るか。
- フロントエンドで `fetch` / `XMLHttpRequest` を直接使用していないか（axios インスタンス経由であること）。
- レスポンスインターセプターが `ApiResponse` をアンラップしているか（サービス層で `resultStatus` を直接参照していないか）。

## 補足

- GitHub Copilot 向けの詳細ルールは `.github/copilot-instructions.md` と `.github/instructions/*.md` に別途存在する（本 CLAUDE.md 群と並存、内容は概ね対応）。ロール・業務制約など仕様側の変更は両方に反映すること。
