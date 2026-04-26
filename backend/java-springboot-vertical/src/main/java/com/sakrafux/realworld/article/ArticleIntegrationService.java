package com.sakrafux.realworld.article;

import java.util.Optional;

public interface ArticleIntegrationService {
    Optional<Long> findArticleIdBySlug(String slug);
}
