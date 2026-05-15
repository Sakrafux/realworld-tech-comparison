package com.sakrafux.realworld.core.exception.handler;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.authentication.DefaultAuthorizationExceptionHandler;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Produces
@Slf4j
@Replaces(DefaultAuthorizationExceptionHandler.class)
public class CustomAuthorizationExceptionHandler implements ExceptionHandler<AuthorizationException, HttpResponse<?>> {
    @Override
    public HttpResponse<?> handle(HttpRequest request, AuthorizationException exception) {
        // If the principal is missing, the user hasn't logged in at all (401)
        if (request.getUserPrincipal().isEmpty()) {
            log.warn("Unauthorized: {}", exception.getMessage());
            return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(GenericErrorResponse.of(exception.getMessage()));
        }

        // If the principal is present but access was denied, they lack roles/permissions (403)
        log.warn("Forbidden: {}", exception.getMessage());
        return HttpResponse.status(HttpStatus.FORBIDDEN).body(GenericErrorResponse.of(exception.getMessage()));
    }
}