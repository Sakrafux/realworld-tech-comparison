package com.sakrafux.realworld.application.port.out;

import com.sakrafux.realworld.application.port.in.article.GetArticlesQuery.GetArticlesFilter;
import com.sakrafux.realworld.domain.model.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    Article save(Article article);
    Optional<Article> findBySlug(String slug);
    Optional<Article> findByTitle(String title);
    void delete(String slug);
    boolean isFavorited(Long userId, Long articleId);

    List<Article> findFiltered(GetArticlesFilter filter);
    long countFiltered(GetArticlesFilter filter);

    List<Article> findFeed(String observerEmail, int limit, int offset);
    long countFeed(String observerEmail);
}
