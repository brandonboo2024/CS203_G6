package com.example.tariffkey.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
@AutoConfigureMockMvc
class TariffIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String adminJwtToken;

    @BeforeEach
    void setup() throws Exception {
        MvcResult result =
            mockMvc.perform(
                    post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        adminJwtToken = body.path("token").asText();
    }

    // Step 2: Test access to protected endpoint with valid JWT
    @Test
    void adminCanAccessProtectedTariffApi() throws Exception {
        mockMvc.perform(
                get("/api/tariff")
                    .header("Authorization", "Bearer " + adminJwtToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo("TariffController active")));
    }

    // Step 3: Test unauthorized access (no token)
    @Test
    void accessWithoutTokenShouldFail() throws Exception {
        mockMvc.perform(get("/api/tariff"))
            .andExpect(status().isForbidden());
    }

    // Step 4: Test access with invalid token
    @Test
    void accessWithInvalidTokenShouldFail() throws Exception {
        mockMvc.perform(
                get("/api/tariff")
                    .header("Authorization", "Bearer invalidtoken123")
            )
            .andExpect(status().isForbidden());
    }
}
