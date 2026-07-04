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
 * TeamController の MockMvc 統合テスト（API-23〜25）。
 * 初期データ: TEAM_A（group=GROUP_A, office=TOKYO, tlUserId=EMP003）、
 * SALES_WEST（group=GROUP_OSAKA_1, office=OSAKA, tlUserId=EMP010）。
 * 既存シードユーザー（EMP003 等）を tlUserId に指定するとロールが TL に
 * 書き換わり他テストへ副作用が及ぶため、各テストは使い捨てユーザー/グループを用意する。
 */
@SpringBootTest
@AutoConfigureMockMvc
class TeamControllerTest {

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

  /** OM 権限でテスト用の使い捨てグループを作成し groupCode を返す。 */
  private String createDisposableGroup(String omToken, String groupCode, String glEmployeeNo) throws Exception {
    long glUserId = createDisposableUser(omToken, glEmployeeNo);
    mockMvc.perform(post("/api/v1/groups/create")
            .header("Authorization", "Bearer " + omToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "groupCode": "%s",
                  "groupName": "テスト用グループ",
                  "officeCode": "TOKYO",
                  "glUserId": %d
                }
                """.formatted(groupCode, glUserId)))
        .andExpect(status().isOk());
    return groupCode;
  }

  // -------------------------------------------------------------------------
  // API-23: /teams/search
  // -------------------------------------------------------------------------

  /** GL（EMP009）が検索 → 自グループ（GROUP_A）配下の TEAM_A のみ返る。 */
  @Test
  void search_as_gl_own_group_only() throws Exception {
    String token = login("EMP009");
    mockMvc.perform(post("/api/v1/teams/search")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.teams.length()").value(1))
        .andExpect(jsonPath("$.params.teams[0].teamCode").value("TEAM_A"));
  }

  /** TM（EMP004）が検索 → resultCd=AUTH_403。 */
  @Test
  void search_denied_as_tm() throws Exception {
    String token = login("EMP004");
    mockMvc.perform(post("/api/v1/teams/search")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }

  // -------------------------------------------------------------------------
  // API-24: /teams/create
  // -------------------------------------------------------------------------

  /** GL（EMP009）が自グループ配下にチームを登録 → teamCode が返る。 */
  @Test
  void create_success_as_gl() throws Exception {
    String glToken = login("EMP009");
    long tlUserId = createDisposableUser(login("EMP001"), "EMPT001");

    mockMvc.perform(post("/api/v1/teams/create")
            .header("Authorization", "Bearer " + glToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "teamCode": "TEAM_TEST_1",
                  "teamName": "テストチーム",
                  "groupCode": "GROUP_A",
                  "tlUserId": %d
                }
                """.formatted(tlUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.teamCode").value("TEAM_TEST_1"));
  }

  /** GL（EMP009）が他グループ（GROUP_OSAKA_1）配下にチームを登録しようとする → resultCd=AUTH_403。 */
  @Test
  void create_denied_as_gl_out_of_scope() throws Exception {
    String glToken = login("EMP009");
    long tlUserId = createDisposableUser(login("EMP001"), "EMPT002");

    mockMvc.perform(post("/api/v1/teams/create")
            .header("Authorization", "Bearer " + glToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "teamCode": "TEAM_TEST_2",
                  "teamName": "スコープ外チーム",
                  "groupCode": "GROUP_OSAKA_1",
                  "tlUserId": %d
                }
                """.formatted(tlUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }

  /** 存在しないグループコードを指定 → resultCd=GROUP_404。 */
  @Test
  void create_group_not_found() throws Exception {
    String omToken = login("EMP001");
    long tlUserId = createDisposableUser(omToken, "EMPT003");

    mockMvc.perform(post("/api/v1/teams/create")
            .header("Authorization", "Bearer " + omToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "teamCode": "TEAM_TEST_3",
                  "teamName": "グループ未存在チーム",
                  "groupCode": "GROUP_NON_EXISTENT",
                  "tlUserId": %d
                }
                """.formatted(tlUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("GROUP_404"));
  }

  /** チームコードが重複 → resultCd=TEAM_409。 */
  @Test
  void create_duplicate_team_code() throws Exception {
    String omToken = login("EMP001");
    long tlUserId = createDisposableUser(omToken, "EMPT004");

    mockMvc.perform(post("/api/v1/teams/create")
            .header("Authorization", "Bearer " + omToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "teamCode": "TEAM_A",
                  "teamName": "重複チーム",
                  "groupCode": "GROUP_A",
                  "tlUserId": %d
                }
                """.formatted(tlUserId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("TEAM_409"));
  }

  // -------------------------------------------------------------------------
  // API-25: /teams/delete
  // -------------------------------------------------------------------------

  /** OM（EMP001）が自営業所のチームを削除 → deleted=true。 */
  @Test
  void delete_success_as_om() throws Exception {
    String omToken = login("EMP001");
    String groupCode = createDisposableGroup(omToken, "GROUP_DEL_TEST_1", "EMPT005");
    long tlUserId = createDisposableUser(omToken, "EMPT006");

    mockMvc.perform(post("/api/v1/teams/create")
            .header("Authorization", "Bearer " + omToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "teamCode": "TEAM_DEL_TEST_1",
                  "teamName": "削除対象チーム",
                  "groupCode": "%s",
                  "tlUserId": %d
                }
                """.formatted(groupCode, tlUserId)))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/teams/delete")
            .header("Authorization", "Bearer " + omToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"teamCode": "TEAM_DEL_TEST_1"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.deleted").value(true));
  }

  /** 存在しないチームコード → resultCd=TEAM_404。 */
  @Test
  void delete_not_found() throws Exception {
    String token = login("EMP001");
    mockMvc.perform(post("/api/v1/teams/delete")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"teamCode": "TEAM_NON_EXISTENT"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("TEAM_404"));
  }
}
