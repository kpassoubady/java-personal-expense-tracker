package com.expensetracker.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for ValidColor annotation.
 * Validates that a color string is a proper hex color format.
 */
public class ValidColorValidator implements ConstraintValidator<ValidColor, String> {

    private static final String HEX_COLOR_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

    @Override
    public void initialize(ValidColor constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String color, ConstraintValidatorContext context) {
        // Null or empty values are valid (handled by @NotNull/@NotBlank if required)
        if (color == null || color.trim().isEmpty()) {
            return true;
        }
        
        // Check if color matches hex pattern
        return color.matches(HEX_COLOR_PATTERN);
    }
}