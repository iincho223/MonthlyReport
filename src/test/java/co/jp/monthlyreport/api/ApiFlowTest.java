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

@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void loginThenGetMe() throws Exception {
    MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "employeeNo": "EMP004",
                  "password": "pass"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.accessToken").exists())
        .andReturn();

    JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    String accessToken = root.path("params").path("accessToken").asText();

    mockMvc.perform(post("/api/v1/users/me")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultStatus").value("0"))
        .andExpect(jsonPath("$.params.employeeNo").value("EMP004"));
  }
}
