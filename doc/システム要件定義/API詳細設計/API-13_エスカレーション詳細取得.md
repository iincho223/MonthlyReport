# API-13 エスカレーション詳細取得

## 目次

1. [基本情報](#1-基本情報)
2. [概要](#2-概要)
3. [前提条件](#3-前提条件)
4. [入出力仕様](#4-入出力仕様)
5. [ロジックフロー](#5-ロジックフロー)
6. [例外ケース](#6-例外ケース)
7. [CRUD](#7-crud)

---

## 1. 基本情報

- API ID: API-13
- 名称: エスカレーション詳細取得
- Method/URI: `POST /api/v1/escalations/detail`
- 認証: 必須
- アクセス可能ロール: **TL・GL・OM のみ**（REPORTER は 403）

## 2. 概要

指定されたエスカレーションの詳細情報と対応ログ（履歴）を返却する。

## 3. 前提条件

- 認証済み JWT が有効であること。
- ログインユーザーのロールが TL 以上であること。
- 指定 `escalationId` がログインユーザーの参照スコープ内に存在すること。

## 4. 入出力仕様

### Request

```json
{
  "escalationId": "ESC001"
}
```

### Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| escalationId | 必須 | - |

### Response(params)

```json
{
  "escalationId": "ESC001",
  "title": "長期欠勤対応",
  "targetEmployeeName": "山田 健太",
  "targetTeam": "チームA",
  "description": "長期欠勤によりチームの負荷が増加している。",
  "severity": "HIGH",
  "status": "ONGOING",
  "createdByName": "佐藤 花子",
  "createdByRole": "TL",
  "history": [
    {
      "logId": "LOG001",
      "date": "2026-03-10 09:00",
      "author": "佐藤 花子",
      "text": "本人と面談し1回目完了。来週再欠勤予定とのこと。"
    }
  ],
  "updatedAt": "2026-03-10T09:00:00+09:00"
}
```

### レスポンス項目定義

| 項目 | 型 | 説明 |
|---|---|---|
| escalationId | string | エスカレーションID |
| title | string | 案件タイトル |
| targetEmployeeName | string | 対象メンバー氏名 |
| targetTeam | string | 対象チーム名 |
| description | string | 詳細内容（null 可） |
| severity | string | 重要度（`LOW` / `MEDIUM` / `HIGH`） |
| status | string | ステータス（`PENDING` / `ONGOING` / `RESOLVED`） |
| createdByName | string | 起票者氏名 |
| createdByRole | string | 起票者ロール |
| history | array | 対応ログ一覧（時系列順） |
| history[].logId | string | ログID |
| history[].date | string | 記録日時（`yyyy-MM-dd HH:mm`） |
| history[].author | string | 記録者氏名 |
| history[].text | string | ログ内容 |
| updatedAt | string | 最終更新日時（ISO 8601） |

## 5. ロジックフロー

1. JWT からログインユーザーのロール/所属を取得する。
2. ロールが REPORTER の場合は `AUTH_403` を返す。
3. `escalationId` で `escalations` テーブルを検索する。
4. 対象が存在しない場合は `ESC_404` を返す。
5. ロール別アクセス可否を判定する（API-12 と同スコープ）。スコープ外の場合は `AUTH_403`。
6. `escalation_logs` テーブルから対応ログを時系列順（作成日時 ASC）で取得する。
7. 詳細 DTO に整形して返却する。

## 6. 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 権限不足 | `AUTH_403` | 権限がありません |
| 対象なし | `ESC_404` | 対象のエスカレーションが存在しません |

## 7. CRUD

| テーブル | 操作 |
|---|---|
| escalations | SELECT |
| escalation_logs | SELECT |
| users | SELECT（起票者情報結合） |
