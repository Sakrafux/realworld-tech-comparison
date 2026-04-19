package com.sakrafux.realworld.exception;

/**
 * Exception thrown during user registration when the provided email or username
 * is already taken by another user.
 * Handled globally to return a 422 Unprocessable Entity HTTP status.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
