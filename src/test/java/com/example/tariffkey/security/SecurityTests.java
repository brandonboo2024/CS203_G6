package com.example.tariffkey.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class SecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Test 1: Register endpoint should be public
    @Test
    void registerShouldBePublic() throws Exception {
        String uniqueUser = "testuser" + "12345678";
        var request = new RegisterRequest(uniqueUser, uniqueUser + "@example.com", "password1!");

        var result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        int status = result.getResponse().getStatus();
        org.assertj.core.api.Assertions.assertThat(status)
                .as("register should be accessible")
                .isIn(200, 409);
    }

    // Test 2: Login endpoint should be public (should not get 403)
    @Test
    void loginShouldBePublic() throws Exception {
        var request = new LoginRequest("admin", "password");
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError()); // Expect 401 (invalid creds), not 403
    }

    // Test 3: Protected API should reject anonymous users
    @Test
    void anonymousAccessToProtectedShouldFail() throws Exception {
        mockMvc.perform(get("/api/tariff"))
                .andExpect(status().isForbidden());
    }

    // Test 4: Non-admin user should be forbidden
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void nonAdminCannotAccessProtectedApi() throws Exception {
        mockMvc.perform(get("/api/tariff"))
                .andExpect(status().isForbidden());
    }

    // Test 5: Admin user should be allowed
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void adminCanAccessProtectedApi() throws Exception {
        mockMvc.perform(get("/api/tariff"))
                .andExpect(status().isOk());
    }

    // Helper DTOs to simulate JSON body
    static class RegisterRequest {
        public String username;
        public String email;
        public String password;
        RegisterRequest(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }

    static class LoginRequest {
        public String username;
        public String password;
        LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
