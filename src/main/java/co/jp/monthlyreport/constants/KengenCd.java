package co.jp.monthlyreport.constants;

/**
 * CD006 権限コード.
 */
public enum KengenCd {
    USER("001","ユーザー"),
    MANAGE("002","管理"),
    SALES("003","営業"),
    JIMU("004", "事務");

    private String code;
    private String value;

    private KengenCd(String code, String value) {
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
