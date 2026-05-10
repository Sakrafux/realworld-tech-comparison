package com.sakrafux.realworld.core.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class CoreExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof ResourceNotFoundException) {
            log.warn("Resource not found: {}", exception.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(GenericErrorResponse.of(exception.getMessage()))
                    .build();
        }
        if (exception instanceof ResourceAlreadyExistsException) {
            log.warn("Resource already exists: {}", exception.getMessage());
            return Response.status(422) // Unprocessable Content
                    .entity(GenericErrorResponse.of(exception.getMessage()))
                    .build();
        }
        if (exception instanceof InvalidCredentialsException) {
            log.warn("Invalid credentials");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(GenericErrorResponse.of(exception.getMessage()))
                    .build();
        }
        if (exception instanceof UnauthorizedException) {
            log.warn("Unauthorized: {}", exception.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(GenericErrorResponse.of(exception.getMessage()))
                    .build();
        }

        if (exception instanceof NotFoundException) {
            log.warn("Not found: {}", exception.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(GenericErrorResponse.of(exception.getMessage()))
                    .build();
        }

        log.error("Unhandled exception", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(GenericErrorResponse.of(exception.getMessage() != null ? exception.getMessage() : "An unexpected error occurred"))
                .build();
    }
}
