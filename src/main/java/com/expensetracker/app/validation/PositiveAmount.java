package com.expensetracker.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for positive amounts.
 * Validates that an amount is greater than zero.
 */
@Documented
@Constraint(validatedBy = PositiveAmountValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveAmount {
    String message() default "Amount must be greater than zero";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}