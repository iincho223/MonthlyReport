# API-07 月報詳細取得

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

- API ID: API-07
- 名称: 月報詳細取得
- Method/URI: `POST /api/v1/reports/detail`
- 認証: 必須
- アクセス可能ロール: **全ロール**（NG を除く）

## 2 概要

指定された月報の詳細情報を返却する。
一覧と同じデータスコープ制御を適用し、閲覧可能なデータのみ返却する。

## 3 前提条件

- 認証済み JWT が有効であること。
- 指定 `reportId` がログインユーザーの参照スコープ内に存在すること。

## 4 入出力仕様

### 4.1 Request

```json
{
  "reportId": "01HXYZ..."
}
```

### 4.2 Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| reportId | 必須 | - |

### 4.3 Response(params)

```json
{
  "reportId": "01HXYZ...",
  "month": "2026-03",
  "salesInfo": "特になし",
  "nextMonthOvertimeHours": 20,
  "nextMonthOvertimeReason": "案件リリース対応",
  "thisMonthOvertimeHours": 18,
  "thisMonthOvertimeReason": "障害調査",
  "conditions": {
    "physical": "GOOD",
    "stress": "WARN",
    "relationships": "BEST",
    "worries": "WARN",
    "fatigue": "NG",
    "sleep": "WARN",
    "motivation": "GOOD"
  },
  "comments": "相談事項あり",
  "author": {
    "employeeNo": "EMP004",
    "name": "山田 健太",
    "role": "TM",
    "officeCode": "TOKYO",
    "teamCode": "TEAM_A"
  },
  "feedback": {
    "feedbackComment": null,
    "responderRole": null,
    "responderName": null,
    "respondedAt": null
  },
  "updatedAt": "2026-03-08T09:30:00+09:00"
}
```

## 5 ロジックフロー

1. JWT からログインユーザーのロール/所属を取得する。
2. `reportId` で `reports` テーブルを検索する。
   - 存在しない場合は、以下の処理を実行する。
     - `REPORT_404` を返す。
   - 存在する場合は、以下の処理を実行する。
     - 次のステップに進む。
3. ログインユーザーで参照可否を判定する（API-06 と同スコープ制御）。
   - スコープ外の場合は、以下の処理を実行する。
     - `AUTH_403` を返す。
   - スコープ内の場合は、以下の処理を実行する。
     - 次のステップに進む。
4. `report_conditions` と `report_feedbacks` を取得する。
5. 詳細 DTO に整形して返却する。

## 6 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 権限不足 | `AUTH_403` | 権限がありません |
| 対象なし | `REPORT_404` | 対象の月報が存在しません |

## 7 CRUD

| テーブル | 操作 |
|---|---|
| reports | SELECT |
| report_conditions | SELECT |
| report_feedbacks | SELECT |
