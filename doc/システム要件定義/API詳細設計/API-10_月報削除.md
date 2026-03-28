# API-10 月報削除

## 1 基本情報

- API ID: API-10
- 名称: 月報削除
- Method/URI: `POST /api/v1/reports/delete`
- 認証: 必須

## 2 目的

月報を論理削除する。

## 3 リクエスト

```json
{
  "reportId": "01HXYZ..."
}
```

## 4 業務ルール

- 作成者本人またはOMのみ削除可。
- `delete_flag=1` へ更新する。

## 5 正常レスポンス(params)

```json
{
  "reportId": "01HXYZ...",
  "deleted": true
}
```

## 6 エラー

- `AUTH_403`: 削除権限なし
- `REPORT_404`: 対象なし

## 7 主処理

1. 対象月報を取得
2. 権限チェック
3. `reports` を論理削除
4. 関連 `report_conditions/report_feedbacks` も論理削除
