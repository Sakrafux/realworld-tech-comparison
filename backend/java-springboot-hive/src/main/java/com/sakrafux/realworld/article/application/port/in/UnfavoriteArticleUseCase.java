package com.sakrafux.realworld.article.application.port.in;

import com.sakrafux.realworld.article.domain.Article;

public interface UnfavoriteArticleUseCase {
    Article unfavoriteArticle(String slug, String userEmail);
}
