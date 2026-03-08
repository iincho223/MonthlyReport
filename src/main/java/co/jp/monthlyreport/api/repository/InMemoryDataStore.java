package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.model.ReportRecord;
import co.jp.monthlyreport.api.model.UserAccount;
import co.jp.monthlyreport.api.model.UserRole;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryDataStore {
  private final Map<String, UserAccount> usersByEmployeeNo = new ConcurrentHashMap<>();
  private final Map<Long, UserAccount> usersById = new ConcurrentHashMap<>();
  private final Map<String, ReportRecord> reports = new ConcurrentHashMap<>();

  public InMemoryDataStore() {
    registerUser(new UserAccount(1001L, "EMP001", "田中 太郎", "pass", UserRole.OM, "TOKYO", "HQ", true, false));
    registerUser(new UserAccount(1002L, "EMP002", "鈴木 一郎", "pass", UserRole.GL, "OSAKA", "SALES_WEST", true, false));
    registerUser(new UserAccount(1003L, "EMP003", "佐藤 花子", "pass", UserRole.TL, "TOKYO", "TEAM_A", true, false));
    registerUser(new UserAccount(1004L, "EMP004", "山田 健太", "pass", UserRole.REPORTER, "TOKYO", "TEAM_A", true, false));

    ReportRecord seed = new ReportRecord();
    seed.setReportId(newReportId());
    seed.setMonth("2026-03");
    seed.setTitle("今月の業務報告");
    seed.setSalesInfo("特になし");
    seed.setNextMonthOvertimeHours(20);
    seed.setNextMonthOvertimeReason("案件リリース対応");
    seed.setThisMonthOvertimeHours(18);
    seed.setThisMonthOvertimeReason("障害調査");
    seed.setComments("相談事項あり");
    seed.setAuthorUserId(1004L);
    seed.setReporterName("山田 健太");
    seed.setReporterId("EMP004");
    seed.setAuthorRole(UserRole.REPORTER);
    seed.setOfficeCode("TOKYO");
    seed.setTeamCode("TEAM_A");
    seed.setConditions(Map.of(
        "physical", "OK",
        "stress", "WARN",
        "relationships", "OK",
        "worries", "WARN",
        "fatigue", "NG",
        "sleep", "WARN",
        "motivation", "OK"));
    seed.setCreatedAt(OffsetDateTime.now().minusDays(1));
    seed.setUpdatedAt(OffsetDateTime.now().minusHours(2));
    saveReport(seed);
  }

  private void registerUser(UserAccount user) {
    usersByEmployeeNo.put(user.getEmployeeNo(), user);
    usersById.put(user.getUserId(), user);
  }

  public Optional<UserAccount> findUserByEmployeeNo(String employeeNo) {
    return Optional.ofNullable(usersByEmployeeNo.get(employeeNo));
  }

  public Optional<UserAccount> findUserById(Long userId) {
    return Optional.ofNullable(usersById.get(userId));
  }

  public Collection<ReportRecord> findAllReports() {
    return reports.values();
  }

  public Optional<ReportRecord> findReportById(String reportId) {
    return Optional.ofNullable(reports.get(reportId));
  }

  public void saveReport(ReportRecord record) {
    reports.put(record.getReportId(), record);
  }

  public String newReportId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }
}
