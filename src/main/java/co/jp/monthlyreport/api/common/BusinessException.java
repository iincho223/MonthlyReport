package co.jp.monthlyreport.api.common;

public class BusinessException extends RuntimeException {
  private final String resultCd;

  public BusinessException(String resultCd, String message) {
    super(message);
    this.resultCd = resultCd;
  }

  public String getResultCd() {
    return resultCd;
  }
}
