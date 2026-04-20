package com.sakrafux.realworld.exception;

/**
 * Exception thrown when an attempt is made to create a resource (like User or Article)
 * with a unique identifier (like email, username, or slug) that already exists.
 * Handled globally to return a 422 Unprocessable Entity HTTP status.
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
