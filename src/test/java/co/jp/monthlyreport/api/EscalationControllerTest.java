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
 * EscalationController の MockMvc 統合テスト（API-12〜16）。
 * 各テストはログイン → トークン取得 → API 呼び出しの順で独立して実行する。
 */
@SpringBootTest
@AutoConfigureMockMvc
class EscalationControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  // -------------------------------------------------------------------------
  // ヘルパー
  // -------------------------------------------------------------------------

  /** 指定ユーザーでログインしてアクセストークンを返す。 */
  private String login(String employeeNo) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"employeeNo": "%s", "password": "pass"}
                """.formatted(employeeNo)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andReturn();
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("params").path("accessToken").asText();
  }

  /**
   * 指定トークンでエスカレーションを 1 件作成し、escalationId を返す。
   * テスト内のセットアップ用途のみに使用する。
   */
  private String createEscalation(String token) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/escalations/create")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "テスト案件",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "description": "テスト用の詳細内容",
                  "severity": "MEDIUM",
                  "status": "PENDING",
                  "dueDate": "2026-04-30"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andReturn();
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("params").path("escalationId").asText();
  }

  // -------------------------------------------------------------------------
  // API-12: /escalations/search
  // -------------------------------------------------------------------------

  /** TL（EMP003）で検索 → resultStatus=0、items と paging が返る。 */
  @Test
  void search_as_tl_success() throws Exception {
    String token = login("EMP003");
    mockMvc.perform(post("/api/v1/escalations/search")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.items").isArray())
        .andExpect(jsonPath("$.params.paging").exists());
  }

  /** TM（EMP004）で検索 → resultCd=AUTH_403。 */
  @Test
  void search_as_tm_denied() throws Exception {
    String token = login("EMP004");
    mockMvc.perform(post("/api/v1/escalations/search")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }

  /** SM（EMP007）で検索 → resultStatus=0（SP/SM/SA も参照可能）。 */
  @Test
  void search_as_sm_success() throws Exception {
    String token = login("EMP007");
    mockMvc.perform(post("/api/v1/escalations/search")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.items").isArray());
  }

  /** SA（EMP008）で検索 → resultStatus=0（全件参照、画面側でマスク表示）。 */
  @Test
  void search_as_sa_success() throws Exception {
    String token = login("EMP008");
    mockMvc.perform(post("/api/v1/escalations/search")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.items").isArray());
  }

  // -------------------------------------------------------------------------
  // API-14: /escalations/create
  // -------------------------------------------------------------------------

  /** TL（EMP003）が起票 → resultStatus=0、escalationId が返る。 */
  @Test
  void create_success_as_tl() throws Exception {
    String token = login("EMP003");
    mockMvc.perform(post("/api/v1/escalations/create")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "長期欠勤対応",
                  "targetEmployeeName": "山田 健太",
                  "targetTeam": "チームA",
                  "description": "詳細内容",
                  "severity": "HIGH",
                  "status": "PENDING",
                  "dueDate": "2026-04-30"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.escalationId").exists());
  }

  /** TM（EMP004）が起票 → resultCd=AUTH_403。 */
  @Test
  void create_denied_as_tm() throws Exception {
    String token = login("EMP004");
    mockMvc.perform(post("/api/v1/escalations/create")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "不正起票テスト",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "severity": "LOW",
                  "status": "PENDING",
                  "dueDate": "2026-04-30"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }

  /**
   * SP（EMP006）が起票 → resultCd=AUTH_403。
   * SP/SM/SA は参照・更新は可能だが起票は TL/GL/OM のみ許可される。
   */
  @Test
  void create_denied_as_sp() throws Exception {
    String token = login("EMP006");
    mockMvc.perform(post("/api/v1/escalations/create")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "不正起票テスト",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "severity": "LOW",
                  "status": "PENDING",
                  "dueDate": "2026-04-30"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }

  /** title が空の場合 → resultStatus=1、resultCd=VAL_001。 */
  @Test
  void create_validation_blank_title() throws Exception {
    String token = login("EMP003");
    mockMvc.perform(post("/api/v1/escalations/create")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "severity": "LOW",
                  "status": "PENDING",
                  "dueDate": "2026-04-30"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("VAL_001"));
  }

  /** dueDate が未入力の場合 → resultStatus=1、resultCd=VAL_001。 */
  @Test
  void create_validation_missing_due_date() throws Exception {
    String token = login("EMP003");
    mockMvc.perform(post("/api/v1/escalations/create")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "対応期日未入力テスト",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "severity": "LOW",
                  "status": "PENDING"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("VAL_001"));
  }

  // -------------------------------------------------------------------------
  // API-13: /escalations/detail
  // -------------------------------------------------------------------------

  /** TL（EMP003）が自分で起票したエスカレーションの詳細取得 → escalationId が返る。 */
  @Test
  void detail_success() throws Exception {
    String token = login("EMP003");
    String escalationId = createEscalation(token);

    mockMvc.perform(post("/api/v1/escalations/detail")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"escalationId": "%s"}
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.escalationId").value(escalationId));
  }

  /** 存在しない escalationId → resultCd=ESC_404。 */
  @Test
  void detail_not_found() throws Exception {
    String token = login("EMP003");
    mockMvc.perform(post("/api/v1/escalations/detail")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"escalationId": "ESC_NON_EXISTENT_99999"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("ESC_404"));
  }

  // -------------------------------------------------------------------------
  // API-15: /escalations/update
  // -------------------------------------------------------------------------

  /** 起票者（EMP003/TL）が更新 → updated=true。 */
  @Test
  void update_success_as_creator() throws Exception {
    String token = login("EMP003");
    String escalationId = createEscalation(token);

    mockMvc.perform(post("/api/v1/escalations/update")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "escalationId": "%s",
                  "title": "更新後タイトル",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "description": "更新後の詳細",
                  "severity": "LOW",
                  "status": "ONGOING",
                  "dueDate": "2026-05-15"
                }
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.updated").value(true));
  }

  /**
   * OM（EMP001）が EMP003（TL）起票のエスカレーションを更新 → updated=true。
   * EMP002（GL/OSAKA）は TOKYO のエスカレーションを更新する権限を持たないため、
   * 代わりに OM（EMP001）で更新権限を検証する。
   */
  @Test
  void update_success_as_om() throws Exception {
    String tlToken = login("EMP003");
    String escalationId = createEscalation(tlToken);

    String omToken = login("EMP001");
    mockMvc.perform(post("/api/v1/escalations/update")
            .header("Authorization", "Bearer " + omToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "escalationId": "%s",
                  "title": "OM更新タイトル",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "description": "OM による更新",
                  "severity": "HIGH",
                  "status": "ONGOING",
                  "dueDate": "2026-05-15"
                }
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.updated").value(true));
  }

  /**
   * 起票者でない TL が他チームのエスカレーションを更新 → AUTH_403。
   * OM（EMP001, teamCode=HQ）で起票したエスカレーションを
   * TL（EMP003, teamCode=TEAM_A）が更新しようとするケース。
   */
  @Test
  void update_denied_as_non_creator_tl() throws Exception {
    // OM が起票 → teamCode=HQ のエスカレーションが作成される。
    String omToken = login("EMP001");
    String escalationId = createEscalation(omToken);

    // TL（TEAM_A）は createdBy!=EMP003 かつ teamCode!=TEAM_A のため更新不可。
    String tlToken = login("EMP003");
    mockMvc.perform(post("/api/v1/escalations/update")
            .header("Authorization", "Bearer " + tlToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "escalationId": "%s",
                  "title": "不正更新タイトル",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "description": "不正更新の詳細",
                  "severity": "LOW",
                  "status": "PENDING",
                  "dueDate": "2026-05-15"
                }
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }

  /** ステータスを RESOLVED にする際に完了期日（resolvedDate）が未入力 → resultCd=VAL_001。 */
  @Test
  void update_denied_resolved_without_resolved_date() throws Exception {
    String token = login("EMP003");
    String escalationId = createEscalation(token);

    mockMvc.perform(post("/api/v1/escalations/update")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "escalationId": "%s",
                  "title": "解決済みタイトル",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "description": "解決済みにする",
                  "severity": "LOW",
                  "status": "RESOLVED",
                  "dueDate": "2026-05-15"
                }
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("VAL_001"));
  }

  /** 完了期日を添えて RESOLVED に更新 → updated=true。 */
  @Test
  void update_success_resolved_with_resolved_date() throws Exception {
    String token = login("EMP003");
    String escalationId = createEscalation(token);

    mockMvc.perform(post("/api/v1/escalations/update")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "escalationId": "%s",
                  "title": "解決済みタイトル",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "description": "解決済みにする",
                  "severity": "LOW",
                  "status": "RESOLVED",
                  "dueDate": "2026-05-15",
                  "resolvedDate": "2026-05-10"
                }
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.updated").value(true));
  }

  /** 担当者を設定後、未設定（null）へ変更しようとする → resultCd=ESC_400。 */
  @Test
  void update_denied_unset_assignee() throws Exception {
    String token = login("EMP003");
    String escalationId = createEscalation(token);

    // まず担当者（EMP004）を設定する。
    mockMvc.perform(post("/api/v1/escalations/update")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "escalationId": "%s",
                  "title": "担当者設定テスト",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "severity": "LOW",
                  "status": "PENDING",
                  "dueDate": "2026-05-15",
                  "assigneeUserId": 1004
                }
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"));

    // 担当者を未設定（null）へ戻そうとすると拒否される。
    mockMvc.perform(post("/api/v1/escalations/update")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "escalationId": "%s",
                  "title": "担当者解除テスト",
                  "targetEmployeeName": "テスト 太郎",
                  "targetTeam": "チームA",
                  "severity": "LOW",
                  "status": "PENDING",
                  "dueDate": "2026-05-15"
                }
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("ESC_400"));
  }

  // -------------------------------------------------------------------------
  // API-16: /escalations/log/add
  // -------------------------------------------------------------------------

  /** TL（EMP003）がログ追加 → logAdded=true。 */
  @Test
  void log_add_success() throws Exception {
    String token = login("EMP003");
    String escalationId = createEscalation(token);

    mockMvc.perform(post("/api/v1/escalations/log/add")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "escalationId": "%s",
                  "logText": "対応状況を確認しました。"
                }
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.logAdded").value(true));
  }

  /** TM（EMP004）がログ追加 → resultCd=AUTH_403。 */
  @Test
  void log_add_denied_as_tm() throws Exception {
    // エスカレーション作成には TL を使用する。
    String tlToken = login("EMP003");
    String escalationId = createEscalation(tlToken);

    String tmToken = login("EMP004");
    mockMvc.perform(post("/api/v1/escalations/log/add")
            .header("Authorization", "Bearer " + tmToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "escalationId": "%s",
                  "logText": "不正ログ追加テスト"
                }
                """.formatted(escalationId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("1"))
        .andExpect(jsonPath("$.resultCd").value("AUTH_403"));
  }
}
