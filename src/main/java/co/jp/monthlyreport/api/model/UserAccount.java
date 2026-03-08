package co.jp.monthlyreport.api.model;

public class UserAccount {
  private final Long userId;
  private final String employeeNo;
  private final String name;
  private final String password;
  private final UserRole role;
  private final String officeCode;
  private final String teamCode;
  private final boolean active;
  private final boolean deleted;

  public UserAccount(Long userId, String employeeNo, String name, String password, UserRole role, String officeCode, String teamCode, boolean active, boolean deleted) {
    this.userId = userId;
    this.employeeNo = employeeNo;
    this.name = name;
    this.password = password;
    this.role = role;
    this.officeCode = officeCode;
    this.teamCode = teamCode;
    this.active = active;
    this.deleted = deleted;
  }

  public Long getUserId() { return userId; }
  public String getEmployeeNo() { return employeeNo; }
  public String getName() { return name; }
  public String getPassword() { return password; }
  public UserRole getRole() { return role; }
  public String getOfficeCode() { return officeCode; }
  public String getTeamCode() { return teamCode; }
  public boolean isActive() { return active; }
  public boolean isDeleted() { return deleted; }
}
