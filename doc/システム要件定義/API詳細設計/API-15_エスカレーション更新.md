# API-15 エスカレーション更新

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

- API ID: API-15
- 名称: エスカレーション更新
- Method/URI: `POST /api/v1/escalations/update`
- 認証: 必須
- アクセス可能ロール: **TL・GL・OM・SP・SM・SA**（NG・TM は 403）

## 2 概要

エスカレーションの内容またはステータスを更新する。
ステータスのみ変更する場合もこの API を使用する。

### 2.1 ステータス遷移

```
PENDING（未対応） ↔ ONGOING（対応中） ↔ RESOLVED（解決済み）
```

双方向遷移を許可する。

## 3 前提条件

- 認証済み JWT が有効であること。
- ログインユーザーのロールが TL・GL・OM・SP・SM・SA のいずれかであること。
- 対象エスカレーションが参照スコープ内に存在すること。

## 4 入出力仕様

### 4.1 Request

```json
{
  "escalationId": "ESC001",
  "title": "長期欠勤対応",
  "targetEmployeeName": "山田 健太",
  "targetTeam": "チームA",
  "description": "長期欠勤によりチームの負荷が増加している。",
  "severity": "HIGH",
  "status": "ONGOING",
  "dueDate": "2026-03-20",
  "resolvedDate": null,
  "assigneeUserId": 1003
}
```

### 4.2 Request バリデーション

| 項目 | 必須 | ルール |
|---|---|---|
| escalationId | 必須 | - |
| title | 必須 | 最大200文字 |
| targetEmployeeName | 必須 | - |
| targetTeam | 必須 | - |
| description | 任意 | 最大2000文字 |
| severity | 必須 | `LOW` / `MEDIUM` / `HIGH` |
| status | 必須 | `PENDING` / `ONGOING` / `RESOLVED` |
| dueDate | 必須 | `yyyy-MM-dd` 形式 |
| resolvedDate | `status=RESOLVED` の場合必須 | `yyyy-MM-dd` 形式 |
| assigneeUserId | 任意 | 既存ユーザーID。ただし既に担当者が設定済みの場合、未設定（null）への変更は不可 |

### 4.3 Response(params)

```json
{
  "escalationId": "ESC001",
  "updated": true
}
```

## 5 ロジックフロー

1. JWT からログインユーザーのロール/所属を取得する。
2. ロールを確認する。
   - TL・GL・OM・SP・SM・SA の場合は、以下の処理を実行する。
     - 次のステップに進む。
   - NG・TM の場合は、以下の処理を実行する。
     - `AUTH_403` を返す。
3. `escalationId` で `escalations` テーブルを検索する。
   - 存在しない場合は、以下の処理を実行する。
     - `ESC_404` を返す。
   - 存在する場合は、以下の処理を実行する。
     - 次のステップに進む。
4. ロール別更新権限を確認する（参照スコープに準ずる。API-12/13 と同スコープ）。
   - OM・SA の場合は、以下の処理を実行する。
     - 全件の更新を許可する（SA は画面表示時に内容をマスクする）。
   - GL・SM の場合は、以下の処理を実行する。
     - 自営業所の案件か確認する。
   - TL の場合は、以下の処理を実行する。
     - 起票者 = ログインユーザー、または自チームの案件か確認する。
   - SP の場合は、以下の処理を実行する。
     - 担当者（`assigneeUserId`）= ログインユーザーか確認する。
5. 権限不足の場合は、以下の処理を実行する。
   - `AUTH_403` を返す。
6. バリデーションを実行する。
7. `assigneeUserId` の変更を確認する。
   - リクエストが `null` の場合は、以下の処理を実行する。
     - `ESC_400` を返す（担当者は未設定に変更できない）。
   - リクエストが値を持つ場合は、以下の処理を実行する。
     - 次のステップに進む。
8. `status` を確認する。
   - `RESOLVED` の場合は、以下の処理を実行する。
     - `resolvedDate` が未指定の場合は `VAL_001` を返す。
   - `RESOLVED` 以外の場合は、以下の処理を実行する。
     - 次のステップに進む。
9. `escalations` テーブルを UPDATE する（`updated_at/updated_by` 更新）。

## 6 例外ケース

| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 権限不足 | `AUTH_403` | 権限がありません |
| 対象なし | `ESC_404` | 対象のエスカレーションが存在しません |
| 担当者未設定への変更 | `ESC_400` | 担当者を未設定にはできません |
| 完了期日未入力（RESOLVED時） | `VAL_001` | 完了期日を入力してください |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7 CRUD

| テーブル | 操作 |
|---|---|
| escalations | SELECT / UPDATE |
