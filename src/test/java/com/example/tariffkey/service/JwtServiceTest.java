package com.example.tariffkey.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.core.userdetails.UserDetails;


public class JwtServiceTest {

    private JwtService jwtServ;

    private UserDetails user1;

    @BeforeEach
    void setUp(){
        jwtServ = new JwtService("TEST_SECRET_KEY_123456789012345678901234567890", 86400000);
    }


    @Test
    @DisplayName("Test should generate a token that is valid and can be verified using isTokenValid")
    void generateToken_ShouldGenerateValidToken_WhenProvidedWithUserDetails(){
        //Arrange
        user1 = org.springframework.security.core.userdetails.User
        .withUsername("testUser1")
        .password("This is a PasswordHash")
        .authorities("ROLE_USER")
        .build();

        //Act
        String token = jwtServ.generateToken(user1);

        //Assertion
        assertNotNull(token);

        assertTrue(jwtServ.isTokenValid(token, user1));
        assertEquals("testUser1" , jwtServ.extractUsername(token));
    }

    @Test
    @DisplayName("Extract username from token should have same username as UserDetails provided to generate the token")
    void extractUsername_ShouldHaveSameUsernameAsUserDetails_WhenTokenGeneratedWithUserDetails(){
        //Arrange
        user1 = org.springframework.security.core.userdetails.User
        .withUsername("john")
        .password("pw")
        .authorities("USER_ROLE")
        .build();

        //Act
        String token = jwtServ.generateToken(user1);

        //Assertion
        assertEquals("john", jwtServ.extractUsername(token));
    }

    @Test
    @DisplayName("Extract the specific claim from the token, and it should contain authorities from userDetail")
    void extractClaim_ShouldHaveClaimThatContainsAuthorityWhichIsSameAsUserDetail_WhenTokenGeneratedFromUserDetails(){
        //Arrange
        user1 = org.springframework.security.core.userdetails.User
                .withUsername("testUser1")
                .password("ThisIsAPassword")
                .authorities("ROLE_ADMIN")
                .build();

        //Act
        String token = jwtServ.generateToken(user1);
        String role = jwtServ.extractClaim(token, claims -> claims.get("role", String.class));

        //Assert
        assertEquals("ROLE_ADMIN", role);

    }


    @Test
    @DisplayName("Should return a true when tested via isTokenValid Object")
    void extractExpiration_ShouldReturnTrueForIsTokenValid_WhenTokenGenerated(){
        //Arrange
        user1 = org.springframework.security.core.userdetails.User
                .withUsername("testUser1")
                .password("pw")
                .authorities("ROLE_USER")
                .build();

        //Act
        String token = jwtServ.generateToken(user1);
        

        //Assert
        assertTrue(jwtServ.isTokenValid(token, user1)); //indirectly test for ExtractExpiration as it is private
    }

    @Test
    @DisplayName("isTokenExpired Should throw exception when provided with expired Token")
    void isTokenExpired_ShouldThrowExpiredTokenException_WhenTokenIsExpired(){
        //Arrange
        user1 = org.springframework.security.core.userdetails.User
                .withUsername("testUser1")
                .password("pw")
                .authorities("ROLE_USER")
                .build();
        JwtService jwtExpiredService = new JwtService("TEST_SECRET_KEY_123456789012345678901234567890", 0);

        //Act
        //expires immediately
        String token = jwtExpiredService.generateToken(user1);

        //Assert
        io.jsonwebtoken.ExpiredJwtException e = assertThrows(io.jsonwebtoken.ExpiredJwtException.class , ()->{jwtExpiredService.isTokenValid(token, user1);
        });
        assertNotNull(e);

    }



}
