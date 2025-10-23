package com.example.tariffkey.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TariffIntegrationTest {

    @LocalServerPort
    private int port;

    private String adminJwtToken;

    @BeforeEach
    void setup() {
        RestAssured.port = port;

        // Step 1: Login to get a valid JWT token
        adminJwtToken =
            given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"admin123\"}") 
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");
    }

    // Step 2: Test access to protected endpoint with valid JWT
    @Test
    void adminCanAccessProtectedTariffApi() {
        given()
            .header("Authorization", "Bearer " + adminJwtToken)
        .when()
            .get("/api/tariff")
        .then()
            .statusCode(200)
            .body(equalTo("TariffController active"));
    }

    // Step 3: Test unauthorized access (no token)
    @Test
    void accessWithoutTokenShouldFail() {
        given()
        .when()
            .get("/api/tariff")
        .then()
            .statusCode(403); // or 401 depending on your filter
    }

    // Step 4: Test access with invalid token
    @Test
    void accessWithInvalidTokenShouldFail() {
        given()
            .header("Authorization", "Bearer invalidtoken123")
        .when()
            .get("/api/tariff")
        .then()
            .statusCode(403); // or 401
    }
}
