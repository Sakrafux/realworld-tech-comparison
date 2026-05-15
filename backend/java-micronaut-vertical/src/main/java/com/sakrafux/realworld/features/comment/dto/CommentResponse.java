package com.sakrafux.realworld.features.comment.dto;

import com.sakrafux.realworld.features.user.dto.ProfileResponse;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Serdeable
public class CommentResponse {
    private CommentData comment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Serdeable
    public static class CommentData {
        private Long id;
        private Instant createdAt;
        private Instant updatedAt;
        private String body;
        private ProfileResponse.ProfileData author;
    }
}
