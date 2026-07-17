package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.ReportConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** 月報コンディションの Spring Data JPA リポジトリ。 */
public interface ReportConditionRepository extends JpaRepository<ReportConditionEntity, String> {
}
