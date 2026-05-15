package com.sakrafux.realworld.core.exception.handler;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import com.sakrafux.realworld.core.exception.ResourceAlreadyExistsException;
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
public class ResourceAlreadyExistsExceptionHandler implements ExceptionHandler<ResourceAlreadyExistsException, HttpResponse<GenericErrorResponse>> {
    @Override
    public HttpResponse<GenericErrorResponse> handle(HttpRequest request, ResourceAlreadyExistsException exception) {
        log.warn("Resource already exists: {}", exception.getMessage());
        return HttpResponse.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericErrorResponse.of(exception.getMessage()));
    }
}
