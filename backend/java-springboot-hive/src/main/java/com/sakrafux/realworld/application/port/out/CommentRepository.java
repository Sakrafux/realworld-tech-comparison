package com.sakrafux.realworld.application.port.out;

import com.sakrafux.realworld.domain.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment, String articleSlug);
    List<Comment> findByArticleSlug(String slug);
    Optional<Comment> findById(Long id);
    void delete(Long id);
}
