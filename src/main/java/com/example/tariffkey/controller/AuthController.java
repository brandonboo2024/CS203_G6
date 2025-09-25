package com.example.tariffkey.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import com.example.tariffkey.model.*;
import com.example.tariffkey.service.*;
import com.example.tariffkey.security.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest req) {
        User newUser = userService.registerUser(req.getUsername(), req.getEmail(), req.getPassword());
        return ResponseEntity.ok(newUser); // newUser has joinedAt
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        User user = userService.findByUsername(req.getUsername());
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(
                token,
                user.getUsername(),
                user.getEmail()));
    }
}
