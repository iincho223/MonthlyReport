package co.jp.monthlyreport.api.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * エスカレーションレコード。
 * フィールドは Lombok により getter/setter を自動生成する。
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class EscalationRecord {

  /** エスカレーションID */
  private String escalationId;

  /** 案件タイトル */
  private String title;

  /** 対象メンバー氏名 */
  private String targetEmployeeName;

  /** 対象チーム名 */
  private String targetTeam;

  /** 詳細内容 */
  private String description;

  /** 重要度 (LOW / MEDIUM / HIGH) */
  private String severity;

  /** ステータス (PENDING / ONGOING / RESOLVED) */
  private String status;

  /** 起票者ユーザーID */
  private Long createdBy;

  /** 起票者氏名 */
  private String createdByName;

  /** 起票者ロール */
  private String createdByRole;

  /** 担当者ユーザーID（SP ロールのスコープ判定用） */
  private Long assigneeUserId;

  /** 起票者の拠点コード（スコープ判定用） */
  private String officeCode;

  /** 起票者のチームコード（スコープ判定用） */
  private String teamCode;

  /** 作成日時 */
  private OffsetDateTime createdAt;

  /** 更新日時 */
  private OffsetDateTime updatedAt;

  /** 対応ログ一覧 */
  private List<EscalationLogRecord> logs = new ArrayList<>();
}
