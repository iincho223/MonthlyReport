package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.model.EscalationRecord;
import co.jp.monthlyreport.api.model.GroupRecord;
import co.jp.monthlyreport.api.model.ReportRecord;
import co.jp.monthlyreport.api.model.TeamRecord;
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
  private final Map<String, GroupRecord> groups = new ConcurrentHashMap<>();
  private final Map<String, TeamRecord> teams = new ConcurrentHashMap<>();

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
    registerUser(buildUser(1004L, "EMP004", "山田 健太", "pass", UserRole.TM, "TOKYO", "TEAM_A"));
    registerUser(buildUser(1005L, "EMP005", "新卒 一郎", "pass", UserRole.NG, "TOKYO", "TEAM_A"));
    registerUser(buildUser(1006L, "EMP006", "営業 次郎", "pass", UserRole.SP, "TOKYO", "HQ"));
    registerUser(buildUser(1007L, "EMP007", "支店長 三郎", "pass", UserRole.SM, "TOKYO", "HQ"));
    registerUser(buildUser(1008L, "EMP008", "管理者 四郎", "pass", UserRole.SA, "TOKYO", "HQ"));
    registerUser(buildUser(1009L, "EMP009", "髙橋 五郎", "pass", UserRole.GL, "TOKYO", "TEAM_A"));
    registerUser(buildUser(1010L, "EMP010", "伊藤 六郎", "pass", UserRole.TL, "OSAKA", "SALES_WEST"));

    // 検証用の初期グループ/チームを登録する。
    saveGroup(buildGroup("GROUP_A", "東京第1グループ", "TOKYO", 1009L));
    saveGroup(buildGroup("GROUP_OSAKA_1", "大阪第1グループ", "OSAKA", 1002L));
    saveTeam(buildTeam("TEAM_A", "Aチーム", "GROUP_A", "TOKYO", 1003L));
    saveTeam(buildTeam("SALES_WEST", "西営業チーム", "GROUP_OSAKA_1", "OSAKA", 1010L));

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
    seed.setAuthorRole(UserRole.TM);
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
   * GroupRecord オブジェクトをセッターで組み立てるファクトリメソッド。
   * インプット: 各フィールドの値（groupCode, groupName, officeCode, glUserId）。
   * アウトプット: 削除フラグ=false に設定した GroupRecord。
   *
   * @param groupCode  グループコード
   * @param groupName  グループ名
   * @param officeCode 拠点コード
   * @param glUserId   グループリーダーのユーザーID
   * @return 組み立て済みの GroupRecord
   */
  private GroupRecord buildGroup(String groupCode, String groupName, String officeCode, Long glUserId) {
    GroupRecord g = new GroupRecord();
    g.setGroupCode(groupCode);
    g.setGroupName(groupName);
    g.setOfficeCode(officeCode);
    g.setGlUserId(glUserId);
    g.setDeleted(false);
    return g;
  }

  /**
   * TeamRecord オブジェクトをセッターで組み立てるファクトリメソッド。
   * インプット: 各フィールドの値（teamCode, teamName, groupCode, officeCode, tlUserId）。
   * アウトプット: 削除フラグ=false に設定した TeamRecord。
   *
   * @param teamCode   チームコード
   * @param teamName   チーム名
   * @param groupCode  所属グループコード
   * @param officeCode 拠点コード
   * @param tlUserId   チームリーダーのユーザーID
   * @return 組み立て済みの TeamRecord
   */
  private TeamRecord buildTeam(String teamCode, String teamName, String groupCode, String officeCode, Long tlUserId) {
    TeamRecord t = new TeamRecord();
    t.setTeamCode(teamCode);
    t.setTeamName(teamName);
    t.setGroupCode(groupCode);
    t.setOfficeCode(officeCode);
    t.setTlUserId(tlUserId);
    t.setDeleted(false);
    return t;
  }

  /**
   * ユーザーを社員番号とユーザーIDで登録する。
   * インプット: user ユーザー情報。
   * アウトプット: 社員番号・ID の両インデックスに登録された状態。
   *
   * @param user ユーザー情報
   */
  public void registerUser(UserAccount user) {
    usersByEmployeeNo.put(user.getEmployeeNo(), user);
    usersById.put(user.getUserId(), user);
  }

  /**
   * 新しいユーザーIDを発番する。
   * インプット: なし。
   * アウトプット: 既存の最大ユーザーIDに1を加えた値。
   *
   * @return 新しいユーザーID
   */
  public synchronized Long newUserId() {
    return usersById.keySet().stream().mapToLong(Long::longValue).max().orElse(1000L) + 1;
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
   * 全グループを取得する。
   * インプット: なし。
   * アウトプット: グループコレクション。
   *
   * @return 全グループ
   */
  public Collection<GroupRecord> findAllGroups() {
    return groups.values();
  }

  /**
   * グループコードでグループを検索する。
   * インプット: groupCode グループコード。
   * アウトプット: グループの Optional。
   *
   * @param groupCode グループコード
   * @return グループ検索結果
   */
  public Optional<GroupRecord> findGroupByCode(String groupCode) {
    return Optional.ofNullable(groups.get(groupCode));
  }

  /**
   * グループを保存する。
   * インプット: record グループレコード。
   * アウトプット: グループストアへ保存された状態。
   *
   * @param record グループレコード
   */
  public void saveGroup(GroupRecord record) {
    groups.put(record.getGroupCode(), record);
  }

  /**
   * 全チームを取得する。
   * インプット: なし。
   * アウトプット: チームコレクション。
   *
   * @return 全チーム
   */
  public Collection<TeamRecord> findAllTeams() {
    return teams.values();
  }

  /**
   * チームコードでチームを検索する。
   * インプット: teamCode チームコード。
   * アウトプット: チームの Optional。
   *
   * @param teamCode チームコード
   * @return チーム検索結果
   */
  public Optional<TeamRecord> findTeamByCode(String teamCode) {
    return Optional.ofNullable(teams.get(teamCode));
  }

  /**
   * チームを保存する。
   * インプット: record チームレコード。
   * アウトプット: チームストアへ保存された状態。
   *
   * @param record チームレコード
   */
  public void saveTeam(TeamRecord record) {
    teams.put(record.getTeamCode(), record);
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
