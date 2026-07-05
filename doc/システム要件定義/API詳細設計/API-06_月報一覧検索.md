# API-06 月報一覧検索

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

- API ID: API-06
- 名称: 月報一覧検索
- Method/URI: `POST /api/v1/reports/search`
- 認証: 必須
- アクセス可能ロール: **全ロール**（NG を除く）

## 2 概要

月報一覧をページング付きで返却する。
ロール別データスコープはサーバー側で強制適用し、クライアントからの上書きを許可しない。

## 3 前提条件

- 認証済み JWT が有効であること。

## 4 入出力仕様

### 4.1 Request

```json
{
  "month": "2026-03",
  "status": "ALL",
  "page": 1,
  "size": 20,
  "sort": ["month,desc", "updatedAt,desc"]
}
```

### 4.2 Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| month | 任意 | 指定時 `yyyy-MM` 形式 |
| status | 任意 | `ALL` / `SUBMITTED` / `PENDING_FEEDBACK` / `FEEDBACKED` |
| page | 任意 | 1以上（省略時 1） |
| size | 任意 | 1-100（省略時 20） |
| sort | 任意 | ソート条件配列 |

### 4.3 Response(params)

```json
{
  "items": [
    {
      "reportId": "01HXYZ...",
      "month": "2026-03",
      "reporterName": "山田 健太",
      "reporterId": "EMP004",
      "authorRole": "TM",
      "officeCode": "TOKYO",
      "teamCode": "TEAM_A",
      "feedbackRegistered": false,
      "updatedAt": "2026-03-08T09:30:00+09:00"
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

## 5 ロジックフロー

1. JWT からログインユーザーのロール/所属を取得する。
2. ロール別検索スコープ条件を組み立てる。
   - TM / SP の場合は、以下の処理を実行する。
     - `author_user_id = ログイン者` 条件を付与する。
   - TL の場合は、以下の処理を実行する。
     - `author_user_id = ログイン者` または `team_code = ログイン者のチーム` 条件を付与する。
   - GL の場合は、以下の処理を実行する。
     - `author_user_id = ログイン者` または `office_code = ログイン者の営業所` 条件を付与する。
   - SM の場合は、以下の処理を実行する。
     - `office_code = ログイン者の営業所` 条件を付与する。
   - OM / SA の場合は、以下の処理を実行する。
     - スコープ制限なしで全件を返却する（SA は画面表示時に内容をマスクする）。
3. `reports` から `delete_flag=0` のみを検索する。
4. `report_feedbacks` の有無で `feedbackRegistered` を生成する。
5. ページング情報とデータを返却する。

## 6 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7 CRUD

| テーブル | 操作 |
|---|---|
| reports | SELECT |
| report_feedbacks | SELECT（回答有無判定） |
