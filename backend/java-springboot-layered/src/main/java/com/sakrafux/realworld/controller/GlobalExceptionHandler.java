package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.response.GenericErrorResponse;
import com.sakrafux.realworld.exception.InvalidCredentialsException;
import com.sakrafux.realworld.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.exception.ResourceNotFoundException;
import com.sakrafux.realworld.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors (e.g., @NotBlank, @Size) from @Valid in Controllers
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(fieldName + " " + errorMessage);
        });

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new GenericErrorResponse(new GenericErrorResponse.ErrorBody(errors)));
    }

    /**
     * Handle ConstraintViolationException (e.g., @Min, @Max on @RequestParam)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GenericErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.add(violation.getPropertyPath() + " " + violation.getMessage());
        });

        log.warn("Constraint violation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new GenericErrorResponse(new GenericErrorResponse.ErrorBody(errors)));
    }

    /**
     * Handle HttpMessageNotReadableException (e.g., malformed JSON or type mismatch in body)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String error = "Invalid JSON payload or type mismatch";
        
        // Try to get a more specific message if it's a Jackson exception
        if (ex.getCause() instanceof MismatchedInputException mie) {
            if (mie.getPath() != null && !mie.getPath().isEmpty()) {
                String fieldName = mie.getPath().getLast().getPropertyName();
                error = String.format("Invalid value for field '%s'", fieldName);
            }
        }
        
        log.warn("Message not readable: {}", error);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(GenericErrorResponse.of(error));
    }

    /**
     * Handle MethodArgumentTypeMismatchException (e.g., passing "abc" for an integer @RequestParam)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String error = String.format("Parameter '%s' should be of type '%s'", 
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        log.warn("Type mismatch: {}", error);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(GenericErrorResponse.of(error));
    }

    /**
     * Handle ResourceNotFoundException (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GenericErrorResponse.of(ex.getMessage()));
    }

    /**
     * Handle ResourceAlreadyExistsException (422)
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<GenericErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        log.warn("Resource already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(GenericErrorResponse.of(ex.getMessage()));
    }

    /**
     * Handle InvalidCredentialsException (401)
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<GenericErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        log.warn("Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericErrorResponse.of(ex.getMessage()));
    }

    /**
     * Handle UnauthorizedException (401)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<GenericErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericErrorResponse.of(ex.getMessage()));
    }

    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GenericErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Unhandled runtime exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericErrorResponse.of(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"));
    }
}
