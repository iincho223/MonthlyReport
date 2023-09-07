package co.jp.monthlyreport.service;

import co.jp.monthlyreport.dto.response.TopInitResponse;

/**
 * TOPサービスインターフェースクラス.
 */
public interface TopService {
    /**
     * TOP画面初期表示.
     * @return TOP画面初期表示情報
     * @throws Exception
     */
    public TopInitResponse init() throws Exception;
}
