package com.sakrafux.realworld.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

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

    /**
     * Helper method to quickly create a single-message error response.
     */
    public static GenericErrorResponse of(String message) {
        return GenericErrorResponse.builder()
                .errors(ErrorBody.builder()
                        .body(List.of(message))
                        .build())
                .build();
    }
}
