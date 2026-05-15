package com.sakrafux.realworld.core.exception.handler;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.server.exceptions.NotFoundException;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Produces
@Slf4j
public class NotFoundExceptionHandler implements ExceptionHandler<NotFoundException, HttpResponse<?>> {
    @Override
    public HttpResponse<?> handle(HttpRequest request, NotFoundException exception) {
        log.warn("Not found: {}", request.getPath());
        return HttpResponse.status(HttpStatus.NOT_FOUND).body(GenericErrorResponse.of(exception.getMessage()));
    }
}