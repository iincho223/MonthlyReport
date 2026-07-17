package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.common.MessageKeys;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.common.UlidGenerator;
import co.jp.monthlyreport.api.dto.request.LoginRequest;
import co.jp.monthlyreport.api.entity.RefreshTokenEntity;
import co.jp.monthlyreport.api.entity.UserEntity;
import co.jp.monthlyreport.api.model.UserRole;
import co.jp.monthlyreport.api.repository.RefreshTokenRepository;
import co.jp.monthlyreport.api.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
/**
 * 認証処理とトークン管理を行うサービス。
 * インプット: ログイン情報、認可ヘッダ、リフレッシュトークン。
 * アウトプット: 認証済みユーザー情報とトークン発行結果。
 *
 * <p>アクセストークンは単一インスタンス運用を前提としたサーバーメモリ管理とする
 * （DB設計.md に対応テーブルを設けていないため）。リフレッシュトークンのみ
 * {@code refresh_tokens} テーブルへハッシュ化して永続化する。</p>
 */
public class AuthService {
  private static final long ACCESS_TOKEN_TTL_SECONDS = 3600L;
  private static final long REFRESH_TOKEN_TTL_SECONDS = 60L * 60L * 24L * 7L;

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final MessageSource messageSource;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final Map<String, TokenSession> accessTokens = new ConcurrentHashMap<>();

  public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, MessageSource messageSource) {
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.messageSource = messageSource;
  }

  /**
   * ログインを実行しトークンを発行する。
   * インプット: request 社員番号とパスワード。
   * アウトプット: アクセストークン、リフレッシュトークン、ユーザー情報。
   *
   * @param request ログインリクエスト
   * @return ログイン結果
   */
  public Map<String, Object> login(LoginRequest request) {
    // 社員番号に紐づく有効ユーザーを検索する。
    UserEntity account = userRepository.findByEmployeeNoAndDeleteFlagFalse(request.employeeNo())
        .filter(UserEntity::isActive)
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_001, msg(MessageKeys.AUTH_LOGIN_FAILED)));

    // パスワード不一致時は認証失敗とする。
    if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
      throw new BusinessException(ErrorCodes.AUTH_001, msg(MessageKeys.AUTH_LOGIN_FAILED));
    }

    // 認証成功時にトークンを発行する。
    return issueTokens(account);
  }

  /**
   * リフレッシュトークンでトークンを再発行する。
   * インプット: refreshToken リフレッシュトークン。
   * アウトプット: 新しいアクセストークンとリフレッシュトークン。
   *
   * @param refreshToken リフレッシュトークン
   * @return 再発行結果
   */
  public Map<String, Object> refresh(String refreshToken) {
    RefreshTokenEntity session = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hashToken(refreshToken))
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_002, msg(MessageKeys.AUTH_REFRESH_TOKEN_INVALID)));
    // 失効済みまたは期限切れトークンは再発行不可。
    if (session.getExpiresAt().isBefore(OffsetDateTime.now())) {
      throw new BusinessException(ErrorCodes.AUTH_003, msg(MessageKeys.AUTH_REFRESH_TOKEN_EXPIRED));
    }
    // 紐づくユーザー情報を取得して再発行処理へ進む。
    UserEntity account = userRepository.findByUserIdAndDeleteFlagFalse(session.getUserId())
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_001, msg(MessageKeys.AUTH_USER_NOT_FOUND)));
    session.setRevokedAt(OffsetDateTime.now());
    refreshTokenRepository.save(session);
    return issueTokens(account);
  }

  /**
   * 認証ユーザーのトークンを無効化する。
   * インプット: authorizationHeader 認可ヘッダ。
   * アウトプット: ログアウト成功フラグ。
   *
   * @param authorizationHeader 認可ヘッダ
   * @return ログアウト結果
   */
  public Map<String, Object> logout(String authorizationHeader) {
    // 認可ヘッダからログインユーザーを解決する。
    AuthUser user = resolveAuthUser(authorizationHeader);
    // 同一ユーザーの未失効リフレッシュトークンを全て無効化する。
    refreshTokenRepository.findAll().stream()
        .filter(rt -> rt.getUserId().equals(user.userId()) && rt.getRevokedAt() == null)
        .forEach(rt -> {
          rt.setRevokedAt(OffsetDateTime.now());
          refreshTokenRepository.save(rt);
        });
    // 同一ユーザーのアクセストークンを削除する。
    accessTokens.entrySet().removeIf(e -> e.getValue().userId.equals(user.userId()));
    return Map.of(ResponseKeys.SUCCESS, true);
  }

  /**
   * 認可ヘッダから認証ユーザー情報を解決する。
   * インプット: authorizationHeader 認可ヘッダ。
   * アウトプット: 認証済みユーザー情報。
   *
   * @param authorizationHeader 認可ヘッダ
   * @return 認証済みユーザー
   */
  public AuthUser resolveAuthUser(String authorizationHeader) {
    // Bearer トークンを抽出する。
    String token = parseBearerToken(authorizationHeader);
    TokenSession session = accessTokens.get(token);
    // セッション未存在または期限切れなら未認証とする。
    if (session == null || session.expiresAt.isBefore(Instant.now())) {
      throw new BusinessException(ErrorCodes.AUTH_001, msg(MessageKeys.AUTH_UNAUTHORIZED));
    }
    // 有効なユーザー情報のみ認証ユーザーとして返す。
    UserEntity account = userRepository.findByUserIdAndDeleteFlagFalse(session.userId)
        .filter(UserEntity::isActive)
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_001, msg(MessageKeys.AUTH_USER_DISABLED)));
    return toAuthUser(account);
  }

  /**
   * メッセージキーから日本語メッセージを取得する。
   * インプット: key メッセージキー。
   * アウトプット: messages.properties から取得したメッセージ文字列。
   *
   * @param key メッセージキー
   * @return メッセージ文字列
   */
  private String msg(String key) {
    return messageSource.getMessage(key, null, Locale.JAPANESE);
  }

  /**
   * アクセストークンとリフレッシュトークンを発行する。
   * インプット: account 認証済みユーザー。
   * アウトプット: トークン情報とプロフィール情報。
   *
   * @param account 認証済みユーザー
   * @return トークン発行結果
   */
  private Map<String, Object> issueTokens(UserEntity account) {
    // 一意なトークン文字列を生成する。
    String accessToken = UUID.randomUUID().toString();
    String refreshToken = UUID.randomUUID().toString();

    // アクセストークンはサーバーメモリ上に有効期限付きで保持する。
    accessTokens.put(accessToken, new TokenSession(account.getUserId(), Instant.now().plusSeconds(ACCESS_TOKEN_TTL_SECONDS)));

    // リフレッシュトークンはハッシュ化して DB へ永続化する。
    RefreshTokenEntity refreshEntity = new RefreshTokenEntity();
    refreshEntity.setTokenId(UlidGenerator.generate());
    refreshEntity.setUserId(account.getUserId());
    refreshEntity.setTokenHash(hashToken(refreshToken));
    refreshEntity.setExpiresAt(OffsetDateTime.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS));
    refreshEntity.setDeleteFlag(false);
    refreshEntity.setUpdatedAt(OffsetDateTime.now());
    refreshEntity.setUpdatedBy(account.getEmployeeNo());
    refreshEntity.setRegisteredAt(OffsetDateTime.now());
    refreshEntity.setRegisteredBy(account.getEmployeeNo());
    refreshTokenRepository.save(refreshEntity);

    Map<String, Object> profile = Map.of(
        ResponseKeys.USER_ID, account.getUserId(),
        ResponseKeys.EMPLOYEE_NO, account.getEmployeeNo(),
        ResponseKeys.NAME, account.getUserName(),
        ResponseKeys.ROLE, account.getRoleCode(),
        ResponseKeys.OFFICE_CODE, account.getOfficeCode(),
        ResponseKeys.TEAM_CODE, account.getTeamCode());

    Map<String, Object> params = new HashMap<>();
    params.put(ResponseKeys.ACCESS_TOKEN, accessToken);
    params.put(ResponseKeys.REFRESH_TOKEN, refreshToken);
    params.put(ResponseKeys.EXPIRES_IN, ACCESS_TOKEN_TTL_SECONDS);
    params.put(ResponseKeys.USER_PROFILE, profile);
    return params;
  }

  /**
   * 認可ヘッダから Bearer トークン文字列を取り出す。
   * インプット: authorizationHeader 認可ヘッダ。
   * アウトプット: Bearer 部分を除いたトークン。
   *
   * @param authorizationHeader 認可ヘッダ
   * @return Bearer トークン
   */
  private String parseBearerToken(String authorizationHeader) {
    // Bearer 形式でないヘッダは不正とする。
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new BusinessException(ErrorCodes.AUTH_001, msg(MessageKeys.AUTH_INVALID_HEADER));
    }
    return authorizationHeader.substring("Bearer ".length()).trim();
  }

  /**
   * UserEntity を AuthUser へ変換する。
   * インプット: account ユーザーエンティティ。
   * アウトプット: 認証ユーザー情報。
   *
   * @param account ユーザーエンティティ
   * @return 認証ユーザー情報
   */
  private AuthUser toAuthUser(UserEntity account) {
    return new AuthUser(account.getUserId(), account.getEmployeeNo(), account.getUserName(),
        UserRole.valueOf(account.getRoleCode()), account.getOfficeCode(), account.getTeamCode());
  }

  /**
   * リフレッシュトークンの生値を SHA-256 でハッシュ化する（DB には生値を保存しない）。
   *
   * @param token トークン生値
   * @return ハッシュ値（16進文字列）
   */
  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }

  private static final class TokenSession {
    private final Long userId;
    private final Instant expiresAt;

    private TokenSession(Long userId, Instant expiresAt) {
      this.userId = userId;
      this.expiresAt = expiresAt;
    }
  }
}
