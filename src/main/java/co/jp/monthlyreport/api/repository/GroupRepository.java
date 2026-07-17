package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.GroupEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** グループマスタの Spring Data JPA リポジトリ。 */
public interface GroupRepository extends JpaRepository<GroupEntity, String> {

  List<GroupEntity> findByDeleteFlagFalse();

  Optional<GroupEntity> findByGroupCodeAndDeleteFlagFalse(String groupCode);

  List<GroupEntity> findByGlUserIdAndDeleteFlagFalse(Long glUserId);

  boolean existsByGroupCodeAndDeleteFlagFalse(String groupCode);
}
