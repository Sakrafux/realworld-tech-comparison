package com.sakrafux.realworld.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, Long>, JpaSpecificationExecutor<ArticleEntity> {
    Optional<ArticleEntity> findBySlug(String slug);
    Optional<ArticleEntity> findByTitle(String title);
    Page<ArticleEntity> findByAuthorIdIn(Collection<Long> authorIds, Pageable pageable);
}
