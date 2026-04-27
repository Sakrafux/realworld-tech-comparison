package com.sakrafux.realworld.application.port.in.article;

public interface DeleteArticleUseCase {
    void deleteArticle(String slug, String authorEmail);
}
