package co.jp.monthlyreport.api.common;

import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse> handleBusiness(BusinessException ex) {
    return ResponseEntity.ok(ApiResponse.businessError(ex.getResultCd(), ex.getMessage()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class})
  public ResponseEntity<ApiResponse> handleValidation(Exception ex) {
    String message;
    if (ex instanceof MethodArgumentNotValidException manv) {
      message = manv.getBindingResult().getFieldErrors().stream()
          .map(err -> err.getField() + ":" + err.getDefaultMessage())
          .collect(Collectors.joining(", "));
    } else if (ex instanceof BindException be) {
      message = be.getBindingResult().getFieldErrors().stream()
          .map(err -> err.getField() + ":" + err.getDefaultMessage())
          .collect(Collectors.joining(", "));
    } else {
      message = ex.getMessage();
    }
    return ResponseEntity.ok(ApiResponse.businessError(ErrorCodes.VAL_001, message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> handleSystem(Exception ex) {
    return ResponseEntity.ok(ApiResponse.systemError("システムエラーが発生しました"));
  }
}
