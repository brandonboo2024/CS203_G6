package com.example.tariffkey.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateRangeValidatorTest {

    private DateRangeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setup() {
        validator = new DateRangeValidator();

        // Build a fake annotation instance
        ValidDateRange annotation = Mockito.mock(ValidDateRange.class);
        Mockito.when(annotation.fromField()).thenReturn("from");
        Mockito.when(annotation.toField()).thenReturn("to");
        Mockito.when(annotation.maxYearsPast()).thenReturn(10);
        Mockito.when(annotation.maxYearsFuture()).thenReturn(10);

        validator.initialize(annotation);

        context = Mockito.mock(ConstraintValidatorContext.class);
        Mockito.when(context.buildConstraintViolationWithTemplate(Mockito.anyString()))
                .thenReturn(Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
    }

    // Test Class to pass into validator
    static class TestRangeObj {
        public String from;
        public String to;
        TestRangeObj(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }

    @Test
    void testBothNullValid() {
        TestRangeObj obj = new TestRangeObj(null, null);
        assertTrue(validator.isValid(obj, context));
    }

    @Test
    void testOneMissingInvalid() {
        TestRangeObj obj = new TestRangeObj("2024-01-01T10:00Z", null);
        assertFalse(validator.isValid(obj, context));
    }

    @Test
    void testInvalidDateFormat() {
        TestRangeObj obj = new TestRangeObj("invalid-date", "2024-01-01T10:00Z");
        assertFalse(validator.isValid(obj, context));
    }

    @Test
    void testFromAfterToInvalid() {
        TestRangeObj obj = new TestRangeObj(
                "2024-01-02T10:00Z",
                "2024-01-01T10:00Z"
        );
        assertFalse(validator.isValid(obj, context));
    }

    @Test
    void testOutOfRangeInvalid() {
        ZonedDateTime tooOld = ZonedDateTime.now().minusYears(20);
        ZonedDateTime tooFuture = ZonedDateTime.now().plusYears(20);

        TestRangeObj obj = new TestRangeObj(
                tooOld.toString(),
                tooFuture.toString()
        );
        assertFalse(validator.isValid(obj, context));
    }

    @Test
    void testValidRangeInsideLimits() {
        ZonedDateTime from = ZonedDateTime.now().minusYears(1);
        ZonedDateTime to = ZonedDateTime.now().plusYears(1);

        TestRangeObj obj = new TestRangeObj(from.toString(), to.toString());
        assertTrue(validator.isValid(obj, context));
    }

    @Test
    void testInvalidFieldNames() {
        // Create a different annotation pointing to wrong fields
        ValidDateRange badAnnotation = Mockito.mock(ValidDateRange.class);
        Mockito.when(badAnnotation.fromField()).thenReturn("notExist1");
        Mockito.when(badAnnotation.toField()).thenReturn("notExist2");
        Mockito.when(badAnnotation.maxYearsPast()).thenReturn(10);
        Mockito.when(badAnnotation.maxYearsFuture()).thenReturn(10);

        validator.initialize(badAnnotation);

        TestRangeObj obj = new TestRangeObj("2024-01-01T10:00Z", "2024-01-02T10:00Z");
        assertFalse(validator.isValid(obj, context)); // fails gracefully
    }
}
