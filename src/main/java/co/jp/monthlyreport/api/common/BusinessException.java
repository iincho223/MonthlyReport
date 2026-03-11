package co.jp.monthlyreport.api.common;

import lombok.Getter;

/**
 * 業務例外。resultCd にエラーコードを保持し、Lombok で getter を自動生成する。
 */
@Getter
public class BusinessException extends RuntimeException {

  /** 業務エラーコード */
  private final String resultCd;

  /**
   * インプット: resultCd 業務エラーコード、message エラーメッセージ。
   * アウトプット: 業務例外インスタンス。
   *
   * @param resultCd 業務エラーコード
   * @param message エラーメッセージ
   */
  public BusinessException(String resultCd, String message) {
    super(message);
    this.resultCd = resultCd;
  }
}
