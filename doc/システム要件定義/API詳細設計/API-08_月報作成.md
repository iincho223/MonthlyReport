# API-08 月報作成

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

- API ID: API-08
- 名称: 月報作成
- Method/URI: `POST /api/v1/reports/create`
- 認証: 必須
- アクセス可能ロール: **TM 以上**（NG は不可）

## 2 概要

月報を新規登録する。同一ユーザーの同一月の月報（有効データ）は 1 件のみ登録可能。
登録時の所属情報を `reports.office_code/team_code` にスナップショット保存する。

## 3 前提条件

- 認証済み JWT が有効であること。
- ログインユーザーのロールが TM 以上であること。

## 4 入出力仕様

### 4.1 Request

```json
{
  "month": "2026-03",
  "salesInfo": "特になし",
  "nextMonthOvertimeHours": 20,
  "nextMonthOvertimeReason": "案件リリース対応",
  "thisMonthOvertimeHours": 18,
  "thisMonthOvertimeReason": "障害調査",
  "conditions": {
    "physical": "GOOD",
    "stress": "WARN",
    "relationships": "GOOD",
    "worries": "WARN",
    "fatigue": "NG",
    "sleep": "WARN",
    "motivation": "GOOD"
  },
  "comments": "相談事項あり"
}
```

### 4.2 Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| month | 必須 | `yyyy-MM` 形式 |
| salesInfo | 任意 | 最大500文字 |
| nextMonthOvertimeHours | 任意 | 0-300（単位: 時間） |
| nextMonthOvertimeReason | 任意 | 最大255文字 |
| thisMonthOvertimeHours | 任意 | 0-300（単位: 時間） |
| thisMonthOvertimeReason | 任意 | 最大255文字 |
| conditions | 必須 | 全 7 項目必須、`BEST/GOOD/WARN/NG` のみ |
| comments | 任意 | 最大500文字 |

### 4.3 Response(params)

```json
{
  "reportId": "01HXYZ..."
}
```

## 5 ロジックフロー

1. JWT からログインユーザーの情報を取得する。
2. ロールを確認する。
   - NG の場合は、以下の処理を実行する。
     - `AUTH_403` を返す。
   - NG 以外の場合は、以下の処理を実行する。
     - 次のステップに進む。
3. ダブル登録チェックを行う（`author_user_id + report_month + delete_flag=0`）。
   - 同月の月報が存在する場合は、以下の処理を実行する。
     - `REPORT_409` を返す。
   - 存在しない場合は、以下の処理を実行する。
     - 次のステップに進む。
4. `reports` テーブルに登録する（`office_code/team_code` をスナップショット保存）。
5. `report_conditions` テーブルに登録する。
6. トランザクションをコミットする。
7. 生成した `reportId` を返却する。

## 6 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 権限不足（NG） | `AUTH_403` | 権限がありません |
| 同月重複 | `REPORT_409` | 同月の月報が存在します |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7 CRUD

| テーブル | 操作 |
|---|---|
| reports | INSERT |
| report_conditions | INSERT |
