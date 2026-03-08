package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.dto.request.LoginRequest;
import co.jp.monthlyreport.api.model.UserAccount;
import co.jp.monthlyreport.api.repository.InMemoryDataStore;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final InMemoryDataStore dataStore;
  private final Map<String, TokenSession> accessTokens = new ConcurrentHashMap<>();
  private final Map<String, RefreshSession> refreshTokens = new ConcurrentHashMap<>();

  public AuthService(InMemoryDataStore dataStore) {
    this.dataStore = dataStore;
  }

  public Map<String, Object> login(LoginRequest request) {
    UserAccount account = dataStore.findUserByEmployeeNo(request.employeeNo())
        .filter(u -> u.isActive() && !u.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_001, "認証に失敗しました"));

    if (!account.getPassword().equals(request.password())) {
      throw new BusinessException(ErrorCodes.AUTH_001, "認証に失敗しました");
    }

    return issueTokens(account);
  }

  public Map<String, Object> refresh(String refreshToken) {
    RefreshSession session = refreshTokens.get(refreshToken);
    if (session == null) {
      throw new BusinessException(ErrorCodes.AUTH_002, "リフレッシュトークンが無効です");
    }
    if (session.revoked || session.expiresAt.isBefore(Instant.now())) {
      throw new BusinessException(ErrorCodes.AUTH_003, "リフレッシュトークンが失効しています");
    }
    UserAccount account = dataStore.findUserById(session.userId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_001, "認証情報が見つかりません"));
    session.revoked = true;
    return issueTokens(account);
  }

  public Map<String, Object> logout(String authorizationHeader) {
    AuthUser user = resolveAuthUser(authorizationHeader);
    refreshTokens.values().forEach(rt -> {
      if (rt.userId.equals(user.userId())) {
        rt.revoked = true;
      }
    });
    accessTokens.entrySet().removeIf(e -> e.getValue().userId.equals(user.userId()));
    return Map.of("success", true);
  }

  public AuthUser resolveAuthUser(String authorizationHeader) {
    String token = parseBearerToken(authorizationHeader);
    TokenSession session = accessTokens.get(token);
    if (session == null || session.expiresAt.isBefore(Instant.now())) {
      throw new BusinessException(ErrorCodes.AUTH_001, "未認証です");
    }
    UserAccount account = dataStore.findUserById(session.userId)
        .filter(u -> u.isActive() && !u.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_001, "ユーザーが無効です"));
    return toAuthUser(account);
  }

  private Map<String, Object> issueTokens(UserAccount account) {
    String accessToken = UUID.randomUUID().toString();
    String refreshToken = UUID.randomUUID().toString();
    long expiresIn = 3600L;

    accessTokens.put(accessToken, new TokenSession(account.getUserId(), Instant.now().plusSeconds(expiresIn)));
    refreshTokens.put(refreshToken, new RefreshSession(account.getUserId(), Instant.now().plusSeconds(60L * 60L * 24L * 7L), false));

    Map<String, Object> profile = Map.of(
        "userId", account.getUserId(),
        "employeeNo", account.getEmployeeNo(),
        "name", account.getName(),
        "role", account.getRole().name(),
        "officeCode", account.getOfficeCode(),
        "teamCode", account.getTeamCode());

    Map<String, Object> params = new HashMap<>();
    params.put("accessToken", accessToken);
    params.put("refreshToken", refreshToken);
    params.put("expiresIn", expiresIn);
    params.put("userProfile", profile);
    return params;
  }

  private String parseBearerToken(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new BusinessException(ErrorCodes.AUTH_001, "Authorization ヘッダが不正です");
    }
    return authorizationHeader.substring("Bearer ".length()).trim();
  }

  private AuthUser toAuthUser(UserAccount account) {
    return new AuthUser(account.getUserId(), account.getEmployeeNo(), account.getName(), account.getRole(), account.getOfficeCode(), account.getTeamCode());
  }

  private static final class TokenSession {
    private final Long userId;
    private final Instant expiresAt;

    private TokenSession(Long userId, Instant expiresAt) {
      this.userId = userId;
      this.expiresAt = expiresAt;
    }
  }

  private static final class RefreshSession {
    private final Long userId;
    private final Instant expiresAt;
    private boolean revoked;

    private RefreshSession(Long userId, Instant expiresAt, boolean revoked) {
      this.userId = userId;
      this.expiresAt = expiresAt;
      this.revoked = revoked;
    }
  }
}
