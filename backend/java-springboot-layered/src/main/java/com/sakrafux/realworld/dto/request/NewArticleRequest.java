package com.sakrafux.realworld.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewArticleRequest {
    @NotNull
    @Valid
    private ArticleData article;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArticleData {
        @NotBlank
        @Size(max = 100)
        private String title;

        @NotBlank
        @Size(max = 255)
        private String description;

        @NotBlank
        private String body;

        private List<@Size(max = 20) String> tagList;
    }
}
