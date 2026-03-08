# 月報管理システム API設計方針

## 1. 目的
本書は、`/Users/iincho/Work/FarmEcSite/doc/共通/システム要件定義/アプリ設計方針.md` の方針に沿って、月報管理システムのAPI設計方針を定義する。
UI/UXは `doc/プロトタイプデザイン.html` の画面構成と動作を前提とする。

## 2. 適用方針
- バックエンドは `Java 21 + Spring Boot` を採用する。
- APIは `JSON API` を基本とし、通信メソッドは原則 `POST` に統一する。
- 将来的なマイクロサービス分割に耐えるよう `APIバージョン` を明示する。
- 認証は `Spring Security + JWT (Bearer)` を採用する。
- 永続化は `Spring Data JPA + MariaDB` を採用する。
- 応答性能向上用に、将来 `Redis` を導入可能な構造にする。
- トランザクションデータを日次差分バックアップ対象とする。

## 3. プロトタイプからの機能要件整理
プロトタイプの画面/動作をサーバサイド要件に分解する。

- ログイン
- 社員番号 + パスワードで認証
- ロール/所属(営業所・チーム)を取得
- 月報一覧
- ロールに応じた閲覧範囲制御
- REPORTER: 自分の月報のみ
- TL: 自分 + 同一チーム
- GL: 自分 + 同一営業所
- OM: 全件
- 月報作成/編集/削除
- 作成者本人のみ編集可
- 削除は作成者本人 or OM可
- 月報詳細
- 回答(フィードバック)の参照
- フィードバック登録
- TL以上が他者月報に回答可能
- 未回答件数表示

## 4. API共通設計

### 4.1 エンドポイント命名
- ベースパス: `/api/v1`
- 通信メソッドは原則 `POST` のみ利用する。
- 操作種別はパスで表現する。例: `/reports/search`, `/reports/detail`, `/reports/create`, `/reports/update`, `/reports/delete`
- 画面起点ではなく、業務リソース起点で設計する。

### 4.2 認証/認可
- 公開API: `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`
- それ以外はJWT必須。
- 認可はロール + データスコープで制御する。
- 例: `REPORTER` は自分の `reports` のみ更新可能。

### 4.3 レスポンス形式
既存 `CommonApiResponse<T>` を踏襲しつつ、HTTPステータスと合わせて返す。

```json
{
  "resultStatus": "0",
  "resultMsg": null,
  "resultCd": null,
  "params": {}
}
```

- `resultStatus`: `0=成功`, `1=業務エラー`, `9=システムエラー`
- `resultCd`: 業務エラーコード(例: `AUTH_001`, `REPORT_404`)

### 4.4 バリデーション
- 入力検証は Bean Validation を利用する。
- 主要制約
- `month`: `yyyy-MM`
- `title`: 必須、最大100文字
- `overtimeHours`: `0-300`
- `condition`: 許容値 `OK/WARN/NG` (UI表現の `○/△/×` はAPI境界でマッピング)

### 4.5 監査項目
全トランザクション系テーブルに以下を保持する。
- `registered_at`, `registered_by`
- `updated_at`, `updated_by`
- `delete_flag`

## 5. API一覧(初版)

### 5.1 認証
1. `POST /api/v1/auth/login`
- request: `employeeNo`, `password`
- response: `accessToken`, `refreshToken`, `userProfile`

2. `POST /api/v1/auth/refresh`
- request: `refreshToken`
- response: 新しいトークンペア

3. `POST /api/v1/auth/logout`
- request: なし(Authorizationヘッダ必須)
- response: 成功/失敗

### 5.2 ユーザー
1. `POST /api/v1/users/me`
- ログインユーザーのプロフィールを返す。

### 5.3 月報
1. `POST /api/v1/reports/search`
- body: `month`, `status`, `page`, `size`, `sort`
- 認可に応じてサーバ側で閲覧可能範囲を絞り込む。

2. `POST /api/v1/reports/detail`
- body: `reportId`
- 詳細取得

3. `POST /api/v1/reports/create`
- 月報作成

4. `POST /api/v1/reports/update`
- body: `reportId` + 更新項目
- 月報更新(作成者のみ)

5. `POST /api/v1/reports/delete`
- body: `reportId`
- 月報削除(作成者 or OM)

### 5.4 フィードバック
1. `POST /api/v1/reports/feedback/update`
- body: `reportId`, `feedbackComment`
- TL以上のみ、かつ自分以外の月報に回答可

### 5.5 ダッシュボード
1. `POST /api/v1/dashboard/summary`
- 戻り値: `totalReports`, `pendingFeedbackCount`

## 6. 主要DTO案

### 6.1 ReportCreateRequest
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
    "physical": "OK",
    "stress": "WARN",
    "relationships": "OK",
    "worries": "WARN",
    "fatigue": "NG",
    "sleep": "WARN",
    "motivation": "OK"
  },
  "comments": "相談事項あり"
}
```

### 6.2 ReportResponse
```json
{
  "reportId": "01HXYZ...",
  "month": "2026-03",
  "title": "今月の業務報告",
  "author": {
    "employeeNo": "EMP004",
    "name": "山田 健太",
    "role": "REPORTER",
    "officeCode": "TOKYO",
    "teamCode": "TEAM_A"
  },
  "status": "PENDING_FEEDBACK",
  "feedback": {
    "comment": "体調面を優先してください",
    "responderRole": "TL",
    "responderName": "佐藤 花子",
    "respondedAt": "2026-03-08T10:00:00+09:00"
  },
  "updatedAt": "2026-03-08T09:30:00+09:00"
}
```

## 7. エラー設計
- 400: バリデーションエラー
- 401: 未認証/JWT期限切れ
- 403: 権限不足/データスコープ外アクセス
- 404: リソースなし
- 409: 重複(月次1件制約など)
- 500: 予期しないシステムエラー

エラー応答例:

```json
{
  "resultStatus": "1",
  "resultMsg": "対象の月報にアクセスできません",
  "resultCd": "REPORT_403",
  "params": null
}
```

## 8. 非機能・運用方針への反映
- 監査ログに `userId`, `role`, `uri`, `latencyMs`, `resultCd` を記録する。
- ELK連携を前提にJSONログを出力する。
- API公開時はHTTPSのみ許可する。
- CI/CDで OpenAPI生成と契約テストを実施する。

## 9. 画面動作との対応
`doc/プロトタイプデザイン.html` の主要遷移に対するAPI利用を以下とする。

- ログイン画面送信: `POST /api/v1/auth/login`
- ダッシュボード表示: `POST /api/v1/dashboard/summary`, `POST /api/v1/reports/search`
- 新規提出: `POST /api/v1/reports/create`
- 編集保存: `POST /api/v1/reports/update`
- 詳細表示: `POST /api/v1/reports/detail`
- 回答公開: `POST /api/v1/reports/feedback/update`
- 削除: `POST /api/v1/reports/delete`

これにより、現在のフロントの画面構成・操作感を維持したまま、Firestore依存からSpring Boot + MariaDB構成へ移行できる。