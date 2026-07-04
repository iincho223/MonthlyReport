# API-09 月報更新

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

- API ID: API-09
- 名称: 月報更新
- Method/URI: `POST /api/v1/reports/update`
- 認証: 必須
- アクセス可能ロール: **作成者本人のみ**

## 2 概要

既存月報を更新する。作成者本人のみ実行可。

## 3 前提条件

- 認証済み JWT が有効であること。
- 対象月報がログインユーザーの作成物であること。

## 4 入出力仕様

### 4.1 Request

```json
{
  "reportId": "01HXYZ...",
  "title": "今月の業務報告（更新）",
  "salesInfo": "案件A対応完了",
  "nextMonthOvertimeHours": 10,
  "nextMonthOvertimeReason": "保守対応",
  "thisMonthOvertimeHours": 18,
  "thisMonthOvertimeReason": "障害調査",
  "conditions": {
    "physical": "GOOD",
    "stress": "GOOD",
    "relationships": "GOOD",
    "worries": "WARN",
    "fatigue": "WARN",
    "sleep": "GOOD",
    "motivation": "GOOD"
  },
  "comments": "問題なし"
}
```

### 4.2 Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| reportId | 必須 | - |
| title | 必須 | 最大100文字 |
| salesInfo | 任意 | 最大500文字 |
| nextMonthOvertimeHours | 任意 | 0〜300 |
| nextMonthOvertimeReason | 任意 | 最大255文字 |
| thisMonthOvertimeHours | 任意 | 0〜300 |
| thisMonthOvertimeReason | 任意 | 最大255文字 |
| conditions | 必須 | 全 7 項目必須、`BEST/GOOD/WARN/NG` のみ |
| comments | 任意 | 最大500文字 |

### 4.3 Response(params)

```json
{
  "reportId": "01HXYZ...",
  "updated": true
}
```

## 5 ロジックフロー

1. JWT からログインユーザーの情報を取得する。
2. `reportId` で `reports` テーブルを検索する。
   - 存在しない場合は、以下の処理を実行する。
     - `REPORT_404` を返す。
   - 存在する場合は、以下の処理を実行する。
     - 次のステップに進む。
3. 作成者一致を確認する（`author_user_id = ログインユーザー`）。
   - 不一致の場合は、以下の処理を実行する。
     - `AUTH_403` を返す。
   - 一致する場合は、以下の処理を実行する。
     - 次のステップに進む。
4. `reports` テーブルを UPDATE する。
5. `report_conditions` テーブルを UPDATE する。
6. `updated_at/updated_by` を更新する。

## 6 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 更新権限なし | `AUTH_403` | 更新権限がありません |
| 対象なし | `REPORT_404` | 対象の月報が存在しません |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7 CRUD

| テーブル | 操作 |
|---|---|
| reports | SELECT / UPDATE |
| report_conditions | UPDATE |
