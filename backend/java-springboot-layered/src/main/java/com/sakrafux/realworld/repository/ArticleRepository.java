package com.sakrafux.realworld.repository;

import com.sakrafux.realworld.entity.ArticleEntity;
import com.sakrafux.realworld.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, Long>, JpaSpecificationExecutor<ArticleEntity> {

    // Using EntityGraph to fetch author, tags, and favoritedBy in a single query to prevent N+1 problem
    @EntityGraph(attributePaths = {"author", "tags", "favoritedBy"})
    Optional<ArticleEntity> findBySlug(String slug);

    Optional<ArticleEntity> findByTitle(String title);

    // Using EntityGraph to fetch author, tags, and favoritedBy in a single query to prevent N+1 problem
    @EntityGraph(attributePaths = {"author", "tags", "favoritedBy"})
    Page<ArticleEntity> findByAuthorIn(Collection<UserEntity> authors, Pageable pageable);

    // Overriding findAll from JpaSpecificationExecutor to apply EntityGraph for N+1 prevention
    @Override
    @EntityGraph(attributePaths = {"author", "tags", "favoritedBy"})
    Page<ArticleEntity> findAll(Specification<ArticleEntity> spec, Pageable pageable);
}
