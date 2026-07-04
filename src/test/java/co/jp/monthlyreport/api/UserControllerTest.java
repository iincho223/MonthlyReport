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
 * UserController の MockMvc テスト。
 * /users/me の正常系・認証エラー系を検証する。
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

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
    // /users/me
    // ---------------------------------------------------------------------------

    /**
     * 正常取得: ログイン後に /users/me を呼び出して employeeNo・role を確認する。
     */
    @Test
    void me_success() throws Exception {
        String accessToken = login("EMP004", "pass");

        mockMvc.perform(post("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.employeeNo").value("EMP004"))
                .andExpect(jsonPath("$.params.role").value("TM"))
                .andExpect(jsonPath("$.params.userId").exists())
                .andExpect(jsonPath("$.params.name").exists())
                .andExpect(jsonPath("$.params.officeCode").exists())
                .andExpect(jsonPath("$.params.teamCode").exists());
    }

    /**
     * Authorization ヘッダなし: MissingRequestHeaderException が GlobalExceptionHandler の
     * システムエラーハンドラに捕捉され、HTTP 200 + resultStatus=9 が返る。
     */
    @Test
    void me_unauthorized_no_header() throws Exception {
        mockMvc.perform(post("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("9"));
    }

    /**
     * 不正なアクセストークン: resultStatus=1、resultCd=AUTH_001。
     */
    @Test
    void me_unauthorized_invalid_token() throws Exception {
        mockMvc.perform(post("/api/v1/users/me")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_001"));
    }
}
