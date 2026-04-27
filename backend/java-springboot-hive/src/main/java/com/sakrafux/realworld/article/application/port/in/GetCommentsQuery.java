package com.sakrafux.realworld.article.application.port.in;

import com.sakrafux.realworld.article.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface GetCommentsQuery {
    List<Comment> getComments(String slug, Optional<String> observerEmail);
}
