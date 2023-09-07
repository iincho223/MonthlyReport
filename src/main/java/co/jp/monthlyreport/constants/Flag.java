package co.jp.monthlyreport.constants;

/**
 * CD001 汎用フラグ.
 */
public enum Flag {
    TRUE("1","有効"),
    FALSE("0", "無効");

    private String code;
    private String value;

    private Flag(String code, String value) {
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
