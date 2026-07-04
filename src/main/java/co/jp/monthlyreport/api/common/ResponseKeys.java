package co.jp.monthlyreport.api.common;

/**
 * サービス層が {@code Map} に格納する際のキー定数クラス。
 * キー文字列の散在を防ぎ、タイポによる不具合を防止する。
 * キーを変更する場合はここと対応するサービスを併せて修正すること。
 */
public final class ResponseKeys {

  // ---------------------------------------------------------------------------
  // 認証 / トークン (AuthService)
  // ---------------------------------------------------------------------------

  /** ログアウト成功フラグ */
  public static final String SUCCESS = "success";
  /** アクセストークン */
  public static final String ACCESS_TOKEN = "accessToken";
  /** リフレッシュトークン */
  public static final String REFRESH_TOKEN = "refreshToken";
  /** アクセストークン有効秒数 */
  public static final String EXPIRES_IN = "expiresIn";
  /** ユーザープロフィールオブジェクト */
  public static final String USER_PROFILE = "userProfile";

  // ---------------------------------------------------------------------------
  // ユーザー / プロフィール (UserService・AuthService 共通)
  // ---------------------------------------------------------------------------

  /** ユーザーID */
  public static final String USER_ID = "userId";
  /** 社員番号 */
  public static final String EMPLOYEE_NO = "employeeNo";
  /** 氏名 */
  public static final String NAME = "name";
  /** ロール */
  public static final String ROLE = "role";
  /** 拠点コード */
  public static final String OFFICE_CODE = "officeCode";
  /** チームコード */
  public static final String TEAM_CODE = "teamCode";

  // ---------------------------------------------------------------------------
  // ダッシュボード (DashboardService)
  // ---------------------------------------------------------------------------

  /** 合計月報件数 */
  public static final String TOTAL_REPORTS = "totalReports";
  /** 未回答月報件数 */
  public static final String PENDING_FEEDBACK_COUNT = "pendingFeedbackCount";

  // ---------------------------------------------------------------------------
  // 月報共通 (ReportService)
  // ---------------------------------------------------------------------------

  /** 月報ID */
  public static final String REPORT_ID = "reportId";
  /** 対象年月 */
  public static final String MONTH = "month";
  /** タイトル */
  public static final String TITLE = "title";
  /** 売上・商談情報 */
  public static final String SALES_INFO = "salesInfo";
  /** 来月見込み残業時間 */
  public static final String NEXT_MONTH_OVERTIME_HOURS = "nextMonthOvertimeHours";
  /** 来月見込み残業理由 */
  public static final String NEXT_MONTH_OVERTIME_REASON = "nextMonthOvertimeReason";
  /** 今月実績残業時間 */
  public static final String THIS_MONTH_OVERTIME_HOURS = "thisMonthOvertimeHours";
  /** 今月実績残業理由 */
  public static final String THIS_MONTH_OVERTIME_REASON = "thisMonthOvertimeReason";
  /** 体調コンディション */
  public static final String CONDITIONS = "conditions";
  /** コメント・相談事項 */
  public static final String COMMENTS = "comments";
  /** 投稿者氏名 */
  public static final String REPORTER_NAME = "reporterName";
  /** 投稿者社員番号 */
  public static final String REPORTER_ID = "reporterId";
  /** 投稿者ロール */
  public static final String AUTHOR_ROLE = "authorRole";
  /** 投稿者情報オブジェクト */
  public static final String AUTHOR = "author";
  /** 回答情報オブジェクト */
  public static final String FEEDBACK = "feedback";
  /** 回答登録済みフラグ */
  public static final String FEEDBACK_REGISTERED = "feedbackRegistered";
  /** 回答コメント */
  public static final String FEEDBACK_COMMENT = "feedbackComment";
  /** 回答者ロール */
  public static final String RESPONDER_ROLE = "responderRole";
  /** 回答者氏名 */
  public static final String RESPONDER_NAME = "responderName";
  /** 回答日時 */
  public static final String RESPONDED_AT = "respondedAt";
  /** 更新日時 */
  public static final String UPDATED_AT = "updatedAt";
  /** 更新済みフラグ */
  public static final String UPDATED = "updated";
  /** 削除済みフラグ */
  public static final String DELETED = "deleted";

  // ---------------------------------------------------------------------------
  // ページング (ReportService)
  // ---------------------------------------------------------------------------

  /** 一覧アイテム配列 */
  public static final String ITEMS = "items";
  /** ページング情報オブジェクト */
  public static final String PAGING = "paging";
  /** 現在ページ番号 */
  public static final String PAGE = "page";
  /** 1ページあたりの件数 */
  public static final String SIZE = "size";
  /** 総件数 */
  public static final String TOTAL_ELEMENTS = "totalElements";
  /** 総ページ数 */
  public static final String TOTAL_PAGES = "totalPages";

  // ---------------------------------------------------------------------------
  // ダッシュボード拡張 (DashboardService)
  // ---------------------------------------------------------------------------

  /** 提出済み件数 */
  public static final String SUBMITTED_COUNT = "submittedCount";
  /** 提出率(%) */
  public static final String SUBMISSION_RATE = "submissionRate";
  /** 未提出メンバー一覧 */
  public static final String UNSUBMITTED_MEMBERS = "unsubmittedMembers";

  // ---------------------------------------------------------------------------
  // エスカレーション (EscalationService)
  // ---------------------------------------------------------------------------

  /** エスカレーションID */
  public static final String ESCALATION_ID = "escalationId";
  /** 案件タイトル */
  public static final String ESC_TITLE = "title";
  /** 対象メンバー氏名 */
  public static final String TARGET_EMPLOYEE_NAME = "targetEmployeeName";
  /** 対象チーム */
  public static final String TARGET_TEAM = "targetTeam";
  /** 詳細内容 */
  public static final String DESCRIPTION = "description";
  /** 重要度 */
  public static final String SEVERITY = "severity";
  /** ステータス */
  public static final String STATUS = "status";
  /** 起票者氏名 */
  public static final String CREATED_BY_NAME = "createdByName";
  /** 起票者ロール */
  public static final String CREATED_BY_ROLE = "createdByRole";
  /** 対応ログ一覧 */
  public static final String HISTORY = "history";
  /** ログID */
  public static final String LOG_ID = "logId";
  /** ログ日時 */
  public static final String LOG_DATE = "logDate";
  /** 記録者氏名 */
  public static final String AUTHOR_NAME = "author";
  /** ログ本文 */
  public static final String LOG_TEXT = "text";
  /** ログ追加フラグ */
  public static final String LOG_ADDED = "logAdded";
  /** 担当者ユーザーID */
  public static final String ASSIGNEE_USER_ID = "assigneeUserId";
  /** 担当者氏名 */
  public static final String ASSIGNEE_NAME = "assigneeName";
  /** 対応期日 */
  public static final String DUE_DATE = "dueDate";
  /** 完了期日 */
  public static final String RESOLVED_DATE = "resolvedDate";

  // ---------------------------------------------------------------------------
  // グループ / チーム管理 (GroupService・TeamService)
  // ---------------------------------------------------------------------------

  /** グループコード */
  public static final String GROUP_CODE = "groupCode";
  /** グループ名 */
  public static final String GROUP_NAME = "groupName";
  /** グループ一覧配列 */
  public static final String GROUPS = "groups";
  /** グループリーダー(GL)ユーザーID */
  public static final String GL_USER_ID = "glUserId";
  /** グループリーダー(GL)氏名 */
  public static final String GL_USER_NAME = "glUserName";
  /** チーム名 */
  public static final String TEAM_NAME = "teamName";
  /** チーム一覧配列 */
  public static final String TEAMS = "teams";
  /** チームリーダー(TL)ユーザーID */
  public static final String TL_USER_ID = "tlUserId";
  /** チームリーダー(TL)氏名 */
  public static final String TL_USER_NAME = "tlUserName";
  /** 拠点名 */
  public static final String OFFICE_NAME = "officeName";
  /** グループ配下チーム数 */
  public static final String TEAM_COUNT = "teamCount";
  /** メンバー数 */
  public static final String MEMBER_COUNT = "memberCount";
  /** 検索結果総件数 */
  public static final String TOTAL_COUNT = "totalCount";

  // インスタンス化禁止
  private ResponseKeys() {}
}
