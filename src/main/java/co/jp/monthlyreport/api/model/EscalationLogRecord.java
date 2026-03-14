package co.jp.monthlyreport.api.model;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * エスカレーション対応ログ。
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class EscalationLogRecord {

  /** ログID */
  private String logId;

  /** 対応ログ本文 */
  private String logText;

  /** 記録者ユーザーID */
  private Long authorUserId;

  /** 記録者氏名 */
  private String authorName;

  /** 記録日時 */
  private OffsetDateTime createdAt;
}
