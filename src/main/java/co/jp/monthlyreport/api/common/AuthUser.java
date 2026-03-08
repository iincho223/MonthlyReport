package co.jp.monthlyreport.api.common;

import co.jp.monthlyreport.api.model.UserRole;

public record AuthUser(Long userId, String employeeNo, String name, UserRole role, String officeCode, String teamCode) {}
