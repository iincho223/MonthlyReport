package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.entity.ReportEntity;
import co.jp.monthlyreport.api.entity.UserEntity;
import co.jp.monthlyreport.api.model.UserRole;
import co.jp.monthlyreport.api.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
/**
 * ダッシュボード用集計サービス。
 * インプット: 認証ユーザーと対象月。
 * アウトプット: 月報件数・未回答件数・提出率・未提出メンバー一覧。
 */
public class DashboardService {
  private static final String STATUS_FEEDBACKED = "FEEDBACKED";

  private final ReportService reportService;
  private final UserRepository userRepository;

  public DashboardService(ReportService reportService, UserRepository userRepository) {
    this.reportService = reportService;
    this.userRepository = userRepository;
  }

  /**
   * ダッシュボード集計を返す。
   * インプット: user 認証ユーザー、month 対象月。
   * アウトプット: 集計結果マップ。TL 以上には提出率・未提出者一覧を追加する。
   *
   * @param user  認証ユーザー
   * @param month 対象月
   * @return 集計結果
   */
  public Map<String, Object> summary(AuthUser user, String month) {
    // ユーザーの参照スコープに合わせて月報を絞り込む。
    List<ReportEntity> scoped = reportService.scopedReports(user).stream()
        .filter(r -> month == null || month.isBlank() || month.equals(r.getReportMonth()))
        .toList();

    long pending = scoped.stream().filter(r -> !STATUS_FEEDBACKED.equals(r.getStatus())).count();
    long submitted = scoped.size();

    Map<String, Object> result = new HashMap<>();
    result.put(ResponseKeys.TOTAL_REPORTS, (int) submitted);
    result.put(ResponseKeys.PENDING_FEEDBACK_COUNT, (int) pending);
    result.put(ResponseKeys.SUBMITTED_COUNT, (int) submitted);

    // TL 以上のみ提出率・未提出メンバー一覧を返却する。
    if (user.role() != UserRole.NG && user.role() != UserRole.TM) {
      result.put(ResponseKeys.SUBMISSION_RATE, calcSubmissionRate(user, scoped));
      result.put(ResponseKeys.UNSUBMITTED_MEMBERS, calcUnsubmittedMembers(user, scoped));
    } else {
      result.put(ResponseKeys.SUBMISSION_RATE, 0);
      result.put(ResponseKeys.UNSUBMITTED_MEMBERS, List.of());
    }
    return result;
  }

  /**
   * スコープ内メンバー全体の提出率(%)を算出する。
   *
   * @param user      認証ユーザー
   * @param submitted 提出済み月報一覧
   * @return 提出率（0-100）
   */
  private int calcSubmissionRate(AuthUser user, List<ReportEntity> submitted) {
    List<UserEntity> members = scopedMembers(user);
    if (members.isEmpty()) return 0;
    Set<Long> submittedUserIds = submitted.stream()
        .map(ReportEntity::getAuthorUserId)
        .collect(Collectors.toSet());
    long submittedCount = members.stream().filter(m -> submittedUserIds.contains(m.getUserId())).count();
    return (int) Math.round(submittedCount * 100.0 / members.size());
  }

  /**
   * スコープ内メンバーのうち未提出者一覧を返す。
   *
   * @param user      認証ユーザー
   * @param submitted 提出済み月報一覧
   * @return 未提出者リスト（employeeNo, name）
   */
  private List<Map<String, Object>> calcUnsubmittedMembers(AuthUser user, List<ReportEntity> submitted) {
    Set<Long> submittedIds = submitted.stream()
        .map(ReportEntity::getAuthorUserId)
        .collect(Collectors.toSet());
    return scopedMembers(user).stream()
        .filter(m -> !submittedIds.contains(m.getUserId()))
        .map(m -> {
          Map<String, Object> entry = new HashMap<>();
          entry.put(ResponseKeys.EMPLOYEE_NO, m.getEmployeeNo());
          entry.put(ResponseKeys.NAME, m.getUserName());
          return entry;
        }).toList();
  }

  /**
   * ロール別スコープのメンバー一覧を返す。
   *
   * @param user 認証ユーザー
   * @return スコープ内メンバー
   */
  private List<UserEntity> scopedMembers(AuthUser user) {
    return userRepository.findByDeleteFlagFalse().stream()
        .filter(UserEntity::isActive)
        .filter(u -> switch (user.role()) {
          case OM -> true;
          case GL -> user.officeCode().equals(u.getOfficeCode());
          case TL -> user.teamCode().equals(u.getTeamCode());
          default -> false;
        })
        .toList();
  }
}
