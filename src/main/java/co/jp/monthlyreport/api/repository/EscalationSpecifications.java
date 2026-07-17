package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.entity.EscalationEntity;
import co.jp.monthlyreport.api.model.UserRole;
import org.springframework.data.jpa.domain.Specification;

/**
 * エスカレーションのロール別可視範囲を JPA Specification として組み立てるユーティリティ。
 * ロール別の判定条件は {@code EscalationService.canView} と同一のものを維持する。
 */
public final class EscalationSpecifications {

  private EscalationSpecifications() {
  }

  /**
   * ログインユーザーのロールに応じた可視範囲の Specification を返す。
   * NG / TM はエスカレーション自体にアクセス不可（呼び出し側で事前に弾く）。
   *
   * @param user 認証ユーザー
   * @return Specification
   */
  public static Specification<EscalationEntity> visibleTo(AuthUser user) {
    if (user.role() == UserRole.SA || user.role() == UserRole.OM) {
      return (root, query, cb) -> cb.conjunction();
    }
    if (user.role() == UserRole.SM || user.role() == UserRole.GL) {
      return (root, query, cb) -> cb.equal(root.get("officeCode"), user.officeCode());
    }
    if (user.role() == UserRole.TL) {
      return (root, query, cb) -> cb.or(
          cb.equal(root.get("teamCode"), user.teamCode()),
          cb.equal(root.get("createdBy"), user.userId()));
    }
    if (user.role() == UserRole.SP) {
      return (root, query, cb) -> cb.equal(root.get("assigneeUserId"), user.userId());
    }
    return (root, query, cb) -> cb.disjunction();
  }

  /**
   * ステータスで絞り込む（"ALL" は絞り込みなし）。
   *
   * @param status ステータス
   * @return Specification
   */
  public static Specification<EscalationEntity> status(String status) {
    if ("ALL".equals(status)) {
      return (root, query, cb) -> cb.conjunction();
    }
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }
}
