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

    // ---------------------------------------------------------------------------
    // /users/search（API-17）
    // ---------------------------------------------------------------------------

    /** GL（EMP009）が自グループ配下のエンジニアを検索 → EMP004/EMP005 が含まれる。 */
    @Test
    void search_as_gl_success() throws Exception {
        String token = login("EMP009", "pass");

        mockMvc.perform(post("/api/v1/users/search")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.items").isArray());
    }

    /** TM（EMP004）が検索 → resultCd=AUTH_403（ユーザー管理権限なし）。 */
    @Test
    void search_denied_as_tm() throws Exception {
        String token = login("EMP004", "pass");

        mockMvc.perform(post("/api/v1/users/search")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
    }

    // ---------------------------------------------------------------------------
    // /users/create（API-18）
    // ---------------------------------------------------------------------------

    /** OM（EMP001）が自営業所にエンジニア（TM）を登録 → userId が返る。 */
    @Test
    void create_success_as_om() throws Exception {
        String token = login("EMP001", "pass");

        mockMvc.perform(post("/api/v1/users/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNo": "EMP100",
                                  "name": "新規 太郎",
                                  "role": "TM",
                                  "officeCode": "TOKYO",
                                  "teamCode": "TEAM_A",
                                  "password": "initialPass"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.userId").exists());
    }

    /** GL（EMP009）が自グループ配下チームにエンジニアを登録 → 成功。 */
    @Test
    void create_success_as_gl_in_own_group() throws Exception {
        String token = login("EMP009", "pass");

        mockMvc.perform(post("/api/v1/users/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNo": "EMP101",
                                  "name": "新規 次郎",
                                  "role": "TM",
                                  "officeCode": "TOKYO",
                                  "teamCode": "TEAM_A",
                                  "password": "initialPass"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"));
    }

    /** GL（EMP009）が自グループ配下でないチーム（SALES_WEST）に登録しようとする → resultCd=AUTH_403。 */
    @Test
    void create_denied_as_gl_out_of_scope() throws Exception {
        String token = login("EMP009", "pass");

        mockMvc.perform(post("/api/v1/users/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNo": "EMP102",
                                  "name": "スコープ外太郎",
                                  "role": "TM",
                                  "officeCode": "OSAKA",
                                  "teamCode": "SALES_WEST",
                                  "password": "initialPass"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
    }

    /** 社員番号が重複 → resultCd=USER_409。 */
    @Test
    void create_duplicate_employee_no() throws Exception {
        String token = login("EMP001", "pass");

        mockMvc.perform(post("/api/v1/users/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNo": "EMP004",
                                  "name": "重複太郎",
                                  "role": "TM",
                                  "officeCode": "TOKYO",
                                  "teamCode": "TEAM_A",
                                  "password": "initialPass"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("USER_409"));
    }

    // ---------------------------------------------------------------------------
    // /users/delete（API-19）
    // ---------------------------------------------------------------------------

    /** OM（EMP001）が自営業所のエンジニアを削除 → deleted=true。 */
    @Test
    void delete_success_as_om() throws Exception {
        String omToken = login("EMP001", "pass");
        MvcResult createResult = mockMvc.perform(post("/api/v1/users/create")
                        .header("Authorization", "Bearer " + omToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeNo": "EMP103",
                                  "name": "削除対象太郎",
                                  "role": "TM",
                                  "officeCode": "TOKYO",
                                  "teamCode": "TEAM_A",
                                  "password": "initialPass"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        long userId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("params").path("userId").asLong();

        mockMvc.perform(post("/api/v1/users/delete")
                        .header("Authorization", "Bearer " + omToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": " + userId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("0"))
                .andExpect(jsonPath("$.params.deleted").value(true));
    }

    /** OM（EMP001）が自分自身を削除しようとする → resultCd=USER_400。 */
    @Test
    void delete_denied_self() throws Exception {
        String omToken = login("EMP001", "pass");
        MvcResult meResult = mockMvc.perform(post("/api/v1/users/me")
                        .header("Authorization", "Bearer " + omToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();
        long userId = objectMapper.readTree(meResult.getResponse().getContentAsString())
                .path("params").path("userId").asLong();

        mockMvc.perform(post("/api/v1/users/delete")
                        .header("Authorization", "Bearer " + omToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": " + userId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultStatus").value("1"))
                .andExpect(jsonPath("$.resultCd").value("USER_400"));
    }
}
