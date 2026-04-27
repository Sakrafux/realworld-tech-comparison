package com.sakrafux.realworld.article.infrastructure.persistence.repository;

import com.sakrafux.realworld.article.infrastructure.persistence.entity.ArticleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ArticleJpaRepository extends JpaRepository<ArticleEntity, Long>, JpaSpecificationExecutor<ArticleEntity> {
    Optional<ArticleEntity> findBySlug(String slug);
    Optional<ArticleEntity> findByTitle(String title);
    
    Page<ArticleEntity> findByAuthorIdIn(Collection<Long> authorIds, Pageable pageable);

    @Query("SELECT COUNT(a) > 0 FROM ArticleEntity a WHERE a.id = :articleId AND :userId MEMBER OF a.favoritedByUserIds")
    boolean isFavorited(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
