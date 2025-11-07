package com.example.tariffkey.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "Invalid date range: dates must be within 10 years and 'from' must be before 'to'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String fromField();
    String toField();
    int maxYearsPast() default 10;
    int maxYearsFuture() default 10;
}
