package com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository;

import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long>, JpaSpecificationExecutor<CommentEntity> {
    List<CommentEntity> findByArticleOrderByCreatedAtDesc(ArticleEntity article);
}
