package com.sakrafux.realworld.article.application.port.out;

import com.sakrafux.realworld.article.application.port.in.GetArticlesQuery.GetArticlesFilter;
import com.sakrafux.realworld.article.domain.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    Article save(Article article);
    Optional<Article> findBySlug(String slug);
    Optional<Article> findByTitle(String title);
    void delete(String slug);
    
    void favorite(Long userId, Long articleId);
    void unfavorite(Long userId, Long articleId);
    boolean isFavorited(Long userId, Long articleId);

    List<Article> findFiltered(GetArticlesFilter filter);
    long countFiltered(GetArticlesFilter filter);

    List<Article> findFeed(String observerEmail, int limit, int offset);
    long countFeed(String observerEmail);
}
