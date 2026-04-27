package com.sakrafux.realworld.article.application.port.in;

import com.sakrafux.realworld.article.domain.Article;

import java.util.Optional;

public interface GetArticleQuery {
    Article getArticle(String slug, Optional<String> observerEmail);
}
