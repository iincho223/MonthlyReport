package co.jp.monthlyreport.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * DashboardController の MockMvc テスト。
 * /dashboard/summary のロール別集計結果・認証エラー系を検証する。
 */
@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ---------------------------------------------------------------------------
    // ヘルパー
    // ---------------------------------------------------------------------------

    /**
     * 指定社員番号でログインしてアクセストークンを返す。
     *
     * @param employeeNo 社員番号
     * @param password   パスワード
     * @return アクセストークン
     */
    private String login(String employeeNo, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeNo": "%s", "password": "%s"}
                                """.formatted(employeeNo, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("params").path("accessToken").asText();
    }

    // ---------------------------------------------------------------------------
    // /dashboard/summary
    // ---------------------------------------------------------------------------

    /**
     * TL ロール（EMP003）でサマリー取得: submissionRate・unsubmittedMembers が存在する。
     */
    @Test
    void summary_as_tl() throws Exception {
        String accessToken = login("EMP003", "pass");

        mockMvc.perform(post("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"month": "2025-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.totalReports").exists())
                .andExpect(jsonPath("$.params.pendingFeedbackCount").exists())
                .andExpect(jsonPath("$.params.submittedCount").exists())
                .andExpect(jsonPath("$.params.submissionRate").exists())
                .andExpect(jsonPath("$.params.unsubmittedMembers").exists());
    }

    /**
     * TM ロール（EMP004）でサマリー取得: submissionRate=0、unsubmittedMembers=[] となる。
     */
    @Test
    void summary_as_tm() throws Exception {
        String accessToken = login("EMP004", "pass");

        mockMvc.perform(post("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"month": "2025-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.submissionRate").value(0))
                .andExpect(jsonPath("$.params.unsubmittedMembers").isArray())
                .andExpect(jsonPath("$.params.unsubmittedMembers").isEmpty());
    }

    /**
     * トークンなし: MissingRequestHeaderException が GlobalExceptionHandler の
     * システムエラーハンドラに捕捉され、HTTP 200 + resultStatus=9 が返る。
     */
    @Test
    void summary_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/dashboard/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"month": "2025-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("9"));
    }

    /**
     * 不正なアクセストークン: resultStatus=1、resultCd=AUTH_001。
     */
    @Test
    void summary_invalid_token() throws Exception {
        mockMvc.perform(post("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"month": "2025-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_001"));
    }
}
