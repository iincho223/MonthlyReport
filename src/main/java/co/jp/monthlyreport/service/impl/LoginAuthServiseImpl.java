package co.jp.monthlyreport.service.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.jp.monthlyreport.constants.ResultStatusKbn;
import co.jp.monthlyreport.dao.LoginAuthDao;
import co.jp.monthlyreport.dto.UserSession;
import co.jp.monthlyreport.dto.request.LoginAuthRequest;
import co.jp.monthlyreport.dto.response.LoginAuthResponse;
import co.jp.monthlyreport.entity.LoginAuthGetSyainInfoEntity;
import co.jp.monthlyreport.service.LoginAuthServise;
import co.jp.monthlyreport.util.MrLoggerUtils;
import jakarta.servlet.http.HttpServletRequest;


/**
 * ログイン認証サービス実装クラス.
 */
@Service
public class LoginAuthServiseImpl implements LoginAuthServise {
    /** ログイン認証Dao */
    @Autowired
    private LoginAuthDao loginAuthDao;

    @Autowired
    private UserSession session;

    @Autowired
    private MrLoggerUtils log;

    /** 正常終了 */
    private final static String NORMAL_SUCCESS_STR = "0";
    /** 異常終了 */
    private final static String ABNORMAL_SUCCESS_STR = "1";

    /** エラーコード0001 */
    private static final String ERR_0001 = "E01-MRC01-0001";
    /** エラーコード0002 */
    private static final String ERR_0002 = "E01-MRC01-0002";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginAuthResponse auth(LoginAuthRequest request, HttpServletRequest httpReq) throws Exception {
        LoginAuthResponse response = new LoginAuthResponse();
        LoginAuthGetSyainInfoEntity loginAuthGetSyainInfo = null;
        try {
            // 社員に紐づくパスワードの取得
            loginAuthGetSyainInfo = loginAuthDao.getPassWord(request.getSyainNo(), request.getKaisyaCd());
        } catch (Exception e) {
            log.error(ERR_0001, e);
            setErrorResponse(response, ERR_0001, "パスワード情報が取得できません。");
            response.setSuccess(ABNORMAL_SUCCESS_STR);
            return response;
        }
        if (ObjectUtils.isEmpty(loginAuthGetSyainInfo)) {
            // 社員のパスワードが登録されていない場合
            log.error(ERR_0002);
            setErrorResponse(response, ERR_0002, "パスワード情報が登録されていません。");
            response.setSuccess(ABNORMAL_SUCCESS_STR);
        } else {
            // 社員のパスワードが登録されている場合
            if (loginAuthGetSyainInfo.getPassWd().equals(request.getPassword())) {
                // リクエストのパスワードと登録されているパスワードが一致
                response.setSuccess(NORMAL_SUCCESS_STR);
                response.setResultStatus(ResultStatusKbn.NORMAL.getCode());
                session.setSyainNo(request.getSyainNo());
                session.setKaisyaCd(request.getKaisyaCd());
            } else {
                // リクエストのパスワードと登録されているパスワードが不一致
                response.setSuccess(ABNORMAL_SUCCESS_STR);
                response.setResultStatus(ResultStatusKbn.NORMAL.getCode());
            }
        }
        return response;
    }


    /**
     * エラー時の共通レスポンス作成.
     * @param response レスポンス
     * @param errCd エラーコード
     * @param errMsg エラーメッセージ
     */
    private void setErrorResponse(LoginAuthResponse response, String errCd, String errMsg) {
        response.setResultCd(errCd);
        response.setResultMsg(errMsg);
        response.setResultStatus(ResultStatusKbn.ERROR.getCode());
    }
}
