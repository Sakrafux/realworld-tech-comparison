package com.sakrafux.realworld.application.port.in.article;

import lombok.Builder;
import java.util.Optional;

public interface GetArticlesQuery {
    ArticleListResult getArticles(GetArticlesFilter filter);

    @Builder
    record GetArticlesFilter(
            String tag,
            String author,
            String favorited,
            int limit,
            int offset,
            Optional<String> observerEmail
    ) {}
}
