package com.sakrafux.realworld.core.exception.handler;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import com.sakrafux.realworld.core.exception.InvalidCredentialsException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Produces
@Slf4j
public class InvalidCredentialsExceptionHandler implements ExceptionHandler<InvalidCredentialsException, HttpResponse<GenericErrorResponse>> {
    @Override
    public HttpResponse<GenericErrorResponse> handle(HttpRequest request, InvalidCredentialsException exception) {
        log.warn("Invalid credentials: {}", exception.getMessage());
        return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(GenericErrorResponse.of(exception.getMessage()));
    }
}
