package com.sakrafux.realworld.application.port.out;

import com.sakrafux.realworld.domain.model.Article;

import java.util.Optional;

public interface ArticleRepository {
    Article save(Article article);
    Optional<Article> findBySlug(String slug);
    Optional<Article> findByTitle(String title);
    void delete(String slug);
    boolean isFavorited(Long userId, Long articleId);
}
