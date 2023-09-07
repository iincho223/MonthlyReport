package co.jp.monthlyreport.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import co.jp.monthlyreport.constants.Flag;
import co.jp.monthlyreport.entity.LoginAuthGetSyainInfoEntity;

/**
 * ログイン認証DAO.
 */
@Repository
public class LoginAuthDao {

    @PersistenceContext
    private EntityManager em;

    /** パスワード取得SQL */
    private final static String SQL_GET_PASSWD = ""
    + "SELECT "
    + "PASS_WD "
    + "FROM t_syain_password_info "
    + "WHERE "
    + "SYAIN_NO = :syainNo "
    + "AND KAISYA_CD = :kaisyaCd "
    + "AND SAKUJO_FLAG = " + Flag.FALSE.getCode() + " "
    + "";

    /**
     * パスワード取得.
     * @param syainNo 社員番号
     * @param kaisyaCd 会社コード
     * @return 社員情報
     * @throws Exception
     */
    public LoginAuthGetSyainInfoEntity getPassWord(String syainNo, String kaisyaCd) throws Exception {
        // SQL作成
        Query query = em.createNativeQuery(SQL_GET_PASSWD, LoginAuthGetSyainInfoEntity.class);
        query.setParameter("syainNo", syainNo);
        query.setParameter("kaisyaCd", kaisyaCd);

        // SQL実行
        @SuppressWarnings("unchecked")
        List<LoginAuthGetSyainInfoEntity> resultList = (List<LoginAuthGetSyainInfoEntity>)query.getResultList();

        LoginAuthGetSyainInfoEntity result;
        if (ObjectUtils.isEmpty(resultList)) {
            result = null;
        } else {
            result = resultList.get(0);
        }
        return result;
    }
}
