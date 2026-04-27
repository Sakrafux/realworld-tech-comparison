package com.sakrafux.realworld.core.exception;

/**
 * Exception thrown when a requested operation requires authentication, but no
 * authenticated user is present in the current SecurityContext.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
