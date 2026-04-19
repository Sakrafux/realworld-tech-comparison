package com.sakrafux.realworld.exception;

import lombok.Getter;

/**
 * Exception thrown when a requested domain entity (like User, Article, etc.)
 * cannot be found in the database.
 * Handled globally to return a 404 Not Found HTTP status.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
