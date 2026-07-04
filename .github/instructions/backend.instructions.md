---
description: "Use when designing or implementing backend Java/Spring Boot code. Covers API design rules, Controller/Service/DTO/Constants/Utils manufacturing rules and Javadoc conventions."
applyTo: "src/main/java/**"
---

# 設計ルール

- 新規機能は最初に API 契約を明確化する。
  - エンドポイント
  - リクエスト DTO
  - レスポンス params のキー
  - 業務エラーコード
- 1 メソッド 1 責務を維持し、複雑分岐は private メソッドへ抽出する。
- 外部公開メソッドには Javadoc を付与し、インプット/アウトプットを明記する。
- 詳細設計には次の見出しを含める。
  - タイトル（API-xx 詳細設計）
  - 目次
  - 概要
  - 前提条件
  - 入出力仕様
  - ロジックフロー
  - 例外ケース
  - CRUD
  - SQL（必要な場合）
- API詳細設計の見出しは必ず採番する。
  - 採番形式は整数のみの `x` / `x.x` / `x.x.x` / `x.x.x.x`（最大4階層）とする。
  - 階層の区切りは `.` を使用し、5階層以上の見出しは作成しない。
- 仕様の重要値は定数化する。
  - 正規表現・範囲値: `ValidationConstants`
  - メッセージキー: `MessageKeys`
  - レスポンスキー: `ResponseKeys`

# バックエンド製造ルール

## 全般

- クラスには Javadoc を付与し、役割を明記する。
- メソッドには Javadoc を付与し、インプット/アウトプットを明記する。
- ロジックフローはシンプルに保ち、複雑な分岐は private メソッドへ抽出する。
- API のエンドポイントは RESTful な命名規則を使用する（例: `POST /api/v1/reports/search`）。
- マジックナンバーや文字列を直接コードに埋め込まず、定数クラス（`ValidationConstants`, `MessageKeys`, `ResponseKeys`）を使用する。ただし、その定数がプロジェクト全体で再利用されない場合は、同一クラス内の private static final 定数として定義することも許容する。

## Controller

- `@PostMapping` を使用する。
- `@Valid DTO` を受け取り、サービス戻り値を `ApiResponse.ok(...)` で返す。
- 業務ロジックを書かない。

## Service

- 業務判断・認可・整形を担当する。
- 例外文言は `messageSource + MessageKeys` で取得する。
- 文字列メッセージを直接 throw しない。

## DTO

- request DTO は `dto/request` パッケージに配置する。
- request DTO は `record` を維持する。
- バリデーションは Bean Validation で定義する。
- `@Min/@Max/@Size/@Pattern` の値は `ValidationConstants` を参照する。
- Javadoc にインプット/アウトプットを記載する。

## Constants / Utils / Model

- 定数クラスは `final class + private コンストラクタ` で定義する。
- 共通化できる処理は Utils に切り出す。
- モデルは用途に応じて Lombok（`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` など）を使用する。
- 既存の命名・パッケージ規約を崩さない。

## コメント

- 意味の明確なコードを書くことを優先し、冗長なコメントは避ける。
- 分岐、ループ、例外処理、メソッドの呼び出しなどの複雑なロジックには、処理の目的や意図を説明するコメントを付与する。
- TODO コメントは、必要な修正や改善点を明確に記述する。
- javadocへは、@param / @return / @throws を使用して、メソッドのインプット/アウトプット/例外を明確に記述する。
- 例外処理には、catch ブロック内で発生したエラーの内容や対処方法を説明するコメントを付与する。
- 設計書や仕様書との関連がある場合は、その関連性をコメントで明示する。