# API-04 自分情報取得

## 1 基本情報

- API ID: API-04
- 名称: 自分情報取得
- Method/URI: `POST /api/v1/users/me`
- 認証: 必須

## 2 目的

ログインユーザーのプロフィールを返却する。

## 3 リクエスト

```json
{}
```

## 4 正常レスポンス(params)

```json
{
  "userId": 1004,
  "employeeNo": "EMP004",
  "name": "山田 健太",
  "role": "REPORTER",
  "officeCode": "TOKYO",
  "teamCode": "TEAM_A"
}
```

## 5 エラー

- `AUTH_001`: 未認証
- `USER_404`: ユーザー情報なし

## 6 主処理

1. JWTからユーザーIDを特定
2. `users` を参照し `delete_flag=0` を確認
3. 基本情報を返却
