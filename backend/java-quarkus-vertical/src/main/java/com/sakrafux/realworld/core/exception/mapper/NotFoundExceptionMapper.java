package com.sakrafux.realworld.core.exception.mapper;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        // This can occur when an expected int query parameter is given as String
        if (exception.getCause() instanceof NumberFormatException) {
            log.warn("Number format error: {}", exception.getCause().getMessage());
            return Response.status(422)
                    .entity(GenericErrorResponse.of("Invalid parameter format"))
                    .build();
        }

        log.warn("Not found: {}", exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(GenericErrorResponse.of(exception.getMessage()))
                .build();
    }
}
