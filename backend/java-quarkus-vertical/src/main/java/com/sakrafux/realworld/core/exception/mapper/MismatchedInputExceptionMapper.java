package com.sakrafux.realworld.core.exception.mapper;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class MismatchedInputExceptionMapper implements ExceptionMapper<MismatchedInputException> {

    @Override
    public Response toResponse(MismatchedInputException exception) {
        String error = "Invalid JSON payload or type mismatch";

        if (exception.getPath() != null && !exception.getPath().isEmpty()) {
            String fieldName = exception.getPath().getLast().getFieldName();
            error = String.format("Invalid value for field '%s'", fieldName);
        }

        log.warn("Message not readable: {}", error);
        return Response.status(422).entity(GenericErrorResponse.of(error)).build();
    }
}
