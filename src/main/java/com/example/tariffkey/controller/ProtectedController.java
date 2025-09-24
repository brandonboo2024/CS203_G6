package com.example.tariffkey.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedController {

    @GetMapping("/api/protected")
    public String protectedEndpoint() {
        return "✅ You have accessed a protected resource!";
    }
}