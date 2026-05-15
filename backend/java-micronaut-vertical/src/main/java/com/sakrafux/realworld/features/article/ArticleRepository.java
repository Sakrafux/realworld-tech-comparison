package com.sakrafux.realworld.features.article;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends CrudRepository<ArticleEntity, Long> {
    Optional<ArticleEntity> findBySlug(String slug);
    Optional<ArticleEntity> findByTitle(String title);
}
