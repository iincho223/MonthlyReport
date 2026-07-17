package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.ReportFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** 月報回答の Spring Data JPA リポジトリ。 */
public interface ReportFeedbackRepository extends JpaRepository<ReportFeedbackEntity, String> {
}
