package com.sakrafux.realworld.exception;

/**
 * Exception thrown when a user attempts to log in with an incorrect email or password.
 * Handled globally to return a 401 Unauthorized HTTP status.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
