# 単体テストルール（src/test/**）

このディレクトリ配下のテストを実装・レビューする際に適用するルール。プロジェクト全体のルールは [ルート CLAUDE.md](../../CLAUDE.md) を参照。

## テストレベル

- API 契約テスト: MockMvc で Controller 経由の E2E 風テストを優先。
- サービステスト: 権限分岐・境界値・重複チェックの失敗系を重点化。アウトプットはフル検証。
- SQL: 外部結合条件の抜け漏れや誤りを防止するため、必要に応じて Repository レベルで SQL の正当性を検証する。

## 最低カバレッジ（機能追加時）

- Controller: C0, C1ともに100%。
- Service: C0 100%、C1 100%。ただし、お作法的に到達不可能な例外ケースは C1 免除可（例: `IllegalArgumentException` の発生パターンなど）。
- Repository: C0 100%、C1 100%。ただし、Spring Data JPA の単純な派生クエリメソッドは C1 免除可。
- DTO: C0 100%。
- Utils / Constants: C0 100%.

## 検証観点

- `resultStatus/resultCd/resultMsg/params` の整合。
- `ResponseKeys` のキー名が契約どおりであること。
- `MessageKeys` 由来の文言が返ること。

## テストデータ

- Testcontainers(MariaDB) 上に Flyway (`V1__init_schema.sql` / `V2__seed_data.sql`) が適用する初期データ前提を崩さない。
- 追加データはテスト内で明示し、ケース間で状態依存を作らない。
