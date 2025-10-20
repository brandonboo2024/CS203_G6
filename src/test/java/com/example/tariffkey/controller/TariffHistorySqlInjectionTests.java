package com.example.tariffkey.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Run tests with the 'test' profile (uses in-memory H2) and ensure each test runs inside
// a transaction that is rolled back to avoid persisting any changes even if you
// uncomment authenticated tests later.
@org.springframework.test.context.ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class TariffHistorySqlInjectionTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private com.example.tariffkey.util.TestAuthHelper.AuthTokenProvider authTokenProvider;

    private static final List<String> payloads = List.of(
            "'; DROP TABLE users;--",
            "' OR '1'='1",
            "'); DELETE FROM product; --",
            "\"; UPDATE product SET base_price = 0; --",
            "'); EXEC xp_cmdshell('dir'); --"
    );

    @Test
    void maliciousProductCodeShouldNotBreakEndpoint() throws Exception {
        for (String payload : payloads) {
            TariffHistoryController.TariffHistoryRequest req = new TariffHistoryController.TariffHistoryRequest();
            req.setProductCode(payload);
            req.setStartDate("2020-01-01");
            req.setEndDate("2020-12-31");

            String body = objectMapper.writeValueAsString(req);

            var result = mockMvc.perform(post("/api/tariff/history")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andReturn();

            String resp = result.getResponse().getContentAsString();
            // Basic sanity: response is JSON array or empty
            assertThat(resp).isNotNull();
            assertThat(resp).startsWith("[");
        }
    }

    /*
      ==================================================================
      Authenticated endpoint tests (commented out)
      ==================================================================

      The tests below target `/api/calculations/save` and `/api/calculations/history`,
      which require authentication. They are intentionally commented out so they
      don't run in CI until you provide a way to obtain a valid JWT for the test
      environment. To enable them:
        1. Uncomment the block.
        2. Implement `obtainTestJwt()` to create a test user and return a JWT
           (or hardcode a token from a test account).
        3. Ensure the test database is reset/isolated for safe testing.

      Example test code (uncomment to run):

      @Autowired
      private com.example.tariffkey.util.TestAuthHelper.AuthTokenProvider authTokenProvider;

      @Test
      void authenticatedSaveCalculationShouldHandleMaliciousPayload() throws Exception {
          // Example TariffResponse-like payload (adjust fields to match your DTO)
          Map<String,Object> payload = Map.of(
              "itemPrice", 123.45,
              "tariffRate", 5.0,
              "tariffAmount", 6.17,
              "handlingFee", 1.0,
              "inspectionFee", 0.5,
              "processingFee", 0.25,
              "otherFees", 0.0,
              "totalPrice", 131.37,
              "segments", List.of(
                  Map.of("from", "2020-01-01T00:00:00Z", "to", "2020-06-01T00:00:00Z", "ratePercent", 5.0)
              )
          );

          String jwt = authTokenProvider.obtainToken("test_sql_user", "test_sql_user@example.com", "password123");

          mockMvc.perform(post("/api/calculations/save")
                  .header("Authorization", "Bearer " + jwt)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(payload)))
              .andExpect(status().is2xxSuccessful());
      }

      @Test
      void authenticatedGetHistoryShouldNotBreakWithMaliciousQuery() throws Exception {
          String jwt = authTokenProvider.obtainToken("test_sql_user", "test_sql_user@example.com", "password123");

          mockMvc.perform(get("/api/calculations/history")
                  .header("Authorization", "Bearer " + jwt)
                  .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().is2xxSuccessful());
      }

    */
}
