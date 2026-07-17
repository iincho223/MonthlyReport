package co.jp.monthlyreport.api.service;

import co.jp.monthlyreport.api.common.AuthUser;
import co.jp.monthlyreport.api.common.BusinessException;
import co.jp.monthlyreport.api.common.ErrorCodes;
import co.jp.monthlyreport.api.common.MessageKeys;
import co.jp.monthlyreport.api.common.ResponseKeys;
import co.jp.monthlyreport.api.dto.request.TeamCreateRequest;
import co.jp.monthlyreport.api.dto.request.TeamSearchRequest;
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
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
/**
 * チームの検索・登録・削除を扱うサービス。
 * インプット: 認証ユーザー情報と各 API リクエスト。
 * アウトプット: チームデータまたは処理結果。
 */
public class TeamService {

  private final TeamRepository teamRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final MessageSource messageSource;

  public TeamService(TeamRepository teamRepository, GroupRepository groupRepository,
      UserRepository userRepository, MessageSource messageSource) {
    this.teamRepository = teamRepository;
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.messageSource = messageSource;
  }

  /**
   * ロール別スコープでチーム一覧を検索する。
   * インプット: user 認証ユーザー、request 検索条件。
   * アウトプット: チーム一覧とページング情報。
   *
   * @param user    認証ユーザー
   * @param request 検索条件
   * @return 検索結果
   */
  public Map<String, Object> search(AuthUser user, TeamSearchRequest request) {
    requireAccess(user);

    int page = request.page() == null ? 1 : request.page();
    int size = request.size() == null ? 20 : request.size();

    List<TeamEntity> filtered = scopedTeams(user).stream()
        .filter(t -> request.groupCode() == null || request.groupCode().isBlank() || request.groupCode().equals(t.getGroupCode()))
        .filter(t -> request.teamName() == null || request.teamName().isBlank() || t.getTeamName().contains(request.teamName()))
        .sorted((a, b) -> a.getTeamCode().compareTo(b.getTeamCode()))
        .collect(Collectors.toList());

    int from = Math.min((page - 1) * size, filtered.size());
    int to = Math.min(from + size, filtered.size());

    List<Map<String, Object>> items = filtered.subList(from, to).stream().map(this::toItem).toList();

    return Map.of(
        ResponseKeys.TEAMS, items,
        ResponseKeys.TOTAL_COUNT, filtered.size(),
        ResponseKeys.PAGE, page,
        ResponseKeys.SIZE, size);
  }

  /**
   * チームを新規登録する。
   * インプット: user 認証ユーザー、request 登録内容。
   * アウトプット: 登録結果。
   *
   * @param user    認証ユーザー
   * @param request 登録リクエスト
   * @return 登録結果
   */
  public Map<String, Object> create(AuthUser user, TeamCreateRequest request) {
    if (!canManage(user)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.TEAM_NO_CREATE_PERMISSION));
    }
    GroupEntity group = groupRepository.findByGroupCodeAndDeleteFlagFalse(request.groupCode())
        .orElseThrow(() -> new BusinessException(ErrorCodes.GROUP_404, msg(MessageKeys.TEAM_GROUP_NOT_FOUND)));

    if (!canManageScope(user, group)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.TEAM_NO_CREATE_SCOPE));
    }
    UserEntity tlUser = userRepository.findByUserIdAndDeleteFlagFalse(request.tlUserId())
        .orElseThrow(() -> new BusinessException(ErrorCodes.USER_404, msg(MessageKeys.TEAM_TL_USER_NOT_FOUND)));
    if (teamRepository.existsByTeamCodeAndDeleteFlagFalse(request.teamCode())) {
      throw new BusinessException(ErrorCodes.TEAM_409, msg(MessageKeys.TEAM_DUPLICATE_CODE));
    }

    TeamEntity team = new TeamEntity();
    team.setTeamCode(request.teamCode());
    team.setTeamName(request.teamName());
    team.setGroupCode(request.groupCode());
    team.setOfficeCode(group.getOfficeCode());
    team.setTlUserId(request.tlUserId());
    team.setDeleteFlag(false);
    team.setUpdatedAt(OffsetDateTime.now());
    team.setUpdatedBy(user.employeeNo());
    team.setRegisteredAt(OffsetDateTime.now());
    team.setRegisteredBy(user.employeeNo());
    teamRepository.save(team);

    // 指定ユーザーのロールを TL に更新する。
    tlUser.setRoleCode(UserRole.TL.name());
    tlUser.setUpdatedAt(OffsetDateTime.now());
    tlUser.setUpdatedBy(user.employeeNo());
    userRepository.save(tlUser);

    return Map.of(ResponseKeys.TEAM_CODE, team.getTeamCode(), ResponseKeys.TEAM_NAME, team.getTeamName());
  }

  /**
   * チームを削除する。
   * インプット: user 認証ユーザー、teamCode 削除対象のチームコード。
   * アウトプット: 削除結果。
   *
   * @param user     認証ユーザー
   * @param teamCode 削除対象のチームコード
   * @return 削除結果
   */
  public Map<String, Object> delete(AuthUser user, String teamCode) {
    if (!canManage(user)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.TEAM_NO_DELETE_PERMISSION));
    }
    TeamEntity team = teamRepository.findByTeamCodeAndDeleteFlagFalse(teamCode)
        .orElseThrow(() -> new BusinessException(ErrorCodes.TEAM_404, msg(MessageKeys.TEAM_NOT_FOUND)));
    GroupEntity group = groupRepository.findByGroupCodeAndDeleteFlagFalse(team.getGroupCode())
        .orElseThrow(() -> new BusinessException(ErrorCodes.GROUP_404, msg(MessageKeys.TEAM_GROUP_NOT_FOUND)));

    if (!canManageScope(user, group)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.TEAM_NO_DELETE_SCOPE));
    }

    team.setDeleteFlag(true);
    team.setUpdatedAt(OffsetDateTime.now());
    team.setUpdatedBy(user.employeeNo());
    teamRepository.save(team);
    return Map.of(ResponseKeys.TEAM_CODE, team.getTeamCode(), ResponseKeys.DELETED, true);
  }

  /**
   * チーム参照 API へのアクセス権限を確認する（GL/OM/SA のみ）。
   *
   * @param user 認証ユーザー
   */
  private void requireAccess(AuthUser user) {
    if (!canManage(user)) {
      throw new BusinessException(ErrorCodes.AUTH_403, msg(MessageKeys.TEAM_NO_ACCESS_PERMISSION));
    }
  }

  /**
   * チーム管理権限を持つロールか判定する（GL/OM/SA）。
   *
   * @param user 認証ユーザー
   * @return 権限を持つ場合 true
   */
  private boolean canManage(AuthUser user) {
    return user.role() == UserRole.GL || user.role() == UserRole.OM || user.role() == UserRole.SA;
  }

  /**
   * 対象グループへの登録・削除スコープ内かどうかを判定する。
   *
   * @param user  認証ユーザー
   * @param group 対象チームが所属するグループ
   * @return スコープ内の場合 true
   */
  private boolean canManageScope(AuthUser user, GroupEntity group) {
    if (user.role() == UserRole.SA) {
      return true;
    }
    if (user.role() == UserRole.OM) {
      return user.officeCode().equals(group.getOfficeCode());
    }
    // GL: 自グループ配下のみ。
    return group.getGlUserId().equals(user.userId());
  }

  /**
   * ロール別スコープに絞ったチーム一覧を返す。
   *
   * @param user 認証ユーザー
   * @return 参照可能なチーム一覧
   */
  private List<TeamEntity> scopedTeams(AuthUser user) {
    List<TeamEntity> all = teamRepository.findByDeleteFlagFalse();

    if (user.role() == UserRole.SA) {
      return all;
    }
    if (user.role() == UserRole.OM) {
      return all.stream().filter(t -> t.getOfficeCode().equals(user.officeCode())).toList();
    }
    // GL: 自グループ配下のチームのみ。
    return all.stream()
        .filter(t -> groupRepository.findByGroupCodeAndDeleteFlagFalse(t.getGroupCode())
            .filter(g -> g.getGlUserId().equals(user.userId()))
            .isPresent())
        .toList();
  }

  /**
   * チーム 1 件をレスポンス項目へ整形する。
   *
   * @param team 対象チーム
   * @return レスポンス項目マップ
   */
  private Map<String, Object> toItem(TeamEntity team) {
    long memberCount = userRepository.findByDeleteFlagFalse().stream()
        .filter(u -> team.getTeamCode().equals(u.getTeamCode()))
        .count();
    String groupName = groupRepository.findByGroupCodeAndDeleteFlagFalse(team.getGroupCode()).map(GroupEntity::getGroupName).orElse(null);
    String tlUserName = userRepository.findByUserIdAndDeleteFlagFalse(team.getTlUserId()).map(UserEntity::getUserName).orElse(null);

    Map<String, Object> item = new HashMap<>();
    item.put(ResponseKeys.TEAM_CODE, team.getTeamCode());
    item.put(ResponseKeys.TEAM_NAME, team.getTeamName());
    item.put(ResponseKeys.GROUP_CODE, team.getGroupCode());
    item.put(ResponseKeys.GROUP_NAME, groupName);
    item.put(ResponseKeys.TL_USER_ID, team.getTlUserId());
    item.put(ResponseKeys.TL_USER_NAME, tlUserName);
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
