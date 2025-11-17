package com.example.tariffkey.controller;

import com.example.tariffkey.model.LoginRequest;
import com.example.tariffkey.model.RegisterRequest;
import com.example.tariffkey.model.User;
import com.example.tariffkey.security.JwtAuthenticationFilter;
import com.example.tariffkey.service.JwtService;
import com.example.tariffkey.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void registerReturnsCreatedUserDetails() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new-user");
        request.setEmail("new@example.com");
        request.setPassword("secret123");

        User saved = User.builder()
            .id(1L)
            .username("new-user")
            .email("new@example.com")
            .role("USER")
            .passwordHash("hash")
            .build();
        when(userService.registerUser(eq("new-user"), eq("new@example.com"), eq("secret123"))).thenReturn(saved);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("new-user"))
            .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).registerUser("new-user", "new@example.com", "secret123");
    }

    @Test
    void registerReturnsConflictWhenServiceThrows() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setEmail("existing@example.com");
        request.setPassword("secret123");

        when(userService.registerUser(eq("existing"), eq("existing@example.com"), eq("secret123")))
            .thenThrow(new RuntimeException("Username already taken"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Username already taken"));
    }

    @Test
    void loginAuthenticatesAndReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password1");

        User user = User.builder()
            .id(10L)
            .username("admin")
            .email("admin@example.com")
            .role("ADMIN")
            .passwordHash("hashed")
            .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("admin", "password1"));
        when(userService.findByUsername("admin")).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.email").value("admin@example.com"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(any());
    }
}
