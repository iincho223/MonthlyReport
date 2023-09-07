package co.jp.monthlyreport.constants;

public enum URI {
    LOGIN("/login", "ログイン"),
    LOGIN_AUTH("/login/auth", "ログイン認証"),
    TOP("/top", "TOP"),
    ERROR("/error", "エラー")
    ;

    private String uri;
    private String name;

    private URI(String uri, String name) {
        this.uri = uri;
        this.name = name;
    }

    public String getUri() {
        return this.uri;
    }

    public String getName() {
        return this.name;
    }

}
