package co.jp.monthlyreport.api.model;

public enum UserRole {
  REPORTER,
  TL,
  GL,
  OM;

  public boolean canRespond() {
    return this == TL || this == GL || this == OM;
  }
}
