package com.sakrafux.realworld.core.exception.handler;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ConversionErrorHandler;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Produces
@Slf4j
@Replaces(ConversionErrorHandler.class)
public class CustomConversionErrorExceptionHandler implements ExceptionHandler<ConversionErrorException, HttpResponse<GenericErrorResponse>> {
    @Override
    public HttpResponse<GenericErrorResponse> handle(HttpRequest request, ConversionErrorException exception) {
        log.warn("Conversion failed: {}", exception.getMessage());
        return HttpResponse.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericErrorResponse.of(exception.getMessage()));
    }
}
