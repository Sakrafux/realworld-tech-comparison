package com.sakrafux.realworld.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;
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
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private boolean favorited;
        private int favoritesCount;
        private ProfileResponse.ProfileData author;
    }
}
