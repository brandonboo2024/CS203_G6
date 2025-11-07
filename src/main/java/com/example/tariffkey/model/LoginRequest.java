package com.example.tariffkey.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class LoginRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_\\-.;&|`$(){}\\[\\]@#!%^*+=~]{3,20}$",
             message = "Username must be 3-20 characters, alphanumeric, underscore, hyphen, or common special characters")
    private String username;

    @NotBlank
    @Size(min = 8, max = 128)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&.;&|`$(){}\\[\\]^+=~]{8,}$",
             message = "Password must be at least 8 characters with at least one letter and one number")
    private String password;
}
