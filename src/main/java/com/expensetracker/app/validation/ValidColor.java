package com.expensetracker.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for valid hex color codes.
 * Validates that a color is a proper hex color format (#RRGGBB or #RGB).
 */
@Documented
@Constraint(validatedBy = ValidColorValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidColor {
    String message() default "Color must be a valid hex color code (e.g., #FF0000)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}