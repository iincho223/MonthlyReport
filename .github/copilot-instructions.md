# MonthlyReport Copilot Instructions

## 目的

このリポジトリで GitHub Copilot が提案する設計・実装・単体テストを、現行アーキテクチャと仕様に一致させる。

## 前提アーキテクチャ

- Java 21 + Spring Boot。
- API は原則 POST、ベースパスは `/api/v1`。
- Controller -> Service -> Repository（InMemoryDataStore）の責務分離を維持する。
- 共通レスポンスは `ApiResponse` を使用する。
- エラーは `BusinessException + ErrorCodes + MessageKeys/messages.properties` を使用する。
- レスポンスの Map キーは `ResponseKeys` を使用し、文字列リテラルを新規追加しない。
- バリデーション定数は `ValidationConstants` を使用する。
- フロントエンド（プロトタイプ）の API 通信には **axios** を使用する（ネイティブ `fetch` / `XMLHttpRequest` は使用しない）。

## 詳細ルール（instructions ファイル）

以下の分類ごとに `.github/instructions/` 配下のファイルを参照する。
該当ファイルはタスク種別に応じてオンデマンドで、またはファイルパターン一致で自動付与される。

| ファイル | 対象 |
|---------|------|
| [business-spec.instructions.md](.github/instructions/business-spec.instructions.md) | 仕様確認・ロール定義・業務制約・UI仕様 |
| [backend.instructions.md](.github/instructions/backend.instructions.md) | 設計ルール・バックエンド製造ルール（`src/main/java/**` に自動付与） |
| [frontend.instructions.md](.github/instructions/frontend.instructions.md) | フロントエンド製造ルール（`src/main/resources/static/**` 等に自動付与） |
| [testing.instructions.md](.github/instructions/testing.instructions.md) | 単体テストルール（`src/test/**` に自動付与） |
| [design.instructions.md](.github/instructions/design.instructions.md) | 設計書ルール（`doc/システム要件定義/**` に自動付与） |
| [templates.instructions.md](.github/instructions/templates.instructions.md) | Copilot 依頼テンプレート |

## 変更時チェックリスト

- メッセージ・レスポンスキー・制約値の文字列リテラルを新規追加していないか。
- 例外が `BusinessException` と `ErrorCodes` で統一されているか。
- 仕様書・API 詳細設計との齟齬がないか。
- API詳細設計の見出し採番が `x` / `x.x` / `x.x.x` / `x.x.x.x`（最大4階層）になっているか。
- 新規 API・画面を追加した場合、`API仕様書.md` / `UI設計書.md` / `README.md` も同時に更新したか。
- ロール・業務制約を変更した場合、`business-spec.instructions.md` も更新したか。
- 新規/変更 API に対するテストを追加したか。
- `./mvnw compile` と関連テストが通るか。
- フロントエンドで `fetch` / `XMLHttpRequest` を直接使用していないか（axios インスタンス経由であること）。
- レスポンスインターセプターが `ApiResponse` をアンラップしているか（サービス層で `resultStatus` を直接参照していないか）。
