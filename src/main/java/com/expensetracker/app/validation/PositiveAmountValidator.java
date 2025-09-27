package com.expensetracker.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Validator implementation for PositiveAmount annotation.
 * Validates that a BigDecimal amount is positive (greater than zero).
 */
public class PositiveAmountValidator implements ConstraintValidator<PositiveAmount, BigDecimal> {

    @Override
    public void initialize(PositiveAmount constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(BigDecimal amount, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull annotation
        if (amount == null) {
            return true;
        }
        
        // Amount must be greater than zero
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
}