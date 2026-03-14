# API-08 月報作成

## 1. 基本情報
- API ID: API-08
- 名称: 月報作成
- Method/URI: `POST /api/v1/reports/create`
- 認証: 必須

## 2. 目的
月報を新規登録する。

## 3. リクエスト
`API仕様書.md` の `POST /reports/create` に準拠。

## 4. バリデーション
- `month`: 必須 `yyyy-MM`
- `title`: 必須 最大100
- `overtimeHours`: 0-300
- `conditions`: 全項目必須 `BEST/GOOD/WARN/NG`

## 5. 業務ルール
- 同一ユーザーの同一月報(有効データ)は登録不可。
- 登録時の所属情報を `reports.office_code/team_code` にスナップショット保存する。

## 6. 正常レスポンス(params)
```json
{
  "reportId": "01HXYZ..."
}
```

## 7. エラー
- `REPORT_409`: 同月重複
- `VAL_001`: 入力不正

## 8. 主処理
1. 重複チェック (`author_user_id + report_month + delete_flag=0`)
2. `reports` 登録
3. `report_conditions` 登録
4. トランザクションコミット
