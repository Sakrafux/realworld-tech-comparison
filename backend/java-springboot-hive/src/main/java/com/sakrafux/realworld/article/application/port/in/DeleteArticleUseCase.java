package com.sakrafux.realworld.article.application.port.in;

public interface DeleteArticleUseCase {
    void deleteArticle(String slug, String authorEmail);
}
