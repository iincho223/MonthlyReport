package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** リフレッシュトークン（DB設計.md 5.11 refresh_tokens）。 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends BaseEntity {

  /** トークンID（ULID） */
  @Id
  @Column(name = "token_id", columnDefinition = "char(26)")
  private String tokenId;

  /** ユーザーID */
  @Column(name = "user_id", nullable = false)
  private Long userId;

  /** トークンハッシュ（生値は保存しない） */
  @Column(name = "token_hash", nullable = false, length = 255)
  private String tokenHash;

  /** 有効期限 */
  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  /** 失効日時 */
  @Column(name = "revoked_at")
  private OffsetDateTime revokedAt;
}
