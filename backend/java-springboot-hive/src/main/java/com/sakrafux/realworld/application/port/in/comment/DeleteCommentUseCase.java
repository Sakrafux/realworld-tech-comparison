package com.sakrafux.realworld.application.port.in.comment;

public interface DeleteCommentUseCase {
    void deleteComment(String slug, Long id, String authorEmail);
}
