# API-11 回答更新

## 1. 基本情報
- API ID: API-11
- 名称: 回答更新
- Method/URI: `POST /api/v1/reports/feedback/update`
- 認証: 必須

## 2. 目的
月報へのフィードバックを登録/更新する。

## 3. リクエスト
```json
{
  "reportId": "01HXYZ...",
  "feedbackComment": "体調を優先してください。"
}
```

## 4. バリデーション
| 項目 | ルール |
|---|---|
| reportId | 必須 |
| feedbackComment | 必須、最大2000 |

## 5. 業務ルール
- TL/GL/OM のみ実行可。
- 自分自身が作成した月報への回答は禁止。

## 6. 正常レスポンス(params)
```json
{
  "reportId": "01HXYZ...",
  "feedbackRegistered": true,
  "responderRole": "TL",
  "responderName": "佐藤 花子",
  "respondedAt": "2026-03-08T10:00:00+09:00"
}
```

## 7. エラー
- `AUTH_403`: 権限不足
- `REPORT_404`: 対象なし
- `VAL_001`: 入力不正

## 8. 主処理
1. ロールチェック
2. 対象月報の作成者チェック(自分自身禁止)
3. `report_feedbacks` upsert
4. `reports.status` を `FEEDBACKED` 更新
