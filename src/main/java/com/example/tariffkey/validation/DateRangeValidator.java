package com.example.tariffkey.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String fromField;
    private String toField;
    private int maxYearsPast;
    private int maxYearsFuture;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.fromField = constraintAnnotation.fromField();
        this.toField = constraintAnnotation.toField();
        this.maxYearsPast = constraintAnnotation.maxYearsPast();
        this.maxYearsFuture = constraintAnnotation.maxYearsFuture();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Field fromFieldObj = value.getClass().getDeclaredField(fromField);
            Field toFieldObj = value.getClass().getDeclaredField(toField);

            fromFieldObj.setAccessible(true);
            toFieldObj.setAccessible(true);

            String fromValue = (String) fromFieldObj.get(value);
            String toValue = (String) toFieldObj.get(value);

            // If both are null or empty, that's OK (optional fields)
            if ((fromValue == null || fromValue.isEmpty()) && (toValue == null || toValue.isEmpty())) {
                return true;
            }

            // If only one is null/empty, that's invalid
            if ((fromValue == null || fromValue.isEmpty()) || (toValue == null || toValue.isEmpty())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Both dates must be provided or both must be empty")
                       .addConstraintViolation();
                return false;
            }

            // Parse dates
            ZonedDateTime from;
            ZonedDateTime to;
            try {
                from = ZonedDateTime.parse(fromValue);
                to = ZonedDateTime.parse(toValue);
            } catch (DateTimeParseException e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Invalid date format. Use ISO-8601 format (e.g., 2024-01-01T12:00:00Z)")
                       .addConstraintViolation();
                return false;
            }

            // Check if 'from' is before 'to'
            if (!from.isBefore(to)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start date/time must be before end date/time")
                       .addConstraintViolation();
                return false;
            }

            // Check date range (within maxYearsPast and maxYearsFuture)
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime minDate = now.minusYears(maxYearsPast);
            ZonedDateTime maxDate = now.plusYears(maxYearsFuture);

            if (from.isBefore(minDate) || to.isAfter(maxDate)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("Dates must be within %d years in the past and %d years in the future",
                                  maxYearsPast, maxYearsFuture))
                       .addConstraintViolation();
                return false;
            }

            return true;

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // If fields don't exist, validation fails
            return false;
        }
    }
}
