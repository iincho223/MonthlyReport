# API-05 ダッシュボード集計

## 1. 基本情報
- API ID: API-05
- 名称: ダッシュボード集計
- Method/URI: `POST /api/v1/dashboard/summary`
- 認証: 必須

## 2. 目的
一覧画面に表示する全提出数と未回答件数を返却する。

## 3. リクエスト
```json
{
  "month": "2026-03"
}
```

## 4. バリデーション
| 項目 | ルール |
|---|---|
| month | 任意、指定時は `yyyy-MM` |

## 5. 正常レスポンス(params)
```json
{
  "totalReports": 120,
  "pendingFeedbackCount": 18
}
```

## 6. エラー
- `AUTH_001`: 未認証
- `VAL_001`: 入力不正

## 7. 主処理
1. ロールを取得
2. ロール別スコープで `reports` を集計
3. `report_feedbacks` 未登録件数を算出
