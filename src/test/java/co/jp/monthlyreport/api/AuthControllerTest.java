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
 * AuthController の MockMvc テスト。
 * ログイン・リフレッシュ・ログアウトの正常系・異常系を検証する。
 */
class AuthControllerTest extends AbstractIntegrationTest {

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
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("params").path("accessToken").asText();
    }

    /**
     * 指定社員番号でログインしてリフレッシュトークンを返す。
     *
     * @param employeeNo 社員番号
     * @param password   パスワード
     * @return リフレッシュトークン
     */
    private String loginAndGetRefreshToken(String employeeNo, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeNo": "%s", "password": "%s"}
                                """.formatted(employeeNo, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("params").path("refreshToken").asText();
    }

    // ---------------------------------------------------------------------------
    // ログイン
    // ---------------------------------------------------------------------------

    /**
     * 正常ログイン: resultStatus=0、accessToken 存在、userProfile.employeeNo 確認。
     */
    @Test
    void login_success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeNo": "EMP001", "password": "pass"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.resultCd").doesNotExist())
                .andExpect(jsonPath("$.params.accessToken").exists())
                .andExpect(jsonPath("$.params.refreshToken").exists())
                .andExpect(jsonPath("$.params.expiresIn").exists())
                .andExpect(jsonPath("$.params.userProfile.employeeNo").value("EMP001"));
    }

    /**
     * パスワード不一致: resultStatus=1、resultCd=AUTH_001。
     */
    @Test
    void login_wrong_password() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeNo": "EMP001", "password": "wrong"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_001"));
    }

    /**
     * 存在しない社員番号: resultStatus=1、resultCd=AUTH_001。
     */
    @Test
    void login_unknown_employee() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeNo": "UNKNOWN99", "password": "pass"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_001"));
    }

    /**
     * employeeNo 空: バリデーションエラー（resultStatus=1、resultCd=VAL_001）。
     * GlobalExceptionHandler が HTTP 200 + VAL_001 として返却する。
     */
    @Test
    void login_validation_blank_employeeNo() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeNo": "", "password": "pass"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("VAL_001"));
    }

    // ---------------------------------------------------------------------------
    // リフレッシュ
    // ---------------------------------------------------------------------------

    /**
     * 正常リフレッシュ: accessToken・refreshToken が新たに発行される。
     */
    @Test
    void refresh_success() throws Exception {
        String refreshToken = loginAndGetRefreshToken("EMP002", "pass");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.accessToken").exists())
                .andExpect(jsonPath("$.params.refreshToken").exists());
    }

    /**
     * 無効なリフレッシュトークン: resultStatus=1。
     */
    @Test
    void refresh_invalid_token() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "invalid-token-value"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_002"));
    }

    // ---------------------------------------------------------------------------
    // ログアウト
    // ---------------------------------------------------------------------------

    /**
     * 正常ログアウト: ログイン後にログアウトして success=true を確認する。
     */
    @Test
    void logout_success() throws Exception {
        String accessToken = login("EMP003", "pass");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.success").value(true));
    }
}
