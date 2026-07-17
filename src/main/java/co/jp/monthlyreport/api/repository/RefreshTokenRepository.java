package co.jp.monthlyreport.api.repository;

import co.jp.monthlyreport.api.entity.RefreshTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** リフレッシュトークンの Spring Data JPA リポジトリ。 */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

  Optional<RefreshTokenEntity> findByTokenHashAndRevokedAtIsNull(String tokenHash);
}
