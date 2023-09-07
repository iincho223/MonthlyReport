package co.jp.monthlyreport.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import co.jp.monthlyreport.constants.Flag;
import co.jp.monthlyreport.entity.TopCheckGeppouTeisyutsuEntity;
import co.jp.monthlyreport.entity.TopSyainInfoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class TopInitDao {

    @PersistenceContext
    private EntityManager em;

    /** 社員情報取得SQL */
    private final static String SQL_GET_SYAIN_INFO = ""
    + "SELECT "
    + "SYOZOKU.BUSYO_CD, "
    + "SYOZOKU.TM_CD, "
    + "KENGEN.KENGEN_CD "
    + "FROM T_SYAIN_SYOZOKU AS SYOZOKU "
    + "INNER JOIN T_SYAIN_KENGEN AS KENGEN "
    + "ON SYOZOKU.SYAIN_NO = KENGEN.SYAIN_NO "
    + "AND SYOZOKU.KAISYA_CD = KENGEN.KAISYA_CD "
    + "AND SYOZOKU.BUSYO_CD = KENGEN.BUSYO_CD "
    + "AND SYOZOKU.TM_CD = KENGEN.TM_CD "
    + "AND KENGEN.SAKUJO_FLAG = " + Flag.FALSE.getCode() + " "
    + "WHERE "
    + "SYOZOKU.SYAIN_NO = :syainNo "
    + "AND SYOZOKU.KAISYA_CD = :kaisyaCd "
    + "AND SYOZOKU.SAISIN_FLAG = " + Flag.TRUE.getCode() + " "
    + "AND SYOZOKU.SAKUJO_FLAG = " + Flag.FALSE.getCode() + " "
    + "";

    /**
     * 社員情報の取得.
     * @param syainNo 社員番号
     * @param kaisyaCd 会社コード
     * @return 社員情報
     * @throws Exception
     */
    public TopSyainInfoEntity getSyainInfo(String syainNo, String kaisyaCd) throws Exception {

        // SQL作成
        Query query = em.createNativeQuery(SQL_GET_SYAIN_INFO, TopSyainInfoEntity.class);
        query.setParameter("syainNo", syainNo);
        query.setParameter("kaisyaCd", kaisyaCd);

        // SQL実行
        @SuppressWarnings("unchecked")
        List<TopSyainInfoEntity> resultList = (List<TopSyainInfoEntity>)query.getResultList();

        TopSyainInfoEntity result;
        if (ObjectUtils.isEmpty(resultList)) {
            result = null;
        } else {
            result = resultList.get(0);
        }
        return result;
    }

    /** 月報提出数取得SQL */
    private final static String SQL_GET_CHECK_GEPPOU_TEISYUTSU = ""
    + "SELECT "
    + "COUNT(SYAIN_NO) AS TEISYUTSU_NUM "
    + "FROM T_GEPPO_KAITOU_RIREKI "
    + "WHERE "
    + "SYAIN_NO = :syainNo "
    + "AND KAISYA_CD = :kaisyaCd "
    + "AND BUSYO_CD = :busyoCd "
    + "AND TM_CD = :tmCd "
    + "AND NENGETSU = :nengetsu "
    + "AND SAKUJO_FLAG = " + Flag.FALSE.getCode() + " "
    + "";

    /**
     * 月報提出数取得.
     * @param syainNo 社員番号
     * @param kaisyaCd 会社コード
     * @param busyoCd 部署コード
     * @param tmCd チームコード
     * @param nengetsu 年月
     * @return 月報提出数
     * @throws Exception
     */
    public TopCheckGeppouTeisyutsuEntity checkGeppouTeisyutsu(String syainNo, String kaisyaCd, String busyoCd, String tmCd, String nengetsu) throws Exception {

        // SQL作成
        Query query = em.createNativeQuery(SQL_GET_CHECK_GEPPOU_TEISYUTSU, TopCheckGeppouTeisyutsuEntity.class);
        query.setParameter("syainNo", syainNo);
        query.setParameter("kaisyaCd", kaisyaCd);
        query.setParameter("busyoCd", busyoCd);
        query.setParameter("tmCd", tmCd);
        query.setParameter("nengetsu", nengetsu);

        // SQL実行
        @SuppressWarnings("unchecked")
        List<TopCheckGeppouTeisyutsuEntity> resultList = (List<TopCheckGeppouTeisyutsuEntity>)query.getResultList();

        TopCheckGeppouTeisyutsuEntity result;
        if (ObjectUtils.isEmpty(resultList)) {
            result = null;
        } else {
            result = resultList.get(0);
        }
        return result;
    }

}
