package co.jp.monthlyreport.api.model;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 月報レコード。フィールドは Lombok により getter/setter を自動生成する。
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ReportRecord {

  /** 月報ID */
  private String reportId;
  /** 対象月 (yyyy-MM) */
  private String month;
  /** タイトル */
  private String title;
  /** 営業情報 */
  private String salesInfo;
  /** 来月見込み残業時間 */
  private Integer nextMonthOvertimeHours;
  /** 来月見込み残業理由 */
  private String nextMonthOvertimeReason;
  /** 今月実績残業時間 */
  private Integer thisMonthOvertimeHours;
  /** 今月実績残業理由 */
  private String thisMonthOvertimeReason;
  /** 体調コンディション */
  private Map<String, String> conditions = new HashMap<>();
  /** コメント */
  private String comments;
  /** 作成者ユーザーID */
  private Long authorUserId;
  /** 作成者氏名 */
  private String reporterName;
  /** 作成者社員番号 */
  private String reporterId;
  /** 作成者ロール */
  private UserRole authorRole;
  /** 拠点コード */
  private String officeCode;
  /** チームコード */
  private String teamCode;
  /** 回答コメント */
  private String feedbackComment;
  /** 回答者ロール */
  private String responderRole;
  /** 回答者氏名 */
  private String responderName;
  /** 回答日時 */
  private OffsetDateTime respondedAt;
  /** 作成日時 */
  private OffsetDateTime createdAt;
  /** 更新日時 */
  private OffsetDateTime updatedAt;
  /** 論理削除フラグ */
  private boolean deleted;

  /**
   * 回答が登録済みかを返す。
   * インプット: なし。
   * アウトプット: 回答コメントが空でない場合 true。
   *
   * @return 回答登録済み可否
   */
  public boolean hasFeedback() {
    return feedbackComment != null && !feedbackComment.isBlank();
  }
}
