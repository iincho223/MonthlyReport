package co.jp.monthlyreport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import co.jp.monthlyreport.exception.MrException;
import co.jp.monthlyreport.util.MrLoggerUtils;

/**
 * Webコントローラー共通例外ハンドラー.
 */
@ControllerAdvice
public class WebExceptionHandler {
    @Autowired
    private MrLoggerUtils log;

    @ExceptionHandler(Exception.class)
    public String commonExceptionHandler(Exception e) {
        log.error("E01-MRC00-9901", e);
        return "error";
    }

    @ExceptionHandler(MrException.class)
    public String commonExceptionHandler(MrException e) {
        log.error(e.getErrCode(), e);
        return "error";
    }

}
