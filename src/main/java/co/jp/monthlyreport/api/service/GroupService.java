package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.common.MessageKeys;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.dto.request.GroupCreateRequest;
import co.jp.monthlyreport.api.dto.request.GroupSearchRequest;
import co.jp.monthlyreport.api.model.GroupRecord;
import co.jp.monthlyreport.api.model.UserAccount;
import co.jp.monthlyreport.api.model.UserRole;
import co.jp.monthlyreport.api.repository.InMemoryDataStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
/**
 * グループの検索・登録・削除を扱うサービス。
 * インプット: 認証ユーザー情報と各 API リクエスト。
 * アウトプット: グループデータまたは処理結果。
 */
public class GroupService {

  private final InMemoryDataStore dataStore;
  private final MessageSource messageSource;

  public GroupService(InMemoryDataStore dataStore, MessageSource messageSource) {
    this.dataStore = dataStore;
    this.messageSource = messageSource;
  }

  /**
   * ロール別スコープでグループ一覧を検索する。
   * インプット: user 認証ユーザー、request 検索条件。
   * アウトプット: グループ一覧とページング情報。
   *
   * @param user    認証ユーザー
   * @param request 検索条件
   * @return 検索結果
   */
  public Map<String, Object> search(AuthUser user, GroupSearchRequest request) {
    requireAccess(user);

    int page = request.page() == null ? 1 : request.page();
    int size = request.size() == null ? 20 : request.size();

    List<GroupRecord> filtered = scopedGroups(user).stream()
        .filter(g -> request.officeCode() == null || request.officeCode().isBlank() || request.officeCode().equals(g.getOfficeCode()))
        .filter(g -> request.groupName() == null || request.groupName().isBlank() || g.getGroupName().contains(request.groupName()))
        .sorted((a, b) -> a.getGroupCode().compareTo(b.getGroupCode()))
        .collect(Collectors.toList());

    int from = Math.min((page - 1) * size, filtered.size());
    int to = Math.min(from + size, filtered.size());

    List<Map<String, Object>> items = filtered.subList(from, to).stream().map(this::toItem).toList();

    return Map.of(
        ResponseKeys.GROUPS, items,
        ResponseKeys.TOTAL_COUNT, filtered.size(),
        ResponseKeys.PAGE, page,
        ResponseKeys.SIZE, size);
  }

  /**
   * グループを新規登録する。
   * インプット: user 認証ユーザー、request 登録内容。
   * アウトプット: 登録結果。
   *
   * @param user    認証ユーザー
   * @param request 登録リクエスト
   * @return 登録結果
   */
  public Map<String, Object> create(AuthUser user, GroupCreateRequest request) {
    if (!canManage(user)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.GROUP_NO_CREATE_PERMISSION));
    }
    // GL はグループ自体の登録は不可（OM/SA のみ）。
    if (user.role() == UserRole.GL) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.GROUP_NO_CREATE_PERMISSION));
    }
    if (user.role() == UserRole.OM && !user.officeCode().equals(request.officeCode())) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.GROUP_NO_CREATE_SCOPE));
    }
    UserAccount glUser = dataStore.findUserById(request.glUserId())
        .filter(u -> !u.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.USER_404, msg(MessageKeys.GROUP_GL_USER_NOT_FOUND)));
    if (dataStore.findGroupByCode(request.groupCode()).filter(g -> !g.isDeleted()).isPresent()) {
      throw new BusinessException(ErrorCodes.GROUP_409, msg(MessageKeys.GROUP_DUPLICATE_CODE));
    }

    GroupRecord group = new GroupRecord();
    group.setGroupCode(request.groupCode());
    group.setGroupName(request.groupName());
    group.setOfficeCode(request.officeCode());
    group.setGlUserId(request.glUserId());
    group.setDeleted(false);
    dataStore.saveGroup(group);

    // 指定ユーザーのロールを GL に更新する。
    glUser.setRole(UserRole.GL);
    dataStore.registerUser(glUser);

    return Map.of(ResponseKeys.GROUP_CODE, group.getGroupCode(), ResponseKeys.GROUP_NAME, group.getGroupName());
  }

  /**
   * グループを削除する。
   * インプット: user 認証ユーザー、groupCode 削除対象のグループコード。
   * アウトプット: 削除結果。
   *
   * @param user      認証ユーザー
   * @param groupCode 削除対象のグループコード
   * @return 削除結果
   */
  public Map<String, Object> delete(AuthUser user, String groupCode) {
    if (!canManage(user) || user.role() == UserRole.GL) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.GROUP_NO_DELETE_PERMISSION));
    }
    GroupRecord group = dataStore.findGroupByCode(groupCode)
        .filter(g -> !g.isDeleted())
        .orElseThrow(() -> new BusinessException(ErrorCodes.GROUP_404, msg(MessageKeys.GROUP_NOT_FOUND)));

    if (user.role() == UserRole.OM && !user.officeCode().equals(group.getOfficeCode())) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.GROUP_NO_DELETE_SCOPE));
    }

    group.setDeleted(true);
    dataStore.saveGroup(group);
    return Map.of(ResponseKeys.GROUP_CODE, group.getGroupCode(), ResponseKeys.DELETED, true);
  }

  /**
   * グループ参照 API へのアクセス権限を確認する（GL/OM/SA のみ）。
   *
   * @param user 認証ユーザー
   */
  private void requireAccess(AuthUser user) {
    if (!canManage(user)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.GROUP_NO_ACCESS_PERMISSION));
    }
  }

  /**
   * グループ管理権限を持つロールか判定する（GL/OM/SA）。
   *
   * @param user 認証ユーザー
   * @return 権限を持つ場合 true
   */
  private boolean canManage(AuthUser user) {
    return user.role() == UserRole.GL || user.role() == UserRole.OM || user.role() == UserRole.SA;
  }

  /**
   * ロール別スコープに絞ったグループ一覧を返す。
   *
   * @param user 認証ユーザー
   * @return 参照可能なグループ一覧
   */
  private List<GroupRecord> scopedGroups(AuthUser user) {
    List<GroupRecord> all = new ArrayList<>(dataStore.findAllGroups()).stream()
        .filter(g -> !g.isDeleted())
        .toList();

    if (user.role() == UserRole.SA) {
      return all;
    }
    if (user.role() == UserRole.OM) {
      return all.stream().filter(g -> g.getOfficeCode().equals(user.officeCode())).toList();
    }
    // GL: 自グループのみ。
    return all.stream().filter(g -> g.getGlUserId().equals(user.userId())).toList();
  }

  /**
   * グループ 1 件をレスポンス項目へ整形する。
   *
   * @param group 対象グループ
   * @return レスポンス項目マップ
   */
  private Map<String, Object> toItem(GroupRecord group) {
    long teamCount = dataStore.findAllTeams().stream()
        .filter(t -> !t.isDeleted() && t.getGroupCode().equals(group.getGroupCode()))
        .count();
    long memberCount = dataStore.findAllUsers().stream()
        .filter(u -> !u.isDeleted())
        .filter(u -> dataStore.findAllTeams().stream()
            .anyMatch(t -> !t.isDeleted() && t.getGroupCode().equals(group.getGroupCode()) && t.getTeamCode().equals(u.getTeamCode())))
        .count();
    String glUserName = dataStore.findUserById(group.getGlUserId()).map(UserAccount::getName).orElse(null);

    Map<String, Object> item = new HashMap<>();
    item.put(ResponseKeys.GROUP_CODE, group.getGroupCode());
    item.put(ResponseKeys.GROUP_NAME, group.getGroupName());
    item.put(ResponseKeys.OFFICE_CODE, group.getOfficeCode());
    item.put(ResponseKeys.GL_USER_ID, group.getGlUserId());
    item.put(ResponseKeys.GL_USER_NAME, glUserName);
    item.put(ResponseKeys.TEAM_COUNT, teamCount);
    item.put(ResponseKeys.MEMBER_COUNT, memberCount);
    return item;
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
