package com.sakrafux.realworld.core.exception.mapper;

import com.sakrafux.realworld.core.exception.GenericErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Provider
@Slf4j
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> errors = exception.getConstraintViolations().stream()
                .map(violation -> {
                    String lastPath = "";
                    for (var node : violation.getPropertyPath()) {
                        lastPath = node.getName();
                    }
                    return lastPath + " " + violation.getMessage();
                })
                .collect(Collectors.toList());

        log.warn("Validation failed: {}", errors);
        return Response.status(422)
                .entity(new GenericErrorResponse(new GenericErrorResponse.ErrorBody(errors)))
                .build();
    }
}
