package com.example.tariffkey.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.tariffkey.repository.UserRepository;
import com.example.tariffkey.service.CustomUserDetailsService;
import com.example.tariffkey.model.User;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService CustUseServ;

    private User testUser1;

    @BeforeEach
    void setUp(){
        testUser1 = new User();
        testUser1.setUsername("testUser1");
        testUser1.setEmail("testUser1@gmail.com");
        testUser1.setId(1L);
        testUser1.setPasswordHash("This is a PasswordHash");
        testUser1.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("Should return UsernameNotFoundException when username doesnt exist")
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUsernameDoesntExist(){

        //Arrange
        User user2 = new User();
        user2.setUsername("This is a Nonexistent Username");
        when(userRepository.findByUsername("This is a Nonexistent Username")).thenReturn(Optional.empty());

        UsernameNotFoundException e = assertThrows(UsernameNotFoundException.class, ()-> CustUseServ.loadUserByUsername("This is a Nonexistent Username"));

        assertNotNull(e);
        verify(userRepository, times(1)).findByUsername("This is a Nonexistent Username");

    }

    @Test
    @DisplayName("Should return UserDetails when username exists")
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists(){
        //Arrange
        when(userRepository.findByUsername("testUser1")).thenReturn(Optional.of(testUser1));

        //Act
        UserDetails returnDetails = CustUseServ.loadUserByUsername("testUser1");

        //Assert
        assertEquals("testUser1", returnDetails.getUsername());
        assertEquals("This is a PasswordHash", returnDetails.getPassword());
        assertTrue(returnDetails.getAuthorities().stream().anyMatch(a-> a.getAuthority().equals("ROLE_USER")));
        verify(userRepository, times(1)).findByUsername("testUser1");
    }


}
