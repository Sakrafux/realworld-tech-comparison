package com.sakrafux.realworld.core.exception;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
public class GenericErrorResponse {
    private ErrorBody errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Serdeable
    public static class ErrorBody {
        private List<String> body;
    }

    public static GenericErrorResponse of(String message) {
        return new GenericErrorResponse(new ErrorBody(Collections.singletonList(message)));
    }

    public static GenericErrorResponse of(List<String> messages) {
        return new GenericErrorResponse(new ErrorBody(messages));
    }
}
