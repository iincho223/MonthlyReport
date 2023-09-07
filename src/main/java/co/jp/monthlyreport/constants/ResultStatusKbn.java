package co.jp.monthlyreport.constants;

/**
 * CD001 汎用フラグ.
 */
public enum ResultStatusKbn {
    NORMAL("0", "正常"),
    ERROR("1", "エラー");

    private String code;
    private String value;

    private ResultStatusKbn(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }
}
