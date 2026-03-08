package co.jp.monthlyreport.api.model;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class ReportRecord {
  private String reportId;
  private String month;
  private String title;
  private String salesInfo;
  private Integer nextMonthOvertimeHours;
  private String nextMonthOvertimeReason;
  private Integer thisMonthOvertimeHours;
  private String thisMonthOvertimeReason;
  private Map<String, String> conditions = new HashMap<>();
  private String comments;
  private Long authorUserId;
  private String reporterName;
  private String reporterId;
  private UserRole authorRole;
  private String officeCode;
  private String teamCode;
  private String feedbackComment;
  private String responderRole;
  private String responderName;
  private OffsetDateTime respondedAt;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private boolean deleted;

  public String getReportId() { return reportId; }
  public void setReportId(String reportId) { this.reportId = reportId; }
  public String getMonth() { return month; }
  public void setMonth(String month) { this.month = month; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getSalesInfo() { return salesInfo; }
  public void setSalesInfo(String salesInfo) { this.salesInfo = salesInfo; }
  public Integer getNextMonthOvertimeHours() { return nextMonthOvertimeHours; }
  public void setNextMonthOvertimeHours(Integer nextMonthOvertimeHours) { this.nextMonthOvertimeHours = nextMonthOvertimeHours; }
  public String getNextMonthOvertimeReason() { return nextMonthOvertimeReason; }
  public void setNextMonthOvertimeReason(String nextMonthOvertimeReason) { this.nextMonthOvertimeReason = nextMonthOvertimeReason; }
  public Integer getThisMonthOvertimeHours() { return thisMonthOvertimeHours; }
  public void setThisMonthOvertimeHours(Integer thisMonthOvertimeHours) { this.thisMonthOvertimeHours = thisMonthOvertimeHours; }
  public String getThisMonthOvertimeReason() { return thisMonthOvertimeReason; }
  public void setThisMonthOvertimeReason(String thisMonthOvertimeReason) { this.thisMonthOvertimeReason = thisMonthOvertimeReason; }
  public Map<String, String> getConditions() { return conditions; }
  public void setConditions(Map<String, String> conditions) { this.conditions = conditions; }
  public String getComments() { return comments; }
  public void setComments(String comments) { this.comments = comments; }
  public Long getAuthorUserId() { return authorUserId; }
  public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }
  public String getReporterName() { return reporterName; }
  public void setReporterName(String reporterName) { this.reporterName = reporterName; }
  public String getReporterId() { return reporterId; }
  public void setReporterId(String reporterId) { this.reporterId = reporterId; }
  public UserRole getAuthorRole() { return authorRole; }
  public void setAuthorRole(UserRole authorRole) { this.authorRole = authorRole; }
  public String getOfficeCode() { return officeCode; }
  public void setOfficeCode(String officeCode) { this.officeCode = officeCode; }
  public String getTeamCode() { return teamCode; }
  public void setTeamCode(String teamCode) { this.teamCode = teamCode; }
  public String getFeedbackComment() { return feedbackComment; }
  public void setFeedbackComment(String feedbackComment) { this.feedbackComment = feedbackComment; }
  public String getResponderRole() { return responderRole; }
  public void setResponderRole(String responderRole) { this.responderRole = responderRole; }
  public String getResponderName() { return responderName; }
  public void setResponderName(String responderName) { this.responderName = responderName; }
  public OffsetDateTime getRespondedAt() { return respondedAt; }
  public void setRespondedAt(OffsetDateTime respondedAt) { this.respondedAt = respondedAt; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
  public boolean isDeleted() { return deleted; }
  public void setDeleted(boolean deleted) { this.deleted = deleted; }

  public boolean hasFeedback() {
    return feedbackComment != null && !feedbackComment.isBlank();
  }
}
