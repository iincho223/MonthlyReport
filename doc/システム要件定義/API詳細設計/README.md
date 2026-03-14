# API詳細設計 一覧

## 1. 目的

本ディレクトリは、`API仕様書.md` をAPI単位に分解した詳細設計を管理する。

## 2. 対象API

- API-01: 認証ログイン (`POST /api/v1/auth/login`)
- API-02: トークン再発行 (`POST /api/v1/auth/refresh`)
- API-03: ログアウト (`POST /api/v1/auth/logout`)
- API-04: 自分情報取得 (`POST /api/v1/users/me`)
- API-05: ダッシュボード集計 (`POST /api/v1/dashboard/summary`)
- API-06: 月報一覧検索 (`POST /api/v1/reports/search`)
- API-07: 月報詳細取得 (`POST /api/v1/reports/detail`)
- API-08: 月報作成 (`POST /api/v1/reports/create`)
- API-09: 月報更新 (`POST /api/v1/reports/update`)
- API-10: 月報削除 (`POST /api/v1/reports/delete`)
- API-11: 回答更新 (`POST /api/v1/reports/feedback/update`)
- API-12: エスカレーション一覧検索 (`POST /api/v1/escalations/search`)
- API-13: エスカレーション詳細取得 (`POST /api/v1/escalations/detail`)
- API-14: エスカレーション作成 (`POST /api/v1/escalations/create`)
- API-15: エスカレーション更新 (`POST /api/v1/escalations/update`)
- API-16: 対応ログ追加 (`POST /api/v1/escalations/log/add`)

## 3. 共通仕様

- 通信方式: HTTPS + JSON
- 通信メソッド: 原則POST
- 認証: JWT Bearer (認証API以外)
- 共通レスポンス:

```json
{
  "resultStatus": "0",
  "resultMsg": null,
  "resultCd": null,
  "params": {}
}
```
