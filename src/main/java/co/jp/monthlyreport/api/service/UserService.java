package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.ResponseKeys;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
/**
 * ユーザー情報返却サービス。
 * インプット: 認証ユーザー情報。
 * アウトプット: API 応答用のユーザー情報マップ。
 */
public class UserService {
  /**
   * ログインユーザーの自分情報を返す。
   * インプット: user 認証ユーザー。
   * アウトプット: user 情報を整形したマップ。
   *
   * @param user 認証ユーザー
   * @return 自分情報
   */
  public Map<String, Object> me(AuthUser user) {
    // 画面表示に必要な項目のみ返却する。
    return Map.of(
        ResponseKeys.USER_ID, user.userId(),
        ResponseKeys.EMPLOYEE_NO, user.employeeNo(),
        ResponseKeys.NAME, user.name(),
        ResponseKeys.ROLE, user.role().name(),
        ResponseKeys.OFFICE_CODE, user.officeCode(),
        ResponseKeys.TEAM_CODE, user.teamCode());
  }
}
