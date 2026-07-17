package co.jp.monthlyreport.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * ReportController の MockMvc 統合テスト（API-06〜11）。
 * 各テストは独立した月（YYYY-MM）を使用し、Testcontainers 上の共有 MariaDB での状態干渉を防ぐ。
 * 初期データ: EMP004（TM）が作成した 2026-03 の月報が存在する。
 */
class ReportControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // =========================================================================
    // ヘルパーメソッド
    // =========================================================================

    /**
     * 指定社員でログインしてアクセストークンを取得する。
     *
     * @param employeeNo 社員番号
     * @param password   パスワード
     * @return アクセストークン
     */
    private String getToken(String employeeNo, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeNo":"%s","password":"%s"}
                                """.formatted(employeeNo, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("params").path("accessToken").asText();
    }

    /**
     * 指定トークンで月報を新規作成し、作成された reportId を返す。
     *
     * @param token アクセストークン
     * @param month 対象年月（yyyy-MM）
     * @return 作成された月報ID
     */
    private String createReport(String token, String month) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/reports/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildCreateBody(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("params").path("reportId").asText();
    }

    /**
     * 月報作成リクエスト本文を生成する。
     *
     * @param month 対象年月（yyyy-MM）
     * @return JSON 文字列
     */
    private String buildCreateBody(String month) {
        return """
                {
                  "month": "%s",
                  "salesInfo": "特になし",
                  "nextMonthOvertimeHours": 10,
                  "nextMonthOvertimeReason": "来月の理由",
                  "thisMonthOvertimeHours": 5,
                  "thisMonthOvertimeReason": "今月の理由",
                  "conditions": {
                    "physical": "GOOD",
                    "stress": "GOOD",
                    "relationships": "GOOD",
                    "worries": "GOOD",
                    "fatigue": "GOOD",
                    "sleep": "GOOD",
                    "motivation": "GOOD"
                  },
                  "comments": "テストコメント"
                }
                """.formatted(month);
    }

    /**
     * EMP004 の初期月報（2026-03）の reportId を EMP004 のトークンで検索して取得する。
     *
     * @param emp004Token EMP004 のアクセストークン
     * @return 初期月報の reportId
     */
    private String fetchInitialReportId(String emp004Token) throws Exception {
        MvcResult searchResult = mockMvc.perform(post("/api/v1/reports/search")
                        .header("Authorization", "Bearer " + emp004Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"month\":\"2026-03\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(searchResult.getResponse().getContentAsString())
                .path("params").path("items").get(0).path("reportId").asText();
    }

    // =========================================================================
    // /reports/search（API-06）
    // =========================================================================

    /** TM（EMP004）で検索 → 自分の月報のみ含む */
    @Test
    void search_as_tm_own_reports() throws Exception {
        String token = getToken("EMP004", "pass");

        mockMvc.perform(post("/api/v1/reports/search")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.items").isArray())
                .andExpect(jsonPath("$.params.items[0].reporterId").value("EMP004"));
    }

    /** TL（EMP003）で検索 → 同一チーム（TEAM_A）の EMP004 の月報が含まれる */
    @Test
    void search_as_tl_team_reports() throws Exception {
        String token = getToken("EMP003", "pass");

        mockMvc.perform(post("/api/v1/reports/search")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"month\":\"2026-03\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.items").isArray())
                .andExpect(jsonPath("$.params.items[0].reporterId").value("EMP004"));
    }

    // =========================================================================
    // /reports/detail（API-07）
    // =========================================================================

    /** TM が自分の月報詳細を取得 → reportId と month を確認 */
    @Test
    void detail_success() throws Exception {
        String token = getToken("EMP004", "pass");
        String reportId = fetchInitialReportId(token);

        mockMvc.perform(post("/api/v1/reports/detail")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportId\":\"" + reportId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.reportId").value(reportId))
                .andExpect(jsonPath("$.params.month").value("2026-03"));
    }

    /** 存在しない reportId を指定 → resultCd=REPORT_404 */
    @Test
    void detail_not_found() throws Exception {
        String token = getToken("EMP004", "pass");

        mockMvc.perform(post("/api/v1/reports/detail")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportId\":\"NON_EXISTENT_REPORT_ID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("REPORT_404"));
    }

    // =========================================================================
    // /reports/create（API-08）
    // =========================================================================

    /** TM が新規月報を作成 → reportId が返る */
    @Test
    void create_success() throws Exception {
        String token = getToken("EMP004", "pass");

        mockMvc.perform(post("/api/v1/reports/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildCreateBody("2026-04")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.reportId").exists());
    }

    /** 同一ユーザー・同一月で 2 回作成 → resultCd=REPORT_409 */
    @Test
    void create_duplicate_month() throws Exception {
        String token = getToken("EMP004", "pass");

        // 1 回目作成（成功）
        mockMvc.perform(post("/api/v1/reports/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildCreateBody("2026-05")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"));

        // 同一月で 2 回目作成 → 重複エラー
        mockMvc.perform(post("/api/v1/reports/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildCreateBody("2026-05")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("REPORT_409"));
    }

    /** NG（EMP005）が月報作成を試みる → resultCd=AUTH_403 */
    @Test
    void create_denied_as_ng() throws Exception {
        String token = getToken("EMP005", "pass");

        mockMvc.perform(post("/api/v1/reports/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildCreateBody("2026-04")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
    }

    /** conditions が null → バリデーションエラー（resultCd=VAL_001） */
    @Test
    void create_validation_missing_conditions() throws Exception {
        String token = getToken("EMP004", "pass");

        mockMvc.perform(post("/api/v1/reports/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "month": "2026-12",
                                  "nextMonthOvertimeHours": 0,
                                  "thisMonthOvertimeHours": 0,
                                  "conditions": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("VAL_001"));
    }

    // =========================================================================
    // /reports/update（API-09）
    // =========================================================================

    /** 作成者本人が月報を更新 → updated=true */
    @Test
    void update_success() throws Exception {
        String token = getToken("EMP004", "pass");
        String reportId = createReport(token, "2026-06");

        mockMvc.perform(post("/api/v1/reports/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reportId": "%s",
                                  "month": "2026-06",
                                  "salesInfo": "更新済み",
                                  "nextMonthOvertimeHours": 15,
                                  "nextMonthOvertimeReason": "更新後の理由",
                                  "thisMonthOvertimeHours": 8,
                                  "thisMonthOvertimeReason": "更新後の理由",
                                  "conditions": {
                                    "physical": "BEST",
                                    "stress": "GOOD",
                                    "relationships": "GOOD",
                                    "worries": "GOOD",
                                    "fatigue": "GOOD",
                                    "sleep": "GOOD",
                                    "motivation": "BEST"
                                  },
                                  "comments": "更新後コメント"
                                }
                                """.formatted(reportId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.updated").value(true));
    }

    /** 別ユーザー（EMP003/TL）が他者の月報を更新 → resultCd=AUTH_403 */
    @Test
    void update_permission_denied() throws Exception {
        String emp004Token = getToken("EMP004", "pass");
        String reportId = fetchInitialReportId(emp004Token);

        String emp003Token = getToken("EMP003", "pass");
        mockMvc.perform(post("/api/v1/reports/update")
                        .header("Authorization", "Bearer " + emp003Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reportId": "%s",
                                  "month": "2026-03",
                                  "nextMonthOvertimeHours": 0,
                                  "thisMonthOvertimeHours": 0,
                                  "conditions": {
                                    "physical": "GOOD",
                                    "stress": "GOOD",
                                    "relationships": "GOOD",
                                    "worries": "GOOD",
                                    "fatigue": "GOOD",
                                    "sleep": "GOOD",
                                    "motivation": "GOOD"
                                  }
                                }
                                """.formatted(reportId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
    }

    // =========================================================================
    // /reports/delete（API-10）
    // =========================================================================

    /** 作成者が自分の月報を削除 → deleted=true */
    @Test
    void delete_success_by_author() throws Exception {
        String token = getToken("EMP004", "pass");
        String reportId = createReport(token, "2026-07");

        mockMvc.perform(post("/api/v1/reports/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportId\":\"" + reportId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.deleted").value(true));
    }

    /** OM（EMP001）が他者の月報を削除 → deleted=true */
    @Test
    void delete_success_by_om() throws Exception {
        String emp004Token = getToken("EMP004", "pass");
        String reportId = createReport(emp004Token, "2026-08");

        String omToken = getToken("EMP001", "pass");
        mockMvc.perform(post("/api/v1/reports/delete")
                        .header("Authorization", "Bearer " + omToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportId\":\"" + reportId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.deleted").value(true));
    }

    /** TM（EMP004）が他者の月報を削除しようとする → resultCd=AUTH_403 */
    @Test
    void delete_permission_denied() throws Exception {
        // 削除対象: EMP003（TL）が作成した月報（EMP004 は TM なので他者の月報は削除不可）
        String emp003Token = getToken("EMP003", "pass");
        String reportId = createReport(emp003Token, "2026-11");

        String emp004Token = getToken("EMP004", "pass");
        mockMvc.perform(post("/api/v1/reports/delete")
                        .header("Authorization", "Bearer " + emp004Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportId\":\"" + reportId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
    }

    // =========================================================================
    // /reports/feedback/update（API-11）
    // =========================================================================

    /** TL（EMP003）が他者の月報に回答 → feedbackRegistered=true */
    @Test
    void feedback_success_by_tl() throws Exception {
        String emp004Token = getToken("EMP004", "pass");
        String reportId = createReport(emp004Token, "2026-09");

        String tlToken = getToken("EMP003", "pass");
        mockMvc.perform(post("/api/v1/reports/feedback/update")
                        .header("Authorization", "Bearer " + tlToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportId":"%s","feedbackComment":"問題ありません。引き続き頑張ってください。"}
                                """.formatted(reportId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.feedbackRegistered").value(true));
    }

    /** TL（EMP003）が自分の月報に回答 → resultCd=AUTH_403 */
    @Test
    void feedback_denied_self() throws Exception {
        String tlToken = getToken("EMP003", "pass");
        String reportId = createReport(tlToken, "2026-10");

        mockMvc.perform(post("/api/v1/reports/feedback/update")
                        .header("Authorization", "Bearer " + tlToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportId":"%s","feedbackComment":"自己回答テスト"}
                                """.formatted(reportId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
    }

    /** TM ロール（EMP004）が回答しようとする → resultCd=AUTH_403 */
    @Test
    void feedback_denied_tm_role() throws Exception {
        // TM は canRespond() = false のためロールチェックで即時拒否される
        String token = getToken("EMP004", "pass");
        String reportId = fetchInitialReportId(token);

        mockMvc.perform(post("/api/v1/reports/feedback/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportId":"%s","feedbackComment":"TM による回答試み"}
                                """.formatted(reportId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
    }
}
