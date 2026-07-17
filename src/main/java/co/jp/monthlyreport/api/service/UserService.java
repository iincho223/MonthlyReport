package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.common.MessageKeys;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.dto.request.UserCreateRequest;
import co.jp.monthlyreport.api.dto.request.UserSearchRequest;
import co.jp.monthlyreport.api.entity.GroupEntity;
import co.jp.monthlyreport.api.entity.TeamEntity;
import co.jp.monthlyreport.api.entity.UserEntity;
import co.jp.monthlyreport.api.model.UserRole;
import co.jp.monthlyreport.api.repository.GroupRepository;
import co.jp.monthlyreport.api.repository.TeamRepository;
import co.jp.monthlyreport.api.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
/**
 * ユーザー情報の取得・検索・登録・削除を扱うサービス。
 * インプット: 認証ユーザー情報と各 API リクエスト。
 * アウトプット: API 応答用のユーザー情報マップ。
 */
public class UserService {

  private static final Set<UserRole> ENGINEER_ROLES = Set.of(UserRole.NG, UserRole.TM);
  private static final Set<UserRole> SP_SM_CREATABLE_ROLES = Set.of(UserRole.NG, UserRole.TM, UserRole.SP, UserRole.SM);

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final TeamRepository teamRepository;
  private final MessageSource messageSource;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public UserService(UserRepository userRepository, GroupRepository groupRepository,
      TeamRepository teamRepository, MessageSource messageSource) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.teamRepository = teamRepository;
    this.messageSource = messageSource;
  }

  /**
   * ログインユーザーの自分情報を返す。
   * インプット: user 認証ユーザー。
   * アウトプット: user 情報を整形したマップ。
   *
   * @param user 認証ユーザー
   * @return 自分情報
   */
  public Map<String, Object> me(AuthUser user) {
    // 画面表示に必要な項目のみ返却する。
    return Map.of(
        ResponseKeys.USER_ID, user.userId(),
        ResponseKeys.EMPLOYEE_NO, user.employeeNo(),
        ResponseKeys.NAME, user.name(),
        ResponseKeys.ROLE, user.role().name(),
        ResponseKeys.OFFICE_CODE, user.officeCode(),
        ResponseKeys.TEAM_CODE, user.teamCode());
  }

  /**
   * ロール別スコープでユーザー一覧を検索する。
   * インプット: user 認証ユーザー、request 検索条件。
   * アウトプット: ユーザー一覧とページング情報。
   *
   * @param user    認証ユーザー
   * @param request 検索条件
   * @return 検索結果
   */
  public Map<String, Object> search(AuthUser user, UserSearchRequest request) {
    requireManagementAccess(user, MessageKeys.USER_NO_SEARCH_PERMISSION);

    int page = request.page() == null ? 1 : request.page();
    int size = request.size() == null ? 20 : request.size();

    List<UserEntity> filtered = scopedUsers(user).stream()
        .filter(u -> request.role() == null || request.role().isBlank() || request.role().equalsIgnoreCase(u.getRoleCode()))
        .filter(u -> request.officeCode() == null || request.officeCode().isBlank() || request.officeCode().equals(u.getOfficeCode()))
        .sorted((a, b) -> a.getEmployeeNo().compareTo(b.getEmployeeNo()))
        .collect(Collectors.toList());

    int from = Math.min((page - 1) * size, filtered.size());
    int to = Math.min(from + size, filtered.size());

    List<Map<String, Object>> items = filtered.subList(from, to).stream().map(u -> {
      Map<String, Object> item = new HashMap<>();
      item.put(ResponseKeys.USER_ID, u.getUserId());
      item.put(ResponseKeys.EMPLOYEE_NO, u.getEmployeeNo());
      item.put(ResponseKeys.NAME, u.getUserName());
      item.put(ResponseKeys.ROLE, u.getRoleCode());
      item.put(ResponseKeys.OFFICE_CODE, u.getOfficeCode());
      item.put(ResponseKeys.TEAM_CODE, u.getTeamCode());
      return item;
    }).toList();

    return Map.of(
        ResponseKeys.ITEMS, items,
        ResponseKeys.PAGING, Map.of(
            ResponseKeys.PAGE, page,
            ResponseKeys.SIZE, size,
            ResponseKeys.TOTAL_ELEMENTS, filtered.size(),
            ResponseKeys.TOTAL_PAGES, filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size)));
  }

  /**
   * 新規ユーザーを登録する。
   * インプット: user 認証ユーザー、request 登録内容。
   * アウトプット: 登録結果。
   *
   * @param user    認証ユーザー
   * @param request 登録リクエスト
   * @return 登録結果
   */
  public Map<String, Object> create(AuthUser user, UserCreateRequest request) {
    requireManagementAccess(user, MessageKeys.USER_NO_CREATE_PERMISSION);

    UserRole role = parseRole(request.role());
    // チームコードはエンジニア（NG/TM）登録時のみ必須。
    if (ENGINEER_ROLES.contains(role) && (request.teamCode() == null || request.teamCode().isBlank())) {
      throw new BusinessException(ErrorCodes.VAL_001, msg(MessageKeys.USER_NO_CREATE_SCOPE));
    }
    if (!canCreateInScope(user, role, request.officeCode(), request.teamCode())) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.USER_NO_CREATE_SCOPE));
    }
    if (userRepository.existsByEmployeeNoAndDeleteFlagFalse(request.employeeNo())) {
      throw new BusinessException(ErrorCodes.USER_409, msg(MessageKeys.USER_DUPLICATE_EMPLOYEE_NO));
    }

    UserEntity account = new UserEntity();
    account.setEmployeeNo(request.employeeNo());
    account.setUserName(request.name());
    account.setPasswordHash(passwordEncoder.encode(request.password()));
    account.setRoleCode(role.name());
    account.setOfficeCode(request.officeCode());
    account.setTeamCode(request.teamCode());
    account.setActive(true);
    account.setDeleteFlag(false);
    account.setUpdatedAt(OffsetDateTime.now());
    account.setUpdatedBy(user.employeeNo());
    account.setRegisteredAt(OffsetDateTime.now());
    account.setRegisteredBy(user.employeeNo());
    userRepository.save(account);

    return Map.of(ResponseKeys.USER_ID, account.getUserId(), ResponseKeys.EMPLOYEE_NO, account.getEmployeeNo());
  }

  /**
   * ユーザーを削除する。
   * インプット: user 認証ユーザー、userId 削除対象ユーザーID。
   * アウトプット: 削除結果。
   *
   * @param user   認証ユーザー
   * @param userId 削除対象ユーザーID
   * @return 削除結果
   */
  public Map<String, Object> delete(AuthUser user, Long userId) {
    requireManagementAccess(user, MessageKeys.USER_NO_DELETE_PERMISSION);

    UserEntity target = userRepository.findByUserIdAndDeleteFlagFalse(userId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.USER_404, msg(MessageKeys.USER_NOT_FOUND)));

    // 自分自身のアカウントは削除不可。
    if (target.getUserId().equals(user.userId())) {
      throw new BusinessException(ErrorCodes.USER_400, msg(MessageKeys.USER_SELF_DELETE));
    }
    if (!canDeleteInScope(user, target)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.USER_NO_DELETE_SCOPE));
    }

    target.setDeleteFlag(true);
    target.setUpdatedAt(OffsetDateTime.now());
    target.setUpdatedBy(user.employeeNo());
    userRepository.save(target);
    return Map.of(ResponseKeys.USER_ID, target.getUserId(), ResponseKeys.DELETED, true);
  }

  /**
   * ユーザー管理 API へのアクセス権限を確認する（GL/OM/SP/SM/SA のみ）。
   *
   * @param user             認証ユーザー
   * @param permissionErrorKey 権限エラー時のメッセージキー
   */
  private void requireManagementAccess(AuthUser user, String permissionErrorKey) {
    if (!isManagementRole(user.role())) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(permissionErrorKey));
    }
  }

  /**
   * ユーザー管理権限を持つロールか判定する。
   *
   * @param role ロール
   * @return 管理権限を持つ場合 true
   */
  private boolean isManagementRole(UserRole role) {
    return role == UserRole.GL || role == UserRole.OM || role == UserRole.SP || role == UserRole.SM || role == UserRole.SA;
  }

  /**
   * ロール別スコープに絞ったユーザー一覧を返す。
   *
   * @param user 認証ユーザー
   * @return 参照可能なユーザー一覧
   */
  private List<UserEntity> scopedUsers(AuthUser user) {
    List<UserEntity> all = userRepository.findByDeleteFlagFalse();

    if (user.role() == UserRole.SA) {
      return all;
    }
    if (user.role() == UserRole.OM) {
      return all.stream().filter(u -> u.getOfficeCode().equals(user.officeCode())).toList();
    }
    if (user.role() == UserRole.SP || user.role() == UserRole.SM) {
      return all.stream()
          .filter(u -> u.getOfficeCode().equals(user.officeCode()))
          .filter(u -> SP_SM_CREATABLE_ROLES.contains(UserRole.valueOf(u.getRoleCode())))
          .toList();
    }
    // GL: 自グループ配下チームのエンジニア（NG/TM）のみ。
    Set<String> teamCodes = ownGroupTeamCodes(user);
    return all.stream()
        .filter(u -> ENGINEER_ROLES.contains(UserRole.valueOf(u.getRoleCode())) && teamCodes.contains(u.getTeamCode()))
        .toList();
  }

  /**
   * 登録スコープ内かどうかを判定する。
   *
   * @param user       認証ユーザー
   * @param targetRole 登録対象ロール
   * @param officeCode 登録対象拠点コード
   * @param teamCode   登録対象チームコード
   * @return スコープ内の場合 true
   */
  private boolean canCreateInScope(AuthUser user, UserRole targetRole, String officeCode, String teamCode) {
    if (user.role() == UserRole.SA) {
      return true;
    }
    if (user.role() == UserRole.OM) {
      return ENGINEER_ROLES.contains(targetRole) && user.officeCode().equals(officeCode);
    }
    if (user.role() == UserRole.SP || user.role() == UserRole.SM) {
      return SP_SM_CREATABLE_ROLES.contains(targetRole) && user.officeCode().equals(officeCode);
    }
    // GL: エンジニア（NG/TM）を自グループ配下チームへのみ登録可。
    if (user.role() == UserRole.GL) {
      return ENGINEER_ROLES.contains(targetRole) && ownGroupTeamCodes(user).contains(teamCode);
    }
    return false;
  }

  /**
   * 削除スコープ内かどうかを判定する。
   *
   * @param user   認証ユーザー
   * @param target 削除対象ユーザー
   * @return スコープ内の場合 true
   */
  private boolean canDeleteInScope(AuthUser user, UserEntity target) {
    return canCreateInScope(user, UserRole.valueOf(target.getRoleCode()), target.getOfficeCode(), target.getTeamCode());
  }

  /**
   * ログインユーザー（GL）が管理する自グループ配下のチームコード集合を返す。
   *
   * @param user 認証ユーザー
   * @return 自グループ配下のチームコード集合
   */
  private Set<String> ownGroupTeamCodes(AuthUser user) {
    Set<String> ownGroupCodes = groupRepository.findByGlUserIdAndDeleteFlagFalse(user.userId()).stream()
        .map(GroupEntity::getGroupCode)
        .collect(Collectors.toSet());
    return teamRepository.findByGroupCodeInAndDeleteFlagFalse(ownGroupCodes).stream()
        .map(TeamEntity::getTeamCode)
        .collect(Collectors.toSet());
  }

  /**
   * ロール文字列を UserRole へ変換する。
   *
   * @param role ロール文字列
   * @return UserRole
   */
  private UserRole parseRole(String role) {
    try {
      return UserRole.valueOf(role.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCodes.VAL_001, msg(MessageKeys.USER_NO_CREATE_SCOPE));
    }
  }

  /**
   * メッセージキーから日本語メッセージを取得する。
   *
   * @param key メッセージキー
   * @return メッセージ文字列
   */
  private String msg(String key) {
    return messageSource.getMessage(key, null, Locale.JAPANESE);
  }
}
