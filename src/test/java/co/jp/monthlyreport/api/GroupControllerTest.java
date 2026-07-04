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
 * GroupController の MockMvc 統合テスト（API-20〜22）。
 * 初期データ: GROUP_A（office=TOKYO, glUserId=EMP009）、GROUP_OSAKA_1（office=OSAKA, glUserId=EMP002）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class GroupControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  private String login(String employeeNo) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"employeeNo": "%s", "password": "pass"}
                """.formatted(employeeNo)))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("params").path("accessToken").asText();
  }

  /**
   * グループ登録テスト用の使い捨てユーザーを作成し userId を返す。
   * 既存のシードユーザー（EMP003 等）を glUserId に指定すると、ロールが GL に
   * 書き換わり他テストへ副作用が及ぶため、専用ユーザーを都度作成する。
   */
  private long createDisposableUser(String omToken, String employeeNo) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/users/create")
            .header("Authorization", "Bearer " + omToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "employeeNo": "%s",
                  "name": "使い捨てユーザー",
                  "role": "TM",
                  "officeCode": "TOKYO",
                  "teamCode": "TEAM_A",
                  "password": "initialPass"
                }
                """.formatted(employeeNo)))
        .andExpect(status().isOk())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString())
        .path("params").path("userId").asLong();
  }

  // -------------------------------------------------------------------------
  // API-20: /groups/search
  // -------------------------------------------------------------------------

  /** GL（EMP009）が検索 → 自グループ（GROUP_A）のみ返る。 */
  @Test
  void search_as_gl_own_group_only() throws Exception {
    String token = login("EMP009");
    mockMvc.perform(post("/api/v1/groups/search")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.groups.length()").value(1))
        .andExpect(jsonPath("$.params.groups[0].groupCode").value("GROUP_A"));
  }

  /** TM（EMP004）が検索 → resultCd=AUTH_403。 */
  @Test
  void search_denied_as_tm() throws Exception {
    String token = login("EMP004");
    mockMvc.perform(post("/api/v1/groups/search")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }

  // -------------------------------------------------------------------------
  // API-21: /groups/create
  // -------------------------------------------------------------------------

  /** OM（EMP001）が自営業所にグループを登録 → groupCode が返る。 */
  @Test
  void create_success_as_om() throws Exception {
    String token = login("EMP001");
    long glUserId = createDisposableUser(token, "EMPG001");
    mockMvc.perform(post("/api/v1/groups/create")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "groupCode": "GROUP_TEST_1",
                  "groupName": "テストグループ",
                  "officeCode": "TOKYO",
                  "glUserId": %d
                }
                """.formatted(glUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.groupCode").value("GROUP_TEST_1"));
  }

  /** GL（EMP009）がグループ登録を試みる → resultCd=AUTH_403（GL はグループ作成不可）。 */
  @Test
  void create_denied_as_gl() throws Exception {
    String token = login("EMP009");
    mockMvc.perform(post("/api/v1/groups/create")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "groupCode": "GROUP_TEST_2",
                  "groupName": "テストグループ2",
                  "officeCode": "TOKYO",
                  "glUserId": 1004
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }

  /** グループコードが重複 → resultCd=GROUP_409。 */
  @Test
  void create_duplicate_group_code() throws Exception {
    String token = login("EMP001");
    long glUserId = createDisposableUser(token, "EMPG002");
    mockMvc.perform(post("/api/v1/groups/create")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "groupCode": "GROUP_A",
                  "groupName": "重複グループ",
                  "officeCode": "TOKYO",
                  "glUserId": %d
                }
                """.formatted(glUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("GROUP_409"));
  }

  // -------------------------------------------------------------------------
  // API-22: /groups/delete
  // -------------------------------------------------------------------------

  /** OM（EMP001）が自営業所のグループを削除 → deleted=true。 */
  @Test
  void delete_success_as_om() throws Exception {
    String omToken = login("EMP001");
    long glUserId = createDisposableUser(omToken, "EMPG003");
    mockMvc.perform(post("/api/v1/groups/create")
            .header("Authorization", "Bearer " + omToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "groupCode": "GROUP_TEST_3",
                  "groupName": "削除対象グループ",
                  "officeCode": "TOKYO",
                  "glUserId": %d
                }
                """.formatted(glUserId)))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/groups/delete")
            .header("Authorization", "Bearer " + omToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"groupCode": "GROUP_TEST_3"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.deleted").value(true));
  }

  /** GL（EMP009）がグループ削除を試みる → resultCd=AUTH_403（GL は削除不可）。 */
  @Test
  void delete_denied_as_gl() throws Exception {
    String token = login("EMP009");
    mockMvc.perform(post("/api/v1/groups/delete")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"groupCode": "GROUP_A"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }

  /** 存在しないグループコード → resultCd=GROUP_404。 */
  @Test
  void delete_not_found() throws Exception {
    String token = login("EMP001");
    mockMvc.perform(post("/api/v1/groups/delete")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"groupCode": "GROUP_NON_EXISTENT"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("GROUP_404"));
  }
}
