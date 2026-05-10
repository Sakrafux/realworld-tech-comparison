package com.sakrafux.realworld.features.article.dto;

import com.sakrafux.realworld.features.user.dto.ProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleResponse {
    private ArticleData article;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArticleData {
        private String slug;
        private String title;
        private String description;
        private String body;
        private List<String> tagList;
        private Instant createdAt;
        private Instant updatedAt;
        private boolean favorited;
        private int favoritesCount;
        private ProfileResponse.ProfileData author;
    }
}
