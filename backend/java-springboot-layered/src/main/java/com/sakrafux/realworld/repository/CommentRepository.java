package com.sakrafux.realworld.repository;

import com.sakrafux.realworld.entity.ArticleEntity;
import com.sakrafux.realworld.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long>, JpaSpecificationExecutor<CommentEntity> {
    List<CommentEntity> findByArticleOrderByCreatedAtDesc(ArticleEntity article);
}
