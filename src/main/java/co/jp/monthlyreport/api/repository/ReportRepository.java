package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.ReportEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** 月報ヘッダの Spring Data JPA リポジトリ。 */
public interface ReportRepository extends JpaRepository<ReportEntity, String>, JpaSpecificationExecutor<ReportEntity> {

  Optional<ReportEntity> findByReportIdAndDeleteFlagFalse(String reportId);

  boolean existsByAuthorUserIdAndReportMonthAndDeleteFlagFalse(Long authorUserId, String reportMonth);
}
