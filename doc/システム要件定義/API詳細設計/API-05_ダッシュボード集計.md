# API-05 ダッシュボード集計

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

- API ID: API-05
- 名称: ダッシュボード集計
- Method/URI: `POST /api/v1/dashboard/summary`
- 認証: 必須

## 2 概要

ダッシュボード画面に表示する集計値（提出数・未回答件数・提出率・未提出メンバー一覧）を返却する。
`submissionRate` および `unsubmittedMembers` はTL以上のロールのみ返却。REPORTERはそれぞれ `0` / `[]` を返却する。

## 3 前提条件

- 認証済JWTが有効であること。
- リクエストの `month` は任意。未指定の場合は現在年月（サーバタイムトン）を適用する。

## 4 入出力仕様

### 4.1 Request

```json
{
  "month": "2026-03"
}
```

### 4.2 Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| month | 任意 | `yyyy-MM` 形式 |

### 4.3 Response(params)

```json
{
  "totalReports": 120,
  "pendingFeedbackCount": 18,
  "submittedCount": 11,
  "submissionRate": 75,
  "unsubmittedMembers": [
    { "employeeNo": "EMP004", "name": "山田 健太" },
    { "employeeNo": "EMP005", "name": "伊藤 直樹" }
  ]
}
```

| 項目 | 型 | 説明 |
|---|---|---|
| totalReports | int | 参照スコープ内の月報合計件数 |
| pendingFeedbackCount | int | 回答待ち件数（TL以上のみ意味あり） |
| submittedCount | int | 今月提出済み件数 |
| submissionRate | int | 提出率(%)。TL以上のみ、REPORTERは `0` |
| unsubmittedMembers | array | 未提出メンバー一覧。TL以上のみ、REPORTERは `[]` |

## 5 ロジックフロー

1. JWTからログインユーザのロール/所属を取得する。
2. `month` 未指定の場合は現在年月を適用する。
3. ロール別スコープで `reports` を集計し `totalReports`/`pendingFeedbackCount` を算出する。
4. **TL以上のみ**: スコープ内の全メンバー数と提出済み件数を比較し、`submissionRate`＆`unsubmittedMembers` を算出する。
5. REPORTERには `submissionRate: 0, unsubmittedMembers: []` を返却する。

## 6 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7 CRUD

| テーブル | 操作 |
|---|---|
| reports | SELECT |
| report_feedbacks | SELECT |
| users | SELECT (未提出メンバー算出用) |
