package com.example.tariffkey.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.tariffkey.model.User;
import com.example.tariffkey.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ========== Tests for registerUser() ==========

    @Test
    @DisplayName("registerUser - Should successfully register new user when username and email are unique")
    void registerUser_ShouldRegisterSuccessfully_WhenUsernameAndEmailAreUnique() {
        // Arrange
        String username = "newuser";
        String email = "newuser@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encoded_password";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        User savedUser = User.builder()
                .id(1L)
                .username(username)
                .email(email)
                .passwordHash(encodedPassword)
                .role("USER")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(username, email, rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(encodedPassword, result.getPasswordHash());
        assertEquals("USER", result.getRole());

        verify(userRepository, times(1)).existsByUsername(username);
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser - Should throw RuntimeException when username already exists")
    void registerUser_ShouldThrowRuntimeException_WhenUsernameAlreadyExists() {
        // Arrange
        String username = "existinguser";
        String email = "new@example.com";
        String rawPassword = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.registerUser(username, email, rawPassword));

        assertEquals("Username already taken", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername(username);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser - Should throw RuntimeException when email already exists")
    void registerUser_ShouldThrowRuntimeException_WhenEmailAlreadyExists() {
        // Arrange
        String username = "newuser";
        String email = "existing@example.com";
        String rawPassword = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.registerUser(username, email, rawPassword));

        assertEquals("Email already taken", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername(username);
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== Tests for findByUsername() ==========

    @Test
    @DisplayName("findByUsername - Should return user when username exists")
    void findByUsername_ShouldReturnUser_WhenUsernameExists() {
        // Arrange
        String username = "testuser";
        User mockUser = User.builder()
                .id(1L)
                .username(username)
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role("USER")
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.findByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("findByUsername - Should throw RuntimeException when username does not exist")
    void findByUsername_ShouldThrowRuntimeException_WhenUsernameDoesNotExist() {
        // Arrange
        String username = "nonexistentuser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.findByUsername(username));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("findByUsername - Should handle null username gracefully")
    void findByUsername_ShouldHandleNull_WhenUsernameIsNull() {
        // Arrange
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.findByUsername(null));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(null);
    }
}
