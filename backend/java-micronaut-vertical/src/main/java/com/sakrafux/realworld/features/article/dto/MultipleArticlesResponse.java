package com.sakrafux.realworld.features.article.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Serdeable
public class MultipleArticlesResponse {
    private List<ArticleResponse.ArticleData> articles;
    private int articlesCount;
}
