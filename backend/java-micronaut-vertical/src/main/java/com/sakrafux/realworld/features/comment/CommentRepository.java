package com.sakrafux.realworld.features.comment;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<CommentEntity, Long> {
    List<CommentEntity> findByArticleIdOrderByCreatedAtDesc(Long articleId);
}
