package com.sakrafux.realworld.application.port.in.comment;

import com.sakrafux.realworld.domain.model.Comment;

public interface AddCommentUseCase {
    Comment addComment(String slug, String body, String authorEmail);
}
