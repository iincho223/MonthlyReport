package co.jp.monthlyreport.api.dto.request;

/**
 * リクエストボディを持たない API 向けの空リクエスト（例: API-03 ログアウト）。
 * インプット: なし。
 * アウトプット: なし（型シグネチャとしての役割のみ）。
 */
public record EmptyRequest() {}
