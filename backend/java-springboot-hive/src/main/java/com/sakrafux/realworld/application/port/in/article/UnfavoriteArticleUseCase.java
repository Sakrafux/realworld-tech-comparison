package com.sakrafux.realworld.application.port.in.article;

import com.sakrafux.realworld.domain.model.Article;

public interface UnfavoriteArticleUseCase {
    Article unfavoriteArticle(String slug, String userEmail);
}
