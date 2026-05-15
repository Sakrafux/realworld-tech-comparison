package com.sakrafux.realworld.core.exception;

import lombok.Getter;

@Getter
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
