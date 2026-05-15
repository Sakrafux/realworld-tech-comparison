package com.sakrafux.realworld.core.exception.handler;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.validation.exceptions.ConstraintExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Produces
@Slf4j
@Replaces(ConstraintExceptionHandler.class)
public class CustomConstraintViolationExceptionHandler implements ExceptionHandler<ConstraintViolationException, HttpResponse<GenericErrorResponse>> {
    @Override
    public HttpResponse<GenericErrorResponse> handle(HttpRequest request, ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.toList());
        
        log.warn("Validation failed: {}", errors);
        return HttpResponse.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericErrorResponse.of(errors));
    }
}
