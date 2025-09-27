package com.expensetracker.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for category names.
 * Validates that a category name is unique across all categories.
 */
@Documented
@Constraint(validatedBy = UniqueCategoryNameValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueCategoryName {
    String message() default "A category with this name already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}