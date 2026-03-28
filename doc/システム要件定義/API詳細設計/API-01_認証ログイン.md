# API-01 認証ログイン

## 1 基本情報

- API ID: API-01
- 名称: 認証ログイン
- Method/URI: `POST /api/v1/auth/login`
- 認証: 不要

## 2 目的

社員番号とパスワードを認証し、アクセストークンとリフレッシュトークンを返却する。

## 3 リクエスト

```json
{
  "employeeNo": "EMP004",
  "password": "pass"
}
```

## 4 バリデーション

| 項目 | ルール |
|---|---|
| employeeNo | 必須、英数字、最大20 |
| password | 必須、最大128 |

## 5 正常レスポンス(params)

```json
{
  "accessToken": "jwt...",
  "refreshToken": "jwt...",
  "expiresIn": 3600,
  "userProfile": {
    "userId": 1004,
    "employeeNo": "EMP004",
    "name": "山田 健太",
    "role": "REPORTER",
    "officeCode": "TOKYO",
    "teamCode": "TEAM_A"
  }
}
```

## 6 エラー

- `AUTH_001`: 認証失敗
- `VAL_001`: 入力不正
- `SYS_500`: システムエラー

## 7 主処理

1. `users` を `employee_no` で検索
2. `delete_flag=0` / `is_active=1` を確認
3. パスワードハッシュ照合
4. JWT発行
5. `refresh_tokens` 登録
