package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** ユーザーマスタの Spring Data JPA リポジトリ。 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findByEmployeeNoAndDeleteFlagFalse(String employeeNo);

  Optional<UserEntity> findByUserIdAndDeleteFlagFalse(Long userId);

  List<UserEntity> findByDeleteFlagFalse();

  boolean existsByEmployeeNoAndDeleteFlagFalse(String employeeNo);
}
