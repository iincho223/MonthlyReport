package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.common.MessageKeys;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.dto.request.LoginRequest;
import co.jp.monthlyreport.api.model.UserAccount;
import co.jp.monthlyreport.api.repository.InMemoryDataStore;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
/**
 * 認証処理とトークン管理を行うサービス。
 * インプット: ログイン情報、認可ヘッダ、リフレッシュトークン。
 * アウトプット: 認証済みユーザー情報とトークン発行結果。
 */
public class AuthService {
  private final InMemoryDataStore dataStore;
  private final MessageSource messageSource;
  private final Map<String, TokenSession> accessTokens = new ConcurrentHashMap<>();
  private final Map<String, RefreshSession> refreshTokens = new ConcurrentHashMap<>();

  public AuthService(InMemoryDataStore dataStore, MessageSource messageSource) {
    this.dataStore = dataStore;
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
    UserAccount account = dataStore.findUserByEmployeeNo(request.employeeNo())
        .filter(u -> u.isActive() && !u.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_001, msg(MessageKeys.AUTH_LOGIN_FAILED)));

    // パスワード不一致時は認証失敗とする。
    if (!account.getPassword().equals(request.password())) {
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
    RefreshSession session = refreshTokens.get(refreshToken);
    // セッションが存在しない場合は無効トークン扱い。
    if (session == null) {
      throw new BusinessException(ErrorCodes.AUTH_002, msg(MessageKeys.AUTH_REFRESH_TOKEN_INVALID));
    }
    // 失効または無効化済みトークンは再発行不可。
    if (session.revoked || session.expiresAt.isBefore(Instant.now())) {
      throw new BusinessException(ErrorCodes.AUTH_003, msg(MessageKeys.AUTH_REFRESH_TOKEN_EXPIRED));
    }
    // 紐づくユーザー情報を取得して再発行処理へ進む。
    UserAccount account = dataStore.findUserById(session.userId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.AUTH_001, msg(MessageKeys.AUTH_USER_NOT_FOUND)));
    session.revoked = true;
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
    // 同一ユーザーのリフレッシュトークンを全て無効化する。
    refreshTokens.values().forEach(rt -> {
      if (rt.userId.equals(user.userId())) {
        rt.revoked = true;
      }
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
    UserAccount account = dataStore.findUserById(session.userId)
        .filter(u -> u.isActive() && !u.isDeleted())
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
  private Map<String, Object> issueTokens(UserAccount account) {
    // 一意なトークン文字列を生成する。
    String accessToken = UUID.randomUUID().toString();
    String refreshToken = UUID.randomUUID().toString();
    long expiresIn = 3600L;

    // アクセス/リフレッシュの有効期限付きセッションを保存する。
    accessTokens.put(accessToken, new TokenSession(account.getUserId(), Instant.now().plusSeconds(expiresIn)));
    refreshTokens.put(refreshToken, new RefreshSession(account.getUserId(), Instant.now().plusSeconds(60L * 60L * 24L * 7L), false));

    Map<String, Object> profile = Map.of(
        ResponseKeys.USER_ID, account.getUserId(),
        ResponseKeys.EMPLOYEE_NO, account.getEmployeeNo(),
        ResponseKeys.NAME, account.getName(),
        ResponseKeys.ROLE, account.getRole().name(),
        ResponseKeys.OFFICE_CODE, account.getOfficeCode(),
        ResponseKeys.TEAM_CODE, account.getTeamCode());

    Map<String, Object> params = new HashMap<>();
    params.put(ResponseKeys.ACCESS_TOKEN, accessToken);
    params.put(ResponseKeys.REFRESH_TOKEN, refreshToken);
    params.put(ResponseKeys.EXPIRES_IN, expiresIn);
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
   * UserAccount を AuthUser へ変換する。
   * インプット: account ユーザーアカウント。
   * アウトプット: 認証ユーザー情報。
   *
   * @param account ユーザーアカウント
   * @return 認証ユーザー情報
   */
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
