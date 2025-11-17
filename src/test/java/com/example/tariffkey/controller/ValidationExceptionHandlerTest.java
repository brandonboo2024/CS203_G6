package com.example.tariffkey.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ValidationExceptionHandlerTest {

    private ValidationExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ValidationExceptionHandler();
    }

    @Test
    void handleValidationExceptionsReturnsGroupedErrors() throws NoSuchMethodException {
        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "fieldOne", "must not be blank"));
        bindingResult.addError(new FieldError("request", "fieldOne", "size too small"));
        bindingResult.addError(new FieldError("request", "fieldTwo", "invalid value"));

        Method method = Sample.class.getDeclaredMethod("sampleMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Validation Failed");
        Map<?, ?> errors = (Map<?, ?>) body.get("errors");
        Set<String> keys = errors.keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        assertThat(keys).containsExactlyInAnyOrder("fieldOne", "fieldTwo");

        List<String> firstFieldMessages = new ArrayList<>();
        for (Object message : (Iterable<?>) errors.get("fieldOne")) {
            firstFieldMessages.add(String.valueOf(message));
        }
        assertThat(firstFieldMessages).containsExactly("must not be blank", "size too small");
    }

    @Test
    void handleConstraintViolationReturnsMessages() {
        ConstraintViolation<?> violationOne = Mockito.mock(ConstraintViolation.class);
        ConstraintViolation<?> violationTwo = Mockito.mock(ConstraintViolation.class);
        when(violationOne.getMessage()).thenReturn("first violation");
        when(violationTwo.getMessage()).thenReturn("second violation");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violationOne, violationTwo));

        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Constraint Violation");
        assertThat(body.get("violations")).asList().containsExactlyInAnyOrder("first violation", "second violation");
    }

    @Test
    void handleIllegalArgumentReturnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad input");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Bad input");
    }

    @Test
    void handleGenericExceptionReturnsInternalServerError() {
        Exception ex = new Exception("boom");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
    }

    @SuppressWarnings("unused")
    private static class Sample {
        void sampleMethod(String input) {
            // no-op, used to construct MethodParameter
        }
    }
}
