package com.example.tariffkey.controller;

import com.example.tariffkey.exception.TariffNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    private final RestExceptionHandler exceptionHandler = new RestExceptionHandler();

    @Test
    void handleIllegalArgumentException_ReturnsBadRequest() {
        // Given
        String errorMessage = "Invalid input parameter";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ResponseEntity<RestExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleBadRequest(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().message());
    }

    @Test
    void handleTariffNotFoundException_ReturnsNotFound() {
        // Given
        String errorMessage = "Tariff data not found for the specified criteria";
        TariffNotFoundException exception = new TariffNotFoundException(errorMessage);

        // When
        ResponseEntity<RestExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleTariffNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().message());
    }

    @Test
    void handleIllegalArgumentException_WithNullMessage_ReturnsBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException((String) null);

        // When
        ResponseEntity<RestExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleBadRequest(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().message()); // null message is preserved
    }

    @Test
    void handleTariffNotFoundException_WithNullMessage_ReturnsNotFound() {
        // Given
        TariffNotFoundException exception = new TariffNotFoundException(null);

        // When
        ResponseEntity<RestExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleTariffNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().message()); // null message is preserved
    }

    @Test
    void errorResponseRecord_ConstructorAndAccessorsWork() {
        // Given
        String message = "Test error message";

        // When
        RestExceptionHandler.ErrorResponse errorResponse = 
            new RestExceptionHandler.ErrorResponse(message);

        // Then
        assertNotNull(errorResponse);
        assertEquals(message, errorResponse.message());
    }

    @Test
    void errorResponseRecord_WithNullMessage() {
        // Given & When
        RestExceptionHandler.ErrorResponse errorResponse = 
            new RestExceptionHandler.ErrorResponse(null);

        // Then
        assertNotNull(errorResponse);
        assertNull(errorResponse.message());
    }

    @Test
    void errorResponseRecord_EqualsAndHashCode() {
        // Given
        String message = "Same message";
        RestExceptionHandler.ErrorResponse response1 = 
            new RestExceptionHandler.ErrorResponse(message);
        RestExceptionHandler.ErrorResponse response2 = 
            new RestExceptionHandler.ErrorResponse(message);
        RestExceptionHandler.ErrorResponse response3 = 
            new RestExceptionHandler.ErrorResponse("Different message");

        // Then
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void errorResponseRecord_ToString() {
        // Given
        String message = "Test message";
        RestExceptionHandler.ErrorResponse errorResponse = 
            new RestExceptionHandler.ErrorResponse(message);

        // When
        String toString = errorResponse.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("ErrorResponse"));
        assertTrue(toString.contains(message));
    }

    @Test
    void restExceptionHandler_CanBeInstantiated() {
        // Given & When
        RestExceptionHandler handler = new RestExceptionHandler();

        // Then
        assertNotNull(handler);
    }

    @Test
    void handleIllegalArgumentException_WithEmptyMessage_ReturnsBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("");

        // When
        ResponseEntity<RestExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleBadRequest(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("", response.getBody().message());
    }

    @Test
    void handleTariffNotFoundException_WithEmptyMessage_ReturnsNotFound() {
        // Given
        TariffNotFoundException exception = new TariffNotFoundException("");

        // When
        ResponseEntity<RestExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleTariffNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("", response.getBody().message());
    }
}