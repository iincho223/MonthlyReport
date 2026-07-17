package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.EscalationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** エスカレーションの Spring Data JPA リポジトリ。 */
public interface EscalationRepository extends JpaRepository<EscalationEntity, String>, JpaSpecificationExecutor<EscalationEntity> {

  Optional<EscalationEntity> findByEscalationIdAndDeleteFlagFalse(String escalationId);
}
