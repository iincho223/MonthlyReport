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

## 2.5 フロントエンド HTTP クライアント

- フロントエンド（プロトタイプ）からの全 API 通信は **axios** インスタンス経由で行う。`fetch` / `XMLHttpRequest` の直接使用は禁止する。
- **インスタンス共通設定**:
  - `baseURL`: `/api/v1`
  - `headers`: `{ "Content-Type": "application/json" }`
- **リクエストインターセプター**: `localStorage.getItem("authToken")` の値を取得し、`Authorization: Bearer <token>` を全リクエストに自動付与する（ログイン API は未認証のためトークンなしで送信する）。
- **レスポンスインターセプター**:
  - `resultStatus !== "0"` の場合: `resultMsg` を message とする `Error` を throw する。
  - 正常時（`resultStatus === "0"`）: `response.data.params` を resolve する（`ApiResponse` ラッパーを剥がして返す）。
  - HTTP 401: `localStorage` から `authToken` を削除し、`handleLogout` を呼び出してログイン画面へ遷移する。
- 各サービスモジュール（`reportService.js` 等）は axios インスタンスが resolve した `params` のみを受け取り、`ApiResponse` 構造に直接依存しない。
- スタンドアロン版 (`app.standalone.js`) では CDN `<script>` タグで axios を読み込む（URL: `https://cdn.jsdelivr.net/npm/axios@1.x/dist/axios.min.js`）。
- モジュール版では `doc/prototype/js/services/apiClient.js` に axios インスタンスを集約する。

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
    "role": "TM",
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
  "role": "TM",
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

> `submissionRate` と `unsubmittedMembers` はTL以上のロールのみ返却。TM/NG は空配列・0%。

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
      "authorRole": "TM",
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

> **アクセス可能ロール**: TL・GL・OM・SP・SM・SA。NG・TM は全APIへのアクセス不可。ただし起票（`/escalations/create`）は TL・GL・OM のみ可能。

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
      "dueDate": "2026-03-20",
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
  "dueDate": "2026-03-20",
  "resolvedDate": null,
  "assigneeUserId": 1003,
  "assigneeName": "鈴木 一郎",
  "createdByName": "佐藤 花子",
  "createdByRole": "TL",
  "history": [
    {
      "date": "2026-03-10 09:00",
      "author": "佐藤 花子",
      "text": "本人と面談し1回目完了。来週再欠勤予定とのこと。"
    }
  ],
  "updatedAt": "2026-03-10T09:00:00+09:00"
}
```

### 3.6.3 POST `/escalations/create`

#### 業務ルール

- TL以上のみ起票可。
- 対応期日（`dueDate`）は必ず設定する。
- 対応担当者（`assigneeUserId`）は任意指定（未指定でも起票可）。

#### Request

```json
{
  "title": "長期欠勤対応",
  "targetEmployeeName": "山田 健太",
  "targetTeam": "チームA",
  "description": "長期欠勤によりチームの負荷が増加している。",
  "severity": "HIGH",
  "status": "PENDING",
  "dueDate": "2026-03-20",
  "assigneeUserId": 1003
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

- 更新可否は参照スコープに準ずる（GL・SM は自営業所全件、TL は自チーム+自分起票分、SP は自分が担当のもの、OM・SA は全件）。
- ステータスのみ変更する場合もこのAPIを使用する。
- ステータス遷移: PENDING ↔ ONGOING ↔ RESOLVED（双方向可）
- 対応期日（`dueDate`）は必ず設定する。
- ステータスを `RESOLVED` にする場合は完了期日（`resolvedDate`）を必ず設定する。
- 対応担当者（`assigneeUserId`）は TL 以上であればいつでも変更可能。ただし未設定（null）への変更は不可。

#### Request

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
  "logText": "来週再欠勤予定とのこと。次回面談を設定。"
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

## 3.7 ユーザー管理API

> **アクセス可能ロール**: GL・OM・SP・SM・SA のみ。それ以外は全APIへのアクセス不可。

### 3.7.1 POST `/users/search`

#### 概要

ユーザー一覧を検索する。ロールに応じた参照スコープをサーバーで強制する。

#### Request

```json
{
  "role": "TM",
  "officeCode": "TOKYO",
  "page": 1,
  "size": 20
}
```

#### Response(params)

```json
{
  "items": [
    {
      "userId": 1004,
      "employeeNo": "EMP004",
      "name": "山田 健太",
      "role": "TM",
      "officeCode": "TOKYO",
      "teamCode": "TEAM_A"
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

### 3.7.2 POST `/users/create`

#### 業務ルール

- GL は自グループおよび自グループ配下チームへエンジニアのみ登録可。
- OM は自営業所の全グループ・チームへエンジニアを登録可。
- SP・SM は所属営業所にエンジニア・SP・SM のユーザーを登録可。
- SA は全ユーザーを登録可。

#### Request

```json
{
  "employeeNo": "EMP010",
  "name": "新規 ユーザー",
  "role": "TM",
  "officeCode": "TOKYO",
  "teamCode": "TEAM_A",
  "password": "initialPass"
}
```

#### Response(params)

```json
{
  "userId": 1010,
  "employeeNo": "EMP010"
}
```

### 3.7.3 POST `/users/delete`

#### 業務ルール

- 自分自身のアカウントは削除不可。
- 各ロールの削除可能スコープは登録と同じ。

#### Request

```json
{
  "userId": 1010
}
```

#### Response(params)

```json
{
  "userId": 1010,
  "deleted": true
}
```

## 3.8 グループ管理API

> **アクセス可能ロール**: OM（全グループ）・GL（自グループのチームのみ）・SA のみ。

### 3.8.1 POST `/groups/search`

#### 概要

グループ一覧を検索する。

#### Request

```json
{
  "officeCode": "TOKYO",
  "groupName": "",
  "page": 1,
  "size": 20
}
```

#### Response(params)

```json
{
  "groups": [
    {
      "groupCode": "GROUP_A",
      "groupName": "グループA",
      "officeCode": "TOKYO",
      "officeName": "東京営業所",
      "glUserId": 1001,
      "glUserName": "グループリーダー名",
      "teamCount": 3,
      "memberCount": 12
    }
  ],
  "totalCount": 1,
  "page": 1,
  "size": 20
}
```

### 3.8.2 POST `/groups/create`

#### 業務ルール

- グループ登録時、GL に指定するユーザーを必須とする。
- OM は自営業所にグループを追加可。SA は全営業所に追加可。

#### Request

```json
{
  "groupCode": "GROUP_B",
  "groupName": "グループB",
  "officeCode": "TOKYO",
  "glUserId": 1005
}
```

#### Response(params)

```json
{
  "groupCode": "GROUP_B"
}
```

### 3.8.3 POST `/groups/delete`

#### 業務ルール

- GL はグループ自体の削除は不可（チームの追加・削除のみ可）。

#### Request

```json
{
  "groupCode": "GROUP_B"
}
```

#### Response(params)

```json
{
  "groupCode": "GROUP_B",
  "deleted": true
}
```

## 3.9 チーム管理API

> **アクセス可能ロール**: GL（自グループ配下）・OM（全チーム）・SA のみ。

### 3.9.1 POST `/teams/search`

#### 概要

チーム一覧を検索する。

#### Request

```json
{
  "groupCode": "GROUP_A",
  "teamName": "",
  "page": 1,
  "size": 20
}
```

#### Response(params)

```json
{
  "teams": [
    {
      "teamCode": "TEAM_A",
      "teamName": "チームA",
      "groupCode": "GROUP_A",
      "groupName": "グループA",
      "tlUserId": 1003,
      "tlUserName": "チームリーダー名",
      "memberCount": 4
    }
  ],
  "totalCount": 1,
  "page": 1,
  "size": 20
}
```

### 3.9.2 POST `/teams/create`

#### 業務ルール

- チーム登録時、TL に指定するユーザーを必須とする。
- GL は自グループ配下にチームを追加可。OM は全グループに追加可。SA は全権限。

#### Request

```json
{
  "teamCode": "TEAM_B",
  "teamName": "チームB",
  "groupCode": "GROUP_A",
  "tlUserId": 1006
}
```

#### Response(params)

```json
{
  "teamCode": "TEAM_B"
}
```

### 3.9.3 POST `/teams/delete`

#### Request

```json
{
  "teamCode": "TEAM_B"
}
```

#### Response(params)

```json
{
  "teamCode": "TEAM_B",
  "deleted": true
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
| escalation.dueDate | 必須、`yyyy-MM-dd` |
| escalation.resolvedDate | `status=RESOLVED` の場合必須、`yyyy-MM-dd` |
| escalation.logText | 必須 |
| user.employeeNo | 必須、英数字、最大20文字 |
| user.name | 必須 |
| user.password | 必須、最大128文字 |
| group.groupCode | 必須、英数字 |
| group.groupName | 必須 |
| group.glUserId | 必須（グループ登録時） |
| team.teamCode | 必須、英数字 |
| team.teamName | 必須 |
| team.tlUserId | 必須（チーム登録時） |

## 5. 権限仕様

### 月報

| ロール | 一覧参照範囲 | 月報更新 | 回答更新 | 削除 |
|---|---|---|---|---|
| NG | 不可 | 不可 | 不可 | 不可 |
| TM | 自分のみ | 自分のみ | 不可 | 自分のみ |
| TL | 自分+同一チーム | 自分のみ | 可(他者のみ) | 自分のみ |
| GL | 自分+同一営業所 | 自分のみ | 可(他者のみ) | 自分のみ |
| OM | 全件 | 自分のみ | 可(他者のみ) | 全件 |
| SP | 自分のみ | 自分のみ | 不可 | 自分のみ |
| SM | 自分+同一営業所 | 自分のみ | 不可 | 自分のみ |
| SA | 全件（マスク表示） | 不可 | 不可 | 不可 |

### エスカレーション

| ロール | 一覧参照範囲 | 起票 | 更新 | ログ追加 |
|---|---|---|---|---|
| NG / TM | 不可 | 不可 | 不可 | 不可 |
| TL | 自チーム+自分起票分 | 可 | 可(可視範囲内) | 可 |
| GL | 自営業所全件 | 可 | 可 | 可 |
| OM | 全件 | 可 | 可 | 可 |
| SP | 自分が担当のもの | 不可 | 可(可視範囲内) | 可 |
| SM | 自営業所全件 | 不可 | 可 | 可 |
| SA | 全件（マスク表示） | 不可 | 可 | 可 |

### ユーザー管理

| ロール | ユーザー登録 | ユーザー削除 | ユーザー一覧参照 |
|---|---|---|---|
| NG / TM / TL | 不可 | 不可 | 不可 |
| GL | 自グループ配下にエンジニア | 同左 | 可 |
| OM | 自営業所全体 | 同左 | 可 |
| SP / SM | エンジニア・SP・SM | 同左 | 可 |
| SA | 全ユーザー | 同左 | 可 |

### 組織管理（グループ・チーム）

| ロール | グループ登録 | グループ削除 | チーム登録 | チーム削除 |
|---|---|---|---|---|
| NG / TM / TL | 不可 | 不可 | 不可 | 不可 |
| GL | 不可 | 不可 | 自グループ配下のみ | 同左 |
| OM | 自営業所 | 自営業所 | 自営業所内全体 | 同左 |
| SP / SM | 不可 | 不可 | 不可 | 不可 |
| SA | 全権限 | 全権限 | 全権限 | 全権限 |

| ケース | HTTP | resultCd | メッセージ例 |
|---|---|---|---|
| 未認証 | 401 | AUTH_001 | ログインしてください |
| 権限不足 | 403 | AUTH_403 | 権限がありません |
| 対象なし(月報) | 404 | REPORT_404 | 対象の月報が存在しません |
| 対象なし(エスカレ) | 404 | ESC_404 | 対象のエスカレーションが存在しません |
| 重複登録 | 409 | REPORT_409 | 同一月の月報は既に存在します |
| 入力不正 | 400 | VAL_001 | 入力値を確認してください |
| 対象なし(ユーザー) | 404 | USER_404 | 対象のユーザーが存在しません |
| 対象なし(グループ) | 404 | GROUP_404 | 対象のグループが存在しません |
| 対象なし(チーム) | 404 | TEAM_404 | 対象のチームが存在しません |
| ユーザー重複 | 409 | USER_409 | 同じ社員番号が存在します |
| 重複登録(グループ) | 409 | GROUP_409 | 同じグループコードが存在します |
| 重複登録(チーム) | 409 | TEAM_409 | 同じチームコードが存在します |
| 自分自身削除 | 400 | USER_400 | 自分自身のアカウントは削除できません |

以下の画面操作はすべて axios インスタンス経由で API を呼び出す。

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

- ユーザー一覧 -> `/users/search`
- ユーザー登録 -> `/users/create`
- ユーザー削除 -> `/users/delete`
- グループ一覧 -> `/groups/search`
- グループ登録 -> `/groups/create`
- グループ削除 -> `/groups/delete`
- チーム一覧 -> `/teams/search`
- チーム登録 -> `/teams/create`
- チーム削除 -> `/teams/delete`
