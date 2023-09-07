package co.jp.monthlyreport.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.jp.monthlyreport.constants.KengenCd;
import co.jp.monthlyreport.dao.TopInitDao;
import co.jp.monthlyreport.dto.UserSession;
import co.jp.monthlyreport.dto.response.TopInitResponse;
import co.jp.monthlyreport.entity.TopCheckGeppouTeisyutsuEntity;
import co.jp.monthlyreport.entity.TopSyainInfoEntity;
import co.jp.monthlyreport.service.TopService;
import co.jp.monthlyreport.util.DateUtils;
import co.jp.monthlyreport.util.MrLoggerUtils;
import co.jp.monthlyreport.exception.MrException;

@Service
public class TopServiceImpl implements TopService {

    @Autowired
    private UserSession session;

    @Autowired
    private MrLoggerUtils log;

    @Autowired
    private TopInitDao topInitDao;

    /** エラーコード0001 */
    private static final String ERR_0001 = "E01-MRT01-0001";
    /** エラーコード0002 */
    private static final String ERR_0002 = "E01-MRT01-0002";
    /** エラーコード0003 */
    private static final String ERR_0003 = "E01-MRT01-0003";

    @Override
    public TopInitResponse init() throws Exception {
        TopInitResponse response = new TopInitResponse();
        String syainNo = session.getSyainNo();
        String kaisyaCd = session.getKaisyaCd();
        List<String> msgList = new ArrayList<String>();

        // 社員情報の取得
        TopSyainInfoEntity topSyainInfoEntity = getSyainInfo(syainNo, kaisyaCd);

        // 提出年月の取得
        String teisyutsuYm = DateUtils.getTeisyutsuYm();

        // 月報提出情報
        TopCheckGeppouTeisyutsuEntity topCheckGeppouTeisyutsuEntity = getGeppouTeisyutsuInfo(
                syainNo,
                kaisyaCd,
                topSyainInfoEntity.getBusyoCd(),
                topSyainInfoEntity.getTmCd(),
                teisyutsuYm);

        // メッセージの作成
        if (topCheckGeppouTeisyutsuEntity.getTeisyutsuNum() <= 0) {
            // 月報が未提出
            msgList.add(Integer.parseInt(teisyutsuYm.substring(4, 6)) + "月分の月報を記入してください。");
        }
        response.setMsgList(msgList);

        // メンバ設定表示
        if (KengenCd.USER.getCode().equals(topSyainInfoEntity.getKengenCd())) {
            // 権限がユーザー
            response.setMenberSettingViewFlg(false);
        } else {
            // 権限がユーザー以外
            response.setMenberSettingViewFlg(true);
        }
        
        return response;
    }

    /**
     * 社員情報の取得
     * @param syainNo 社員番号
     * @param kaisyaCd 会社コード
     * @return 社員情報
     * @throws MrException
     */
    private TopSyainInfoEntity getSyainInfo(String syainNo, String kaisyaCd) throws MrException{
        TopSyainInfoEntity result = null;
        try {
            // 社員情報の取得
            result = topInitDao.getSyainInfo(syainNo, kaisyaCd);
        } catch (Exception e) {
            log.error(ERR_0001, e);
            throw new MrException(ERR_0001, "社員情報の取得に失敗しました。", e);
        }

        if (ObjectUtils.isEmpty(result)) {
            // 社員情報が取得できない場合
            log.error(ERR_0002);
            throw new MrException(ERR_0002, "社員情報が登録されていません。");
        }
        return result;
    }

    
    /**
     * 月報提出情報の取得.
     * @param syainNo 社員番号
     * @param kaisyaCd 会社コード
     * @param busyoCd 部署コード
     * @param tmCd チームコード
     * @param teisyutsuYm 提出年月
     * @return 月報提出情報
     * @throws MrException
     */
    private TopCheckGeppouTeisyutsuEntity getGeppouTeisyutsuInfo(String syainNo, String kaisyaCd,
            String busyoCd, String tmCd, String teisyutsuYm) throws MrException {
        try {
            // 月報提出数取得
            return topInitDao.checkGeppouTeisyutsu(syainNo,
                    kaisyaCd,
                    busyoCd,
                    tmCd,
                    teisyutsuYm);
        } catch (Exception e) {
            log.error(ERR_0003, e);
            throw new MrException(ERR_0003, "提出月報情報の取得に失敗しました。", e);
        }
    }

}
