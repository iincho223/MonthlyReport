package co.jp.monthlyreport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.jp.monthlyreport.constants.ResultStatusKbn;
import co.jp.monthlyreport.dto.response.CommonApiResponse;
import co.jp.monthlyreport.exception.MrException;
import co.jp.monthlyreport.util.MrLoggerUtils;

/**
 * Apiコントローラー共通例外ハンドラー.
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    @Autowired
    private MrLoggerUtils log;

    @ExceptionHandler(Exception.class)
    public CommonApiResponse commonExceptionHandler(Exception e) {
        log.error("E01-MRC00-9901", e);
        CommonApiResponse res = new CommonApiResponse();
        res.setResultCd("E01-MRC00-9901");
        res.setResultMsg("予期せぬエラー");
        res.setResultStatus(ResultStatusKbn.ERROR.getCode());
        return res;
    }

    @ExceptionHandler(MrException.class)
    public CommonApiResponse commonExceptionHandler(MrException e) {
        log.error(e.getErrCode(), e);
        CommonApiResponse res = new CommonApiResponse();
        res.setResultCd(e.getErrCode());
        res.setResultMsg(e.getMessage());
        res.setResultStatus(ResultStatusKbn.ERROR.getCode());
        return res;
    }

}
