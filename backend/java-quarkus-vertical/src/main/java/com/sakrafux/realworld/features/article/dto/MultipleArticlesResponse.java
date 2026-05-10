package com.sakrafux.realworld.features.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultipleArticlesResponse {
    private List<ArticleResponse.ArticleData> articles;
    private int articlesCount;
}
