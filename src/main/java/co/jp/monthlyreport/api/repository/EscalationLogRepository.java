package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.EscalationLogEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** エスカレーション対応ログの Spring Data JPA リポジトリ。 */
public interface EscalationLogRepository extends JpaRepository<EscalationLogEntity, String> {

  List<EscalationLogEntity> findByEscalationIdOrderByCreatedAtAsc(String escalationId);
}
