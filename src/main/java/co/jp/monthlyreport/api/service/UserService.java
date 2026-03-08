package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  public Map<String, Object> me(AuthUser user) {
    return Map.of(
        "userId", user.userId(),
        "employeeNo", user.employeeNo(),
        "name", user.name(),
        "role", user.role().name(),
        "officeCode", user.officeCode(),
        "teamCode", user.teamCode());
  }
}
