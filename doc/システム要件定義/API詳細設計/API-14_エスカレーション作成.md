# API-14 エスカレーション作成

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

- API ID: API-14
- 名称: エスカレーション作成
- Method/URI: `POST /api/v1/escalations/create`
- 認証: 必須
- アクセス可能ロール: **TL・GL・OM のみ**（NG・TM・SP・SM・SA は 403）

## 2 概要

エスカレーションを新規起票する。
初期ステータスは `PENDING` とし、起票者情報をスナップショット保存する。

## 3 前提条件

- 認証済み JWT が有効であること。
- ログインユーザーのロールが TL 以上であること。

## 4 入出力仕様

### 4.1 Request

```json
{
  "title": "長期欠勤対応",
  "targetEmployeeName": "山田 健太",
  "targetTeam": "チームA",
  "description": "長期欠勤によりチームの負荷が増加している。",
  "severity": "HIGH",
  "status": "PENDING"
}
```

### 4.2 Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| title | 必須 | 最大200文字 |
| targetEmployeeName | 必須 | - |
| targetTeam | 必須 | - |
| description | 任意 | 最大2000文字 |
| severity | 必須 | `LOW` / `MEDIUM` / `HIGH` |
| status | 任意 | `PENDING` / `ONGOING` / `RESOLVED`（省略時 `PENDING`） |

### 4.3 Response(params)

```json
{
  "escalationId": "ESC001"
}
```

| 項目 | 型 | 説明 |
|---|---|---|
| escalationId | string | 生成されたエスカレーションID |

## 5 ロジックフロー

1. JWT からログインユーザーのロール/所属を取得する。
2. ロールを確認する。
   - TL・GL・OM の場合は、以下の処理を実行する。
     - 次のステップに進む。
   - それ以外（NG・TM・SP・SM・SA）の場合は、以下の処理を実行する。
     - `AUTH_403` を返す。
3. バリデーションを実行する。
4. `escalations` テーブルに INSERT する。
   - `created_by`: ログインユーザー ID
   - `created_by_role`: ログインユーザーのロール
   - `office_code`: ログインユーザーの営業所コード
   - `status`: リクエスト値（省略時 `PENDING`）
5. 生成した `escalationId` を返却する。

## 6 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 権限不足（NG・TM・SP・SM・SA） | `AUTH_403` | 権限がありません |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7 CRUD

| テーブル | 操作 |
|---|---|
| escalations | INSERT |
