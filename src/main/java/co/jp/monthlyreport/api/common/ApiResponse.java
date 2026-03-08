package co.jp.monthlyreport.api.common;

public record ApiResponse(String resultStatus, String resultMsg, String resultCd, Object params) {

  public static ApiResponse ok(Object params) {
    return new ApiResponse("0", null, null, params);
  }

  public static ApiResponse businessError(String resultCd, String resultMsg) {
    return new ApiResponse("1", resultMsg, resultCd, null);
  }

  public static ApiResponse systemError(String resultMsg) {
    return new ApiResponse("9", resultMsg, "SYS_500", null);
  }
}
