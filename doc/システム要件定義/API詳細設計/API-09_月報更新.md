# API-09 月報更新

## 1. 基本情報
- API ID: API-09
- 名称: 月報更新
- Method/URI: `POST /api/v1/reports/update`
- 認証: 必須

## 2. 目的
既存月報を更新する。

## 3. リクエスト
`API仕様書.md` の `POST /reports/update` に準拠。

## 4. バリデーション
| 項目 | ルール |
|---|---|
| reportId | 必須 |
| title | 必須、最太100 |
| overtimeHours | 0-300 |
| overtimeReason | 最太255 |
| conditions | 全項目必須 `BEST/GOOD/WARN/NG` |

## 5. 業務ルール
- 作成者本人のみ更新可。
- 論理削除済データは更新不可。

## 6. 正常レスポンス(params)
```json
{
  "reportId": "01HXYZ...",
  "updated": true
}
```

## 7. エラー
- `AUTH_403`: 更新権限なし
- `REPORT_404`: 対象なし
- `VAL_001`: 入力不正

## 8. 主処理
1. `reports` を取得し作成者一致を確認
2. `reports` 更新
3. `report_conditions` 更新
4. `updated_at/updated_by` 更新
