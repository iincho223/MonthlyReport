# API-16 対応ログ追加

## 目次
- [API-16 対応ログ追加](#api-16-対応ログ追加)
  - [目次](#目次)
  - [1. 基本情報](#1-基本情報)
  - [2. 概要](#2-概要)
  - [3. 前提条件](#3-前提条件)
  - [4. 入出力仕様](#4-入出力仕様)
    - [Request](#request)
    - [Request バリデーション](#request-バリデーション)
    - [Response(params)](#responseparams)
  - [5. ロジックフロー](#5-ロジックフロー)
  - [6. 例外ケース](#6-例外ケース)
  - [7. CRUD](#7-crud)

---

## 1. 基本情報
- API ID: API-16
- 名称: 対応ログ追加
- Method/URI: `POST /api/v1/escalations/log/add`
- 認証: 必須
- アクセス可能ロール: **TL・GL・OM のみ**（REPORTER は 403）

## 2. 概要
エスカレーションの対応ログ（履歴）を追記する。
ログの削除は不可とし、追記専用とする。

## 3. 前提条件
- 認証済み JWT が有効であること。
- ログインユーザーのロールが TL 以上であること。
- 対象エスカレーションが参照スコープ内に存在すること。

## 4. 入出力仕様

### Request
```json
{
  "escalationId": "ESC001",
  "logText": "来週再欠勤予定とのこと。次回面談を設定。"
}
```

### Request バリデーション
| 項目 | 必須 | ルール |
|---|---|---|
| escalationId | 必須 | - |
| logText | 必須 | 最大2000文字 |

### Response(params)
```json
{
  "escalationId": "ESC001",
  "logAdded": true,
  "logDate": "2026-03-14T10:00:00+09:00"
}
```

| 項目 | 型 | 説明 |
|---|---|---|
| escalationId | string | エスカレーションID |
| logAdded | boolean | 追加成功フラグ |
| logDate | string | ログ記録日時（ISO 8601） |

## 5. ロジックフロー
1. JWT からログインユーザーのロール/所属を取得する。
2. ロールが REPORTER の場合は `AUTH_403` を返す。
3. `escalationId` で `escalations` テーブルを検索する。存在しない場合は `ESC_404`。
4. ロール別アクセス可否を確認する（API-13 と同スコープ）。スコープ外の場合は `AUTH_403`。
5. バリデーションを実行する。
6. `escalation_logs` テーブルに INSERT する。
   - `log_text`: リクエストの `logText`
   - `author_user_id`: ログインユーザー ID
   - `created_at`: サーバータイムスタンプ
7. `escalations.updated_at` を更新する。
8. `logDate` にサーバータイムスタンプを設定して返却する。

## 6. 例外ケース
| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 権限不足 | `AUTH_403` | 権限がありません |
| 対象なし | `ESC_404` | 対象のエスカレーションが存在しません |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7. CRUD
| テーブル | 操作 |
|---|---|
| escalations | SELECT / UPDATE（`updated_at` 更新） |
| escalation_logs | INSERT |
