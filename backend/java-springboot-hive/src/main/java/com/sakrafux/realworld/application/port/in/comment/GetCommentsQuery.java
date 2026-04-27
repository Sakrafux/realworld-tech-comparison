package com.sakrafux.realworld.application.port.in.comment;

import com.sakrafux.realworld.domain.model.Comment;

import java.util.List;
import java.util.Optional;

public interface GetCommentsQuery {
    List<Comment> getComments(String slug, Optional<String> observerEmail);
}
