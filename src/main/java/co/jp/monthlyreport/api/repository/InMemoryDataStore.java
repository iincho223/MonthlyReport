package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.model.EscalationRecord;
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
/**
 * インメモリの疑似データストア。
 * インプット: 初期ユーザー/初期月報データ、および各サービスからの保存検索要求。
 * アウトプット: ユーザー・月報の検索結果と保存結果。
 */
public class InMemoryDataStore {
  private final Map<String, UserAccount> usersByEmployeeNo = new ConcurrentHashMap<>();
  private final Map<Long, UserAccount> usersById = new ConcurrentHashMap<>();
  private final Map<String, ReportRecord> reports = new ConcurrentHashMap<>();
  private final Map<String, EscalationRecord> escalations = new ConcurrentHashMap<>();

  /**
   * 初期データを登録する。
   * インプット: なし。
   * アウトプット: ユーザーとサンプル月報がメモリ上に登録された状態。
   */
  public InMemoryDataStore() {
    // 検証用の初期ユーザーを登録する。
    registerUser(buildUser(1001L, "EMP001", "田中 太郎", "pass", UserRole.OM, "TOKYO", "HQ"));
    registerUser(buildUser(1002L, "EMP002", "鈴木 一郎", "pass", UserRole.GL, "OSAKA", "SALES_WEST"));
    registerUser(buildUser(1003L, "EMP003", "佐藤 花子", "pass", UserRole.TL, "TOKYO", "TEAM_A"));
    registerUser(buildUser(1004L, "EMP004", "山田 健太", "pass", UserRole.REPORTER, "TOKYO", "TEAM_A"));

    // 初期月報データを 1 件投入する。
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
        "physical", "GOOD",
        "stress", "WARN",
        "relationships", "BEST",
        "worries", "WARN",
        "fatigue", "NG",
        "sleep", "WARN",
        "motivation", "GOOD"));
    seed.setCreatedAt(OffsetDateTime.now().minusDays(1));
    seed.setUpdatedAt(OffsetDateTime.now().minusHours(2));
    saveReport(seed);
  }

  /**
   * UserAccount オブジェクトをセッターで組み立てるファクトリメソッド。
   * インプット: 各フィールドの値（userId, employeeNo, name, password, role, officeCode, teamCode）。
   * アウトプット: 有効フラグ=true、削除フラグ=false に設定した UserAccount。
   *
   * @param userId     ユーザーID
   * @param employeeNo 社員番号
   * @param name       氏名
   * @param password   パスワード
   * @param role       ロール
   * @param officeCode 拠点コード
   * @param teamCode   チームコード
   * @return 組み立て済みの UserAccount
   */
  private UserAccount buildUser(Long userId, String employeeNo, String name,
      String password, UserRole role, String officeCode, String teamCode) {
    // 各フィールドをセッターで個別に設定する。
    UserAccount u = new UserAccount();
    u.setUserId(userId);
    u.setEmployeeNo(employeeNo);
    u.setName(name);
    u.setPassword(password);
    u.setRole(role);
    u.setOfficeCode(officeCode);
    u.setTeamCode(teamCode);
    u.setActive(true);
    u.setDeleted(false);
    return u;
  }

  /**
   * ユーザーを社員番号とユーザーIDで登録する。
   * インプット: user ユーザー情報。
   * アウトプット: 社員番号・ID の両インデックスに登録された状態。
   *
   * @param user ユーザー情報
   */
  private void registerUser(UserAccount user) {
    usersByEmployeeNo.put(user.getEmployeeNo(), user);
    usersById.put(user.getUserId(), user);
  }

  /**
   * 社員番号でユーザーを検索する。
   * インプット: employeeNo 社員番号。
   * アウトプット: ユーザーの Optional。
   *
   * @param employeeNo 社員番号
   * @return ユーザー検索結果
   */
  public Optional<UserAccount> findUserByEmployeeNo(String employeeNo) {
    return Optional.ofNullable(usersByEmployeeNo.get(employeeNo));
  }

  /**
   * ユーザーIDでユーザーを検索する。
   * インプット: userId ユーザーID。
   * アウトプット: ユーザーの Optional。
   *
   * @param userId ユーザーID
   * @return ユーザー検索結果
   */
  public Optional<UserAccount> findUserById(Long userId) {
    return Optional.ofNullable(usersById.get(userId));
  }

  /**
   * 全ユーザーを取得する。
   * インプット: なし。
   * アウトプット: ユーザーコレクション。
   *
   * @return 全ユーザー
   */
  public Collection<UserAccount> findAllUsers() {
    return usersById.values();
  }

  /**
   * 全月報を取得する。
   * インプット: なし。
   * アウトプット: 月報コレクション。
   *
   * @return 全月報
   */
  public Collection<ReportRecord> findAllReports() {
    return reports.values();
  }

  /**
   * 月報IDで月報を検索する。
   * インプット: reportId 月報ID。
   * アウトプット: 月報の Optional。
   *
   * @param reportId 月報ID
   * @return 月報検索結果
   */
  public Optional<ReportRecord> findReportById(String reportId) {
    return Optional.ofNullable(reports.get(reportId));
  }

  /**
   * 月報を保存する。
   * インプット: record 月報レコード。
   * アウトプット: 月報ストアへ保存された状態。
   *
   * @param record 月報レコード
   */
  public void saveReport(ReportRecord record) {
    reports.put(record.getReportId(), record);
  }

  /**
   * 全エスカレーションを取得する。
   * インプット: なし。
   * アウトプット: エスカレーションコレクション。
   *
   * @return 全エスカレーション
   */
  public Collection<EscalationRecord> findAllEscalations() {
    return escalations.values();
  }

  /**
   * エスカレーションIDでエスカレーションを検索する。
   * インプット: escalationId エスカレーションID。
   * アウトプット: エスカレーションの Optional。
   *
   * @param escalationId エスカレーションID
   * @return エスカレーション検索結果
   */
  public Optional<EscalationRecord> findEscalationById(String escalationId) {
    return Optional.ofNullable(escalations.get(escalationId));
  }

  /**
   * エスカレーションを保存する。
   * インプット: record エスカレーションレコード。
   * アウトプット: エスカレーションストアへ保存された状態。
   *
   * @param record エスカレーションレコード
   */
  public void saveEscalation(EscalationRecord record) {
    escalations.put(record.getEscalationId(), record);
  }

  /**
   * 新しい月報IDを生成する。
   * インプット: なし。
   * アウトプット: 12 文字の英数字ID。
   *
   * @return 月報ID
   */
  public String newReportId() {
    // UUID からハイフンを除去し 12 文字へ切り詰める。
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }

  /**
   * 新しいエスカレーションIDを生成する。
   * インプット: なし。
   * アウトプット: 12 文字の英数字ID。
   *
   * @return エスカレーションID
   */
  public String newEscalationId() {
    return "ESC" + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
  }

  /**
   * 新しいログIDを生成する。
   * インプット: なし。
   * アウトプット: 12 文字の英数字ID。
   *
   * @return ログID
   */
  public String newLogId() {
    return "LOG" + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
  }
}
