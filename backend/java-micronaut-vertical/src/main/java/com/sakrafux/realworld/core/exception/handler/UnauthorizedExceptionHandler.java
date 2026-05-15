package com.sakrafux.realworld.core.exception.handler;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
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
public class UnauthorizedExceptionHandler implements ExceptionHandler<UnauthorizedException, HttpResponse<GenericErrorResponse>> {
    @Override
    public HttpResponse<GenericErrorResponse> handle(HttpRequest request, UnauthorizedException exception) {
        log.warn("Unauthorized: {}", exception.getMessage());
        return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(GenericErrorResponse.of(exception.getMessage()));
    }
}
