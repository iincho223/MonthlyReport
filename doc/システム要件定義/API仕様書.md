# 月報管理システム API仕様書

## 1. 目的

本書は、`API設計方針.md` に基づく実装用API仕様を定義する。
通信メソッドは原則 `POST` とする。

## 2. 共通仕様

## 2.1 Base URL

- `https://{host}/api/v1`

## 2.2 共通ヘッダ

| ヘッダ名 | 必須 | 内容 |
|---|---|---|
| Content-Type | Y | `application/json` |
| Authorization | 認証API以外Y | `Bearer {accessToken}` |
| X-Request-Id | Y | UUID推奨 |

## 2.3 共通レスポンス形式

```json
{
  "resultStatus": "0",
  "resultMsg": null,
  "resultCd": null,
  "params": {}
}
```

## 2.4 ステータス/エラーコード

- `0`: 正常
- `1`: 業務エラー
- `9`: システムエラー

## 3. API定義

## 3.1 認証API

### 3.1.1 POST `/auth/login`

#### 概要

社員番号/パスワードでログインし、トークンとプロフィールを返す。

#### Request

```json
{
  "employeeNo": "EMP004",
  "password": "pass"
}
```

#### Response(params)

```json
{
  "accessToken": "jwt...",
  "refreshToken": "jwt...",
  "expiresIn": 3600,
  "userProfile": {
    "userId": 1004,
    "employeeNo": "EMP004",
    "name": "山田 健太",
    "role": "REPORTER",
    "officeCode": "TOKYO",
    "teamCode": "TEAM_A"
  }
}
```

### 3.1.2 POST `/auth/refresh`

#### Request

```json
{
  "refreshToken": "jwt..."
}
```

#### Response(params)

```json
{
  "accessToken": "jwt...",
  "refreshToken": "jwt...",
  "expiresIn": 3600
}
```

### 3.1.3 POST `/auth/logout`

#### Request

```json
{}
```

#### Response(params)

```json
{
  "success": true
}
```

## 3.2 ユーザーAPI

### 3.2.1 POST `/users/me`

#### 概要

ログインユーザー情報を取得する。

#### Request

```json
{}
```

#### Response(params)

```json
{
  "userId": 1004,
  "employeeNo": "EMP004",
  "name": "山田 健太",
  "role": "REPORTER",
  "officeCode": "TOKYO",
  "teamCode": "TEAM_A"
}
```

## 3.3 ダッシュボードAPI

### 3.3.1 POST `/dashboard/summary`

#### 概要

一覧画面で表示する集計値を返す。

#### Request

```json
{
  "month": "2026-03"
}
```

#### Response(params)

```json
{
  "totalReports": 120,
  "pendingFeedbackCount": 18,
  "submissionRate": 75,
  "unsubmittedMembers": [
    { "employeeNo": "EMP004", "name": "山田 健太" },
    { "employeeNo": "EMP005", "name": "伊藤 直樹" }
  ]
}
```

> `submissionRate` と `unsubmittedMembers` はTL以上のロールのみ返却。REPORTERは空配列・0%。

## 3.4 月報API

### 3.4.1 POST `/reports/search`

#### 概要

月報一覧を検索する。ロール別データスコープはサーバで強制する。

#### Request

```json
{
  "month": "2026-03",
  "status": "ALL",
  "page": 1,
  "size": 20,
  "sort": ["month,desc", "updatedAt,desc"]
}
```

#### Response(params)

```json
{
  "items": [
    {
      "reportId": "01HXYZ...",
      "month": "2026-03",
      "title": "今月の業務報告",
      "reporterName": "山田 健太",
      "reporterId": "EMP004",
      "authorRole": "REPORTER",
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

### 3.4.2 POST `/reports/detail`

#### Request

```json
{
  "reportId": "01HXYZ..."
}
```

#### Response(params)

```json
{
  "reportId": "01HXYZ...",
  "month": "2026-03",
  "title": "今月の業務報告",
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
  "comments": "相談事項あり",
  "author": {
    "employeeNo": "EMP004",
    "name": "山田 健太",
    "role": "REPORTER",
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

### 3.4.3 POST `/reports/create`

#### 業務ルール

- 同一ユーザーの同一月報(有効データ)は重複不可。

#### Request

```json
{
  "month": "2026-03",
  "title": "今月の業務報告",
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

#### Response(params)

```json
{
  "reportId": "01HXYZ..."
}
```

### 3.4.4 POST `/reports/update`

#### 業務ルール

- 作成者本人のみ更新可。

#### Request

```json
{
  "reportId": "01HXYZ...",
  "title": "今月の業務報告(更新)",
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

#### Response(params)

```json
{
  "reportId": "01HXYZ...",
  "updated": true
}
```

### 3.4.5 POST `/reports/delete`

#### 業務ルール

- 作成者本人またはOMのみ削除可。
- 論理削除(`delete_flag=1`)で扱う。

#### Request

```json
{
  "reportId": "01HXYZ..."
}
```

#### Response(params)

```json
{
  "reportId": "01HXYZ...",
  "deleted": true
}
```

## 3.5 フィードバックAPI

### 3.5.1 POST `/reports/feedback/update`

#### 業務ルール

- TL以上のみ操作可。
- 自分自身が作成した月報への回答は禁止。

#### Request

```json
{
  "reportId": "01HXYZ...",
  "feedbackComment": "体調を優先し、来月の工数調整を行ってください。"
}
```

#### Response(params)

```json
{
  "reportId": "01HXYZ...",
  "feedbackRegistered": true,
  "responderRole": "TL",
  "responderName": "佐藤 花子",
  "respondedAt": "2026-03-08T10:00:00+09:00"
}
```

## 3.6 エスカレーションAPI

> **アクセス可能ロール**: TL・GL・OMのみ。REPORTERは全APIへのアクセス不可。

### 3.6.1 POST `/escalations/search`

#### 概要

エスカレーション一覧を検索する。ロール別データスコープはサーバで強制する。

#### Request

```json
{
  "status": "ALL",
  "page": 1,
  "size": 20
}
```

#### Response(params)

```json
{
  "items": [
    {
      "escalationId": "ESC001",
      "title": "長期欠勤対応",
      "targetEmployeeName": "山田 健太",
      "targetTeam": "チームA",
      "severity": "HIGH",
      "status": "PENDING",
      "createdByName": "佐藤 花子",
      "updatedAt": "2026-03-10T09:00:00+09:00"
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

### 3.6.2 POST `/escalations/detail`

#### Request

```json
{
  "escalationId": "ESC001"
}
```

#### Response(params)

```json
{
  "escalationId": "ESC001",
  "title": "長期欠勤対応",
  "targetEmployeeName": "山田 健太",
  "targetTeam": "チームA",
  "description": "長期欠勤によりチームの負荷が増加している。",
  "severity": "HIGH",
  "status": "PENDING",
  "createdByName": "佐藤 花子",
  "createdByRole": "TL",
  "history": [
    {
      "date": "2026-03-10 09:00",
      "author": "佐藤 花子",
      "text": "本人と面談で1履目完了。来週再劤漏勤予定とのこと。"
    }
  ],
  "updatedAt": "2026-03-10T09:00:00+09:00"
}
```

### 3.6.3 POST `/escalations/create`

#### 業務ルール

- TL以上のみ起票可。

#### Request

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

#### Response(params)

```json
{
  "escalationId": "ESC001"
}
```

### 3.6.4 POST `/escalations/update`

#### 業務ルール

- 起票者またはGL以上が更新可。
- ステータスのみ変更する場合もこのAPIを使用する。
- ステータス遷移: PENDING ↔ ONGOING ↔ RESOLVED（双方向可）

#### Request

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

#### Response(params)

```json
{
  "escalationId": "ESC001",
  "updated": true
}
```

### 3.6.5 POST `/escalations/log/add`

#### 業務ルール

- 対応ログは追記のみ可。削除は不可。

#### Request

```json
{
  "escalationId": "ESC001",
  "logText": "来週再劤漏勤予定とのこと。次回面談を設定。"
}
```

#### Response(params)

```json
{
  "escalationId": "ESC001",
  "logAdded": true,
  "logDate": "2026-03-14T10:00:00+09:00"
}
```

## 4. バリデーション仕様

| 項目 | ルール |
|---|---|
| employeeNo | 必須、英数字 |
| password | 必須 |
| month | 必須、`yyyy-MM` |
| title | 必須、最大100 |
| overtimeHours | 0-300 |
| overtimeReason | 最大255 |
| feedbackComment | 最大2000 |
| conditions | `BEST/GOOD/WARN/NG` のみ |
| escalation.title | 必須 |
| escalation.targetEmployeeName | 必須 |
| escalation.severity | `LOW/MEDIUM/HIGH` のみ |
| escalation.status | `PENDING/ONGOING/RESOLVED` のみ |
| escalation.logText | 必須 |

## 5. 権限仕様

### 月報

| ロール | 一覧参照範囲 | 月報更新 | 回答更新 | 削除 |
|---|---|---|---|---|
| REPORTER | 自分のみ | 自分のみ | 不可 | 自分のみ |
| TL | 自分+同一チーム | 自分のみ | 可(他者のみ) | 自分のみ |
| GL | 自分+同一営業所 | 自分のみ | 可(他者のみ) | 自分のみ |
| OM | 全件 | 自分のみ | 可(他者のみ) | 全件 |

### エスカレーション

| ロール | 一覧参照範囲 | 起票 | 更新 | ログ追加 |
|---|---|---|---|---|
| REPORTER | 不可 | 不可 | 不可 | 不可 |
| TL | 自チーム+自分起票分 | 可 | 可(可視範囲内) | 可 |
| GL | 自営業所全件 | 可 | 可 | 可 |
| OM | 全件 | 可 | 可 | 可 |

## 6. エラー仕様

| ケース | HTTP | resultCd | メッセージ例 |
|---|---|---|---|
| 未認証 | 401 | AUTH_001 | ログインしてください |
| 権限不足 | 403 | AUTH_403 | 権限がありません |
| 対象なし(月報) | 404 | REPORT_404 | 対象の月報が存在しません |
| 対象なし(エスカレ) | 404 | ESC_404 | 対象のエスカレーションが存在しません |
| 重複登録 | 409 | REPORT_409 | 同一月の月報は既に存在します |
| 入力不正 | 400 | VAL_001 | 入力値を確認してください |
| 予期しない障害 | 500 | SYS_500 | システムエラーが発生しました |

## 7. プロトタイプ動作との対応

- ログインボタン押下 -> `/auth/login`
- 一覧初期表示 -> `/users/me`, `/dashboard/summary`, `/reports/search`
- カード押下で詳細 -> `/reports/detail`
- 新規提出 -> `/reports/create`
- 編集保存 -> `/reports/update`
- 回答公開 -> `/reports/feedback/update`
- 削除 -> `/reports/delete`
- エスカレーション一覧 -> `/escalations/search`
- エスカレーション詳細 -> `/escalations/detail`
- エスカレーション起票 -> `/escalations/create`
- エスカレーション編集/ステータス更新 -> `/escalations/update`
- 対応ログ追記 -> `/escalations/log/add`

上記により、画面要件の操作感を維持しつつ、Spring Boot + MariaDBの業務APIとして実装可能な仕様とする。
