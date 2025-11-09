package com.example.tariffkey.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import com.example.tariffkey.model.*;
import com.example.tariffkey.service.*;
import com.example.tariffkey.security.*;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = {"http://localhost:5173","https://frontend-production-a446.up.railway.app"})
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@jakarta.validation.Valid @RequestBody RegisterRequest req) {
        try {
            User newUser = userService.registerUser(req.getUsername(), req.getEmail(), req.getPassword());
            // avoid returning passwordHash or sensitive fields
            Map<String, Object> out = new HashMap<>();
            out.put("id", newUser.getId());
            out.put("username", newUser.getUsername());
            out.put("email", newUser.getEmail());
            out.put("role", newUser.getRole());
            return ResponseEntity.ok(out);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@jakarta.validation.Valid @RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        User user = userService.findByUsername(req.getUsername());
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(
                token,
                user.getUsername(),
                user.getEmail()
        ));
    }
}
