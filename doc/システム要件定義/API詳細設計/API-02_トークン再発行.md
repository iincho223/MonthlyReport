# API-02 トークン再発行

## 1 基本情報

- API ID: API-02
- 名称: トークン再発行
- Method/URI: `POST /api/v1/auth/refresh`
- 認証: 不要

## 2 目的

有効なリフレッシュトークンを使ってアクセストークンを再発行する。

## 3 リクエスト

```json
{
  "refreshToken": "jwt..."
}
```

## 4 バリデーション

| 項目 | ルール |
|---|---|
| refreshToken | 必須 |

## 5 正常レスポンス(params)

```json
{
  "accessToken": "jwt...",
  "refreshToken": "jwt...",
  "expiresIn": 3600
}
```

## 6 エラー

- `AUTH_002`: リフレッシュトークン無効
- `AUTH_003`: リフレッシュトークン失効
- `SYS_500`: システムエラー

## 7 主処理

1. `refresh_tokens` をトークンハッシュで照合
2. `delete_flag=0` かつ `revoked_at is null` を確認
3. 有効期限を確認
4. 新しいトークンペアを発行
5. 旧トークンを失効
