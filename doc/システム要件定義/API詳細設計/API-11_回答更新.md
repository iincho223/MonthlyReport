# API-11 回答更新

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

- API ID: API-11
- 名称: 回答更新
- Method/URI: `POST /api/v1/reports/feedback/update`
- 認証: 必須
- アクセス可能ロール: **TL / GL / OM のみ**（NG / TM / SP / SM / SA は 403）

## 2 概要

月報へのフィードバックを登録/更新する。自分自身が作成した月報への回答は禁止。

## 3 前提条件

- 認証済み JWT が有効であること。
- ログインユーザーのロールが TL 以上であること。
- 対象月報がログインユーザー自身の作成物でないこと。

## 4 入出力仕様

### 4.1 Request

```json
{
  "reportId": "01HXYZ...",
  "feedbackComment": "体調を優先してください。"
}
```

### 4.2 Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| reportId | 必須 | - |
| feedbackComment | 必須 | 最大2000文字 |

### 4.3 Response(params)

```json
{
  "reportId": "01HXYZ...",
  "feedbackRegistered": true,
  "responderRole": "TL",
  "responderName": "佐藤 花子",
  "respondedAt": "2026-03-08T10:00:00+09:00"
}
```

## 5 ロジックフロー

1. JWT からログインユーザーのロールを取得する。
2. ロールを確認する。
   - TL / GL / OM の場合は、以下の処理を実行する。
     - 次のステップに進む。
   - それ以外（NG / TM / SP / SM / SA）の場合は、以下の処理を実行する。
     - `AUTH_403` を返す。
3. `reportId` で対象月報の作成者を取得する。
   - 対象が存在しない場合は、以下の処理を実行する。
     - `REPORT_404` を返す。
   - 作成者がログインユーザー自身の場合は、以下の処理を実行する。
     - `AUTH_403` を返す。
   - 作成者が他ユーザーの場合は、以下の処理を実行する。
     - 次のステップに進む。
4. `report_feedbacks` テーブルに upsert する。
5. `reports.status` を `FEEDBACKED` に更新する。
6. 回答者情報を返却する。

## 6 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 権限不足 | `AUTH_403` | 権限がありません |
| 対象なし | `REPORT_404` | 対象の月報が存在しません |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7 CRUD

| テーブル | 操作 |
|---|---|
| reports | SELECT / UPDATE（status 更新） |
| report_feedbacks | SELECT / INSERT / UPDATE（upsert） |
