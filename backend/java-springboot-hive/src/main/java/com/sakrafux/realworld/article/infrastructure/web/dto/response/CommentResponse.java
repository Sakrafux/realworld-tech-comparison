package com.sakrafux.realworld.article.infrastructure.web.dto.response;

import com.sakrafux.realworld.user.infrastructure.web.dto.response.ProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private CommentData comment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentData {
        private Long id;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private String body;
        private ProfileResponse.ProfileData author;
    }
}
