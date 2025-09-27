package com.expensetracker.app.exception;

/**
 * Exception thrown when an expense tracker entity is not found.
 * 
 * This is a runtime exception that indicates a requested entity
 * (expense, category, etc.) does not exist in the system.
 */
public class EntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final String identifier;

    public EntityNotFoundException(String entityType, String identifier) {
        super(String.format("%s not found with identifier: %s", entityType, identifier));
        this.entityType = entityType;
        this.identifier = identifier;
    }

    public EntityNotFoundException(String entityType, Long id) {
        this(entityType, id.toString());
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityType = "Unknown";
        this.identifier = "Unknown";
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.entityType = "Unknown";
        this.identifier = "Unknown";
    }

    public String getEntityType() {
        return entityType;
    }

    public String getIdentifier() {
        return identifier;
    }
}