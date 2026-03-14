# API-15 エスカレーション更新

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
- API ID: API-15
- 名称: エスカレーション更新
- Method/URI: `POST /api/v1/escalations/update`
- 認証: 必須
- アクセス可能ロール: **TL・GL・OM のみ**（REPORTER は 403）

## 2. 概要
エスカレーションの内容またはステータスを更新する。
ステータスのみ変更する場合もこの API を使用する。

### ステータス遷移
```
PENDING（未対応） ↔ ONGOING（対応中） ↔ RESOLVED（解決済み）
```
双方向遷移を許可する。

## 3. 前提条件
- 認証済み JWT が有効であること。
- ログインユーザーのロールが TL 以上であること。
- 対象エスカレーションが参照スコープ内に存在すること。

## 4. 入出力仕様

### Request
```json
{
  "escalationId": "ESC001",
  "title": "長期欠勤対応",
  "targetEmployeeName": "山田 健太",
  "targetTeam": "チームA",
  "description": "長期欠勤によりチームの負荷が増加している。",
  "severity": "HIGH",
  "status": "ONGOING"
}
```

### Request バリデーション
| 項目 | 必須 | ルール |
|---|---|---|
| escalationId | 必須 | - |
| title | 必須 | 最大200文字 |
| targetEmployeeName | 必須 | - |
| targetTeam | 必須 | - |
| description | 任意 | 最大2000文字 |
| severity | 必須 | `LOW` / `MEDIUM` / `HIGH` |
| status | 必須 | `PENDING` / `ONGOING` / `RESOLVED` |

### Response(params)
```json
{
  "escalationId": "ESC001",
  "updated": true
}
```

## 5. ロジックフロー
1. JWT からログインユーザーのロール/所属を取得する。
2. ロールが REPORTER の場合は `AUTH_403` を返す。
3. `escalationId` で `escalations` テーブルを検索する。存在しない場合は `ESC_404`。
4. ロール別更新権限を確認する。
   - TL: 起票者 = ログインユーザー、または自チームの案件
   - GL: 自営業所の案件
   - OM: 全件
5. 権限不足の場合は `AUTH_403` を返す。
6. バリデーションを実行する。
7. `escalations` テーブルを UPDATE する（`updated_at/updated_by` 更新）。

## 6. 例外ケース
| ケース | エラーコード | 説明 |
|---|---|---|
| 未認証 | `AUTH_001` | ログインしてください |
| 権限不足 | `AUTH_403` | 権限がありません |
| 対象なし | `ESC_404` | 対象のエスカレーションが存在しません |
| 入力不正 | `VAL_001` | 入力値を確認してください |

## 7. CRUD
| テーブル | 操作 |
|---|---|
| escalations | SELECT / UPDATE |
