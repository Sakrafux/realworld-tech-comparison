package com.sakrafux.realworld.article.application.port.in;

import com.sakrafux.realworld.article.domain.Article;

public interface FavoriteArticleUseCase {
    Article favoriteArticle(String slug, String userEmail);
}
