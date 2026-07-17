package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** ロールマスタの Spring Data JPA リポジトリ。 */
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
}
