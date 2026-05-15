package com.sakrafux.realworld.features.article.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Serdeable
public class UpdateArticleRequest {
    @NotNull
    @Valid
    private ArticleData article;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Serdeable
    public static class ArticleData {
        @Size(max = 100)
        private String title;

        @Size(max = 255)
        private String description;

        private String body;
    }
}
