package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.TeamEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** チームマスタの Spring Data JPA リポジトリ。 */
public interface TeamRepository extends JpaRepository<TeamEntity, String> {

  List<TeamEntity> findByDeleteFlagFalse();

  Optional<TeamEntity> findByTeamCodeAndDeleteFlagFalse(String teamCode);

  List<TeamEntity> findByGroupCodeInAndDeleteFlagFalse(Collection<String> groupCodes);

  boolean existsByTeamCodeAndDeleteFlagFalse(String teamCode);
}
