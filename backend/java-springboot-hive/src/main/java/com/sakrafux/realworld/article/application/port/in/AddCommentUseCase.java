package com.sakrafux.realworld.article.application.port.in;

import com.sakrafux.realworld.article.domain.Comment;

public interface AddCommentUseCase {
    Comment addComment(String slug, String body, String authorEmail);
}
