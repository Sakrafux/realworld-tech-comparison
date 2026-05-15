package com.sakrafux.realworld.core.exception.handler;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
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
public class GlobalThrowableHandler implements ExceptionHandler<Throwable, HttpResponse<GenericErrorResponse>> {
    @Override
    public HttpResponse<GenericErrorResponse> handle(HttpRequest request, Throwable exception) {
        log.error("Unhandled exception: ", exception);
        String message = exception.getMessage() != null ? exception.getMessage() : "An unexpected error occurred";
        return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericErrorResponse.of(message));
    }
}
