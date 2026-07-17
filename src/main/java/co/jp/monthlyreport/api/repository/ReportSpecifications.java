package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.entity.ReportEntity;
import co.jp.monthlyreport.api.model.UserRole;
import org.springframework.data.jpa.domain.Specification;

/**
 * 月報のロール別可視範囲を JPA Specification として組み立てるユーティリティ。
 * インプット: 認証ユーザー・検索条件。
 * アウトプット: 検索クエリに適用する Specification。
 * ロール別の判定条件は {@code ReportService.canView} と同一のものを維持する。
 */
public final class ReportSpecifications {

  private ReportSpecifications() {
  }

  /**
   * 論理削除されていない月報のみに絞る。
   *
   * @return Specification
   */
  public static Specification<ReportEntity> notDeleted() {
    return (root, query, cb) -> cb.isFalse(root.get("deleteFlag"));
  }

  /**
   * ログインユーザーのロールに応じた可視範囲の Specification を返す。
   * NG は月報を閲覧できないため、常に偽となる条件を返す。
   *
   * @param user 認証ユーザー
   * @return Specification
   */
  public static Specification<ReportEntity> visibleTo(AuthUser user) {
    if (user.role() == UserRole.NG) {
      return (root, query, cb) -> cb.disjunction();
    }
    if (user.role() == UserRole.SA || user.role() == UserRole.OM) {
      return (root, query, cb) -> cb.conjunction();
    }
    if (user.role() == UserRole.SM) {
      return (root, query, cb) -> cb.equal(root.get("officeCode"), user.officeCode());
    }
    if (user.role() == UserRole.GL) {
      return (root, query, cb) -> cb.or(
          cb.equal(root.get("authorUserId"), user.userId()),
          cb.equal(root.get("officeCode"), user.officeCode()));
    }
    if (user.role() == UserRole.TL) {
      return (root, query, cb) -> cb.or(
          cb.equal(root.get("authorUserId"), user.userId()),
          cb.equal(root.get("teamCode"), user.teamCode()));
    }
    // TM / SP: 本人投稿のみ参照可能。
    return (root, query, cb) -> cb.equal(root.get("authorUserId"), user.userId());
  }

  /**
   * 対象月で絞り込む（未指定時は絞り込みなし）。
   *
   * @param month 対象月（yyyy-MM）
   * @return Specification
   */
  public static Specification<ReportEntity> month(String month) {
    if (month == null || month.isBlank()) {
      return (root, query, cb) -> cb.conjunction();
    }
    return (root, query, cb) -> cb.equal(root.get("reportMonth"), month);
  }

  /**
   * フィードバック状態で絞り込む。
   * "ALL" / "SUBMITTED" は絞り込みなし（既存の InMemoryDataStore 実装の挙動を踏襲）。
   *
   * @param status ステータス（ALL / SUBMITTED / PENDING_FEEDBACK / FEEDBACKED）
   * @return Specification
   */
  public static Specification<ReportEntity> status(String status) {
    return switch (status) {
      case "ALL", "SUBMITTED" -> (root, query, cb) -> cb.conjunction();
      case "PENDING_FEEDBACK" -> (root, query, cb) -> cb.notEqual(root.get("status"), "FEEDBACKED");
      case "FEEDBACKED" -> (root, query, cb) -> cb.equal(root.get("status"), "FEEDBACKED");
      default -> throw new IllegalArgumentException("invalid status: " + status);
    };
  }
}
