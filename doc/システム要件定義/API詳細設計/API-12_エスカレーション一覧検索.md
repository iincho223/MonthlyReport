# API-12 エスカレーション一覧検索

## 0 目次

1. [基本情報](#1-基本情報)
2. [概要](#2-概要)
3. [前提条件](#3-前提条件)
4. [入出力仕様](#4-入出力仕様)
5. [ロジックフロー](#5-ロジックフロー)
6. [例外ケース](#6-例外ケース)
7. [CRUD](#7-crud)

---

## 1 基本情報

- API ID: API-12
- 名称: エスカレーション一覧検索
- Method/URI: `POST /api/v1/escalations/search`
- 認証: 必須
- アクセス可能ロール: **TL・GL・OM・SP・SM・SA**（NG・TM は 403）

## 2 概要

ロール別データスコープでエスカレーション一覧をページング付きで返却する。
スコープはサーバー側で強制適用し、クライアントからの上書きを許可しない。

## 3 前提条件

- 認証済み JWT が有効であること。
- ログインユーザーのロールが TL・GL・OM・SP・SM・SA のいずれかであること。

## 4 入出力仕様

### 4.1 Request

```json
{
  "status": "ALL",
  "page": 1,
  "size": 20
}
```

### 4.2 Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| status | 任意 | `ALL` / `PENDING` / `ONGOING` / `RESOLVED` |
| page | 任意 | 1以上（省略時 1） |
| size | 任意 | 1-100（省略時 20） |

### 4.3 Response(params)

```json
{
  "items": [
    {
      "escalationId": "ESC001",
      "title": "長期欠勤対応",
      "targetEmployeeName": "山田 健太",
      "targetTeam": "チームA",
      "severity": "HIGH",
      "status": "PENDING",
      "createdByName": "佐藤 花子",
      "updatedAt": "2026-03-10T09:00:00+09:00"
    }
  ],
  "paging": {
    "page": 1,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 4.4 レスポンス items 項目定義

| 項目 | 型 | 説明 |
|---|---|---|
| escalationId | string | エスカレーションID |
| title | string | 案件タイトル |
| targetEmployeeName | string | 対象メンバー氏名 |
| targetTeam | string | 対象チーム名 |
| severity | string | 重要度（`LOW` / `MEDIUM` / `HIGH`） |
| status | string | ステータス（`PENDING` / `ONGOING` / `RESOLVED`） |
| createdByName | string | 起票者氏名 |
| updatedAt | string | 最終更新日時（ISO 8601） |

## 5 ロジックフロー

1. JWT からログインユーザーのロール/所属を取得する。
2. ロールを確認する。
   - TL・GL・OM・SP・SM・SA の場合は、以下の処理を実行する。
     - 次のステップに進む。
   - NG・TM の場合は、以下の処理を実行する。
     - `AUTH_403` を返す。
3. ロール別スコープ条件を組み立てる。
   - TL の場合は、以下の処理を実行する。
     - `team_code = ログイン者のチーム OR created_by = ログイン者` 条件を付与する。
   - GL・SM の場合は、以下の処理を実行する。
     - `office_code = ログイン者の営業所` 条件を付与する。
   - SP の場合は、以下の処理を実行する。
     - `assignee_user_id = ログイン者` 条件を付与する。
   - OM・SA の場合は、以下の処理を実行する。
     - スコープ制限なしで全件を返却する（SA は画面表示時に内容をマスクする）。
4. `status` 絞り込み条件を付与する（`ALL` の場合は条件なし）。
5. `escalations` テーブルから検索し、ページング結果を返す。

## 6 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 権限不足（NG・TM） | `AUTH_403` | 権限がありません |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7 CRUD

| テーブル | 操作 |
|---|---|
| escalations | SELECT |
| users | SELECT（起票者氏名結合） |
