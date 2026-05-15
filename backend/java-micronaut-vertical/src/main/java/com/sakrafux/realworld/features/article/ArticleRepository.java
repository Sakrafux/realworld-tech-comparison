package com.sakrafux.realworld.features.article;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.PageableRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ArticleRepository extends CrudRepository<ArticleEntity, Long>, PageableRepository<ArticleEntity, Long>, JpaSpecificationExecutor<ArticleEntity> {
    Optional<ArticleEntity> findBySlug(String slug);
    Optional<ArticleEntity> findByTitle(String title);
    Page<ArticleEntity> findByAuthorIdIn(Collection<Long> authorIds, Pageable pageable);
}
