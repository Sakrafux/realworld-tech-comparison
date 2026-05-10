package com.sakrafux.realworld.core.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericErrorResponse {
    private ErrorBody errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorBody {
        private List<String> body;
    }

    public static GenericErrorResponse of(String message) {
        return GenericErrorResponse.builder()
                .errors(ErrorBody.builder()
                        .body(List.of(message))
                        .build())
                .build();
    }
}
