package com.sakrafux.realworld.article.application.port.in;

public interface DeleteCommentUseCase {
    void deleteComment(String slug, Long id, String authorEmail);
}
