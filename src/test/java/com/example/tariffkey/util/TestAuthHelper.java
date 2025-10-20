package com.example.tariffkey.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestConfiguration
public class TestAuthHelper {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public AuthTokenProvider authTokenProvider() {
        return new AuthTokenProvider(mockMvc, objectMapper);
    }

    public static class AuthTokenProvider {
        private final MockMvc mockMvc;
        private final ObjectMapper objectMapper;

        public AuthTokenProvider(MockMvc mockMvc, ObjectMapper objectMapper) {
            this.mockMvc = mockMvc;
            this.objectMapper = objectMapper;
        }

        /**
         * Register a user and return a valid JWT by calling /auth/register and /auth/login
         * If the user already exists, login will still return a token.
         */
        public String obtainToken(String username, String email, String password) throws Exception {
            // Register
            var reg = new java.util.HashMap<String, String>();
            reg.put("username", username);
            reg.put("email", email);
            reg.put("password", password);

            try {
                mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isOk());
            } catch (Exception ex) {
                // ignore - user may already exist
            }

            // Login
            var login = new java.util.HashMap<String, String>();
            login.put("username", username);
            login.put("password", password);

            var result = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andReturn();

            String body = result.getResponse().getContentAsString();
            var node = objectMapper.readTree(body);
            return node.get("token").asText();
        }
    }
}
