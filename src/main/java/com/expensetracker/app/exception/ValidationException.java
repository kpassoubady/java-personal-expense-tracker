package com.expensetracker.app.exception;

/**
 * Exception thrown when a business rule validation fails.
 * 
 * This exception is used for business logic validation errors,
 * such as duplicate category names, invalid expense amounts, etc.
 */
public class ValidationException extends RuntimeException {

    private final String field;
    private final String value;
    private final String validationRule;

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.value = null;
        this.validationRule = null;
    }

    public ValidationException(String field, String value, String validationRule) {
        super(String.format("Validation failed for field '%s' with value '%s': %s", field, value, validationRule));
        this.field = field;
        this.value = value;
        this.validationRule = validationRule;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.value = null;
        this.validationRule = null;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public String getValidationRule() {
        return validationRule;
    }
}