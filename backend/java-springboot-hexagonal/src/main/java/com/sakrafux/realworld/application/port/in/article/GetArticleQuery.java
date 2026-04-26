package com.sakrafux.realworld.application.port.in.article;

import com.sakrafux.realworld.domain.model.Article;

import java.util.Optional;

public interface GetArticleQuery {
    Article getArticle(String slug, Optional<String> observerEmail);
}
