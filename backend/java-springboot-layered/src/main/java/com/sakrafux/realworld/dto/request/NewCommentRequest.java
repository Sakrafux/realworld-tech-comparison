package com.sakrafux.realworld.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCommentRequest {
    @NotNull
    @Valid
    private CommentData comment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentData {
        @NotBlank
        private String body;
    }
}
