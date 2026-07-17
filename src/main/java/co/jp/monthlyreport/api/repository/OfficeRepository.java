package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.OfficeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** 営業所マスタの Spring Data JPA リポジトリ。 */
public interface OfficeRepository extends JpaRepository<OfficeEntity, String> {
}
