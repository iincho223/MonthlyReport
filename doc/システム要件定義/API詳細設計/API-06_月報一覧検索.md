# API-06 月報一覧検索

## 1. 基本情報
- API ID: API-06
- 名称: 月報一覧検索
- Method/URI: `POST /api/v1/reports/search`
- 認証: 必須

## 2. 目的
月報一覧をページング付きで返却する。

## 3. リクエスト
```json
{
  "month": "2026-03",
  "status": "ALL",
  "page": 1,
  "size": 20,
  "sort": ["month,desc", "updatedAt,desc"]
}
```

## 4. バリデーション
| 項目 | ルール |
|---|---|
| month | 任意、指定時 `yyyy-MM` |
| page | 1以上 |
| size | 1-100 |
| status | `ALL/SUBMITTED/PENDING_FEEDBACK/FEEDBACKED` |

## 5. 正常レスポンス(params)
```json
{
  "items": [],
  "paging": {
    "page": 1,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

## 6. 権限制御
- REPORTER: 自分のみ
- TL: 自分 + 同一チーム
- GL: 自分 + 同一営業所
- OM: 全件

## 7. 主処理
1. JWTからログインユーザーのロール/所属を取得
2. ロール別検索条件を強制付与
3. `reports` から `delete_flag=0` のみ検索
4. `report_feedbacks` 有無で `feedbackRegistered` を生成
