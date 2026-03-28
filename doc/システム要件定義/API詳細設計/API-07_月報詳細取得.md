# API-07 月報詳細取得

## 1 基本情報

- API ID: API-07
- 名称: 月報詳細取得
- Method/URI: `POST /api/v1/reports/detail`
- 認証: 必須

## 2 目的

指定された月報の詳細情報を返却する。

## 3 リクエスト

```json
{
  "reportId": "01HXYZ..."
}
```

## 4 バリデーション

| 項目 | ルール |
|---|---|
| reportId | 必須 |

## 5 正常レスポンス(params)

```json
{
  "reportId": "01HXYZ...",
  "month": "2026-03",
  "title": "今月の業務報告",
  "conditions": {
    "physical": "GOOD",
    "stress": "WARN",
    "relationships": "BEST",
    "worries": "WARN",
    "fatigue": "NG",
    "sleep": "WARN",
    "motivation": "GOOD"
  },
  "feedback": {
    "feedbackComment": null,
    "responderRole": null,
    "responderName": null,
    "respondedAt": null
  }
}
```

## 6 権限制御

一覧と同じデータスコープ制御を適用し、閲覧可能なデータのみ返却する。

## 7 主処理

1. `reports` と `report_conditions` と `report_feedbacks` を取得
2. ログインユーザーで参照可否を判定
3. 詳細DTOに整形
