package co.jp.monthlyreport.exception;

import lombok.Getter;

/**
 * 月報システム独自例外.
 */
public class MrException extends Exception {

    @Getter
    private String errCode;

    public MrException(String code, String msg) {
        super(msg);
        this.errCode = code;
    }

    public MrException(String code, String msg, Throwable t) {
        super(msg, t);
        this.errCode = code;
    }

}
