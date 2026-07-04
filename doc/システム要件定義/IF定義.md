# 月報管理システム IF定義

## 1. 目的

本書は、月報管理システムにおける外部/内部インターフェースを定義する。
UI動作は `doc/プロトタイプデザイン.html` を前提とする。

## 2. IF方針

- 通信方式: HTTPS + JSON
- API通信メソッド: 原則 `POST`
- 文字コード: UTF-8
- 認証方式: JWT Bearer
- 時刻形式: ISO-8601 (`yyyy-MM-dd'T'HH:mm:ssXXX`)

## 3. フロントエンド - API IF

## 3.1 共通リクエストヘッダ

| ヘッダ名 | 必須 | 説明 |
|---|---|---|
| Authorization | 認証 API 以外 Y | `Bearer {accessToken}` — axios リクエストインターセプターが `localStorage.authToken` から自動付与 |
| Content-Type | Y | `application/json` — axios インスタンスの共通設定で自動付与 |
| X-Request-Id | Y | 追跡用ID(UUID推奨) |

## 3.2 共通レスポンスIF

```json
{
  "resultStatus": "0",
  "resultMsg": null,
  "resultCd": null,
  "params": {}
}
```

| 項目 | 型 | 説明 |
|---|---|---|
| resultStatus | string | `0=成功, 1=業務エラー, 9=システムエラー` |
| resultMsg | string/null | ユーザー表示メッセージ |
| resultCd | string/null | エラーコード |
| params | object/null | 業務データ |

## 4. IF一覧

| IF-ID | IF名 | 呼出元 | 呼出先 | URI |
|---|---|---|---|---|
| IF-API-01 | ログイン認証 | ログイン画面 | API | `/api/v1/auth/login` |
| IF-API-02 | トークン再発行 | 共通処理 | API | `/api/v1/auth/refresh` |
| IF-API-03 | ログアウト | ヘッダ操作 | API | `/api/v1/auth/logout` |
| IF-API-04 | 自分情報取得 | 初期表示 | API | `/api/v1/users/me` |
| IF-API-05 | ダッシュボード集計取得 | 一覧画面 | API | `/api/v1/dashboard/summary` |
| IF-API-06 | 月報一覧検索 | 一覧画面 | API | `/api/v1/reports/search` |
| IF-API-07 | 月報詳細取得 | 詳細画面 | API | `/api/v1/reports/detail` |
| IF-API-08 | 月報作成 | 作成画面 | API | `/api/v1/reports/create` |
| IF-API-09 | 月報更新 | 編集画面 | API | `/api/v1/reports/update` |
| IF-API-10 | 月報削除 | 詳細画面 | API | `/api/v1/reports/delete` |
| IF-API-11 | 回答更新 | 詳細画面 | API | `/api/v1/reports/feedback/update` |
| IF-API-12 | エスカレーション一覧検索 | エスカレーション一覧画面 | API | `/api/v1/escalations/search` |
| IF-API-13 | エスカレーション詳細取得 | エスカレーション詳細画面 | API | `/api/v1/escalations/detail` |
| IF-API-14 | エスカレーション作成 | エスカレーション作成画面 | API | `/api/v1/escalations/create` |
| IF-API-15 | エスカレーション更新 | エスカレーション詳細/編集画面 | API | `/api/v1/escalations/update` |
| IF-API-16 | 対応ログ追加 | エスカレーション詳細画面 | API | `/api/v1/escalations/log/add` |
| IF-API-17 | ユーザー一覧検索 | ユーザー管理画面 | API | `/api/v1/users/search` |
| IF-API-18 | ユーザー登録 | ユーザー管理画面 | API | `/api/v1/users/create` |
| IF-API-19 | ユーザー削除 | ユーザー管理画面 | API | `/api/v1/users/delete` |
| IF-API-20 | グループ一覧検索 | 組織管理画面 | API | `/api/v1/groups/search` |
| IF-API-21 | グループ登録 | 組織管理画面 | API | `/api/v1/groups/create` |
| IF-API-22 | グループ削除 | 組織管理画面 | API | `/api/v1/groups/delete` |
| IF-API-23 | チーム一覧検索 | 組織管理画面 | API | `/api/v1/teams/search` |
| IF-API-24 | チーム登録 | 組織管理画面 | API | `/api/v1/teams/create` |
| IF-API-25 | チーム削除 | 組織管理画面 | API | `/api/v1/teams/delete` |

## 5. 代表IF定義(抜粋)

## 5.1 IF-API-01 ログイン認証

### Request

```json
{
  "employeeNo": "EMP004",
  "password": "pass"
}
```

### Response(params)

```json
{
  "accessToken": "jwt...",
  "refreshToken": "jwt...",
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

## 5.2 IF-API-06 月報一覧検索

### Request

```json
{
  "month": "2026-03",
  "status": "ALL",
  "page": 1,
  "size": 20,
  "sort": ["month,desc", "updatedAt,desc"]
}
```

### Response(params)

```json
{
  "items": [
    {
      "reportId": "01HXYZ...",
      "month": "2026-03",
      "title": "今月の業務報告",
      "reporterName": "山田 健太",
      "reporterId": "EMP004",
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

## 5.3 IF-API-08 月報作成

### Request

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

### Response(params)

```json
{
  "reportId": "01HXYZ..."
}
```

## 5.4 IF-API-11 回答更新

### Request

```json
{
  "reportId": "01HXYZ...",
  "feedbackComment": "体調を優先し、来月の工数調整を行ってください。"
}
```

### Response(params)

```json
{
  "reportId": "01HXYZ...",
  "feedbackRegistered": true,
  "respondedAt": "2026-03-08T10:00:00+09:00"
}
```

## 6. エラーIF

| HTTP | resultStatus | 例 | 説明 |
|---|---|---|---|
| 400 | 1 | `VAL_001` | 入力値不正 |
| 401 | 1 | `AUTH_001` | 認証失敗/期限切れ |
| 403 | 1 | `AUTH_403` | 権限不足 |
| 404 | 1 | `REPORT_404` | 対象データなし |
| 409 | 1 | `REPORT_409` | 重複(同月報) |
| 500 | 9 | `SYS_500` | システムエラー |

## 7. 連携上の注意事項

- 画面の `◎/○/▲/×` はIF境界で `BEST/GOOD/WARN/NG` に変換する。
- 一覧の表示範囲はサーバ側でロールに応じて制御し、クライアント側での絞り込みは補助用途のみとする。
- API 失敗時は axios レスポンスインターセプターが `resultMsg` を message とする `Error` に変換する。画面層は `error.message` をトーストで表示する（`ApiResponse` 層を直接参照しない）。
- HTTP 401 は axios インターセプターが共通実装する。画面層ほどの個別対応不要。
- ログイン成功時は `params.accessToken` を `localStorage.authToken` に保存する。
