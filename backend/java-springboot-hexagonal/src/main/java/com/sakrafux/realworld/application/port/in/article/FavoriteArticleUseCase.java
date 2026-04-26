package com.sakrafux.realworld.application.port.in.article;

import com.sakrafux.realworld.domain.model.Article;

public interface FavoriteArticleUseCase {
    Article favoriteArticle(String slug, String userEmail);
}
