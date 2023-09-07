package co.jp.monthlyreport.service;

import co.jp.monthlyreport.dto.request.LoginAuthRequest;
import co.jp.monthlyreport.dto.response.LoginAuthResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * ログイン認証サービスインターフェースクラス.
 */
public interface LoginAuthServise {
    /**
     * ログイン認証サービス
     * @param request ログイン認証リクエスト
     * @return ログイン認証レスポンス
     * @throws Exception
     */
    public LoginAuthResponse auth(LoginAuthRequest request, HttpServletRequest httpReq) throws Exception;
}
