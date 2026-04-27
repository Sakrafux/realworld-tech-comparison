package com.sakrafux.realworld.article.infrastructure.persistence.adapter;

import com.sakrafux.realworld.article.application.port.out.CommentRepository;
import com.sakrafux.realworld.article.domain.Comment;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.CommentEntity;
import com.sakrafux.realworld.user.infrastructure.persistence.entity.UserEntity;
import com.sakrafux.realworld.article.infrastructure.persistence.mapper.CommentPersistenceMapper;
import com.sakrafux.realworld.article.infrastructure.persistence.repository.ArticleJpaRepository;
import com.sakrafux.realworld.article.infrastructure.persistence.repository.CommentJpaRepository;
import com.sakrafux.realworld.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;
    private final ArticleJpaRepository articleJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CommentPersistenceMapper commentMapper;

    @Override
    public Comment save(Comment comment, String articleSlug) {
        ArticleEntity article = articleJpaRepository.findBySlug(articleSlug)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        UserEntity author = userJpaRepository.findByUsername(comment.getAuthor().getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CommentEntity entity = commentMapper.toEntity(comment);
        entity.setArticle(article);
        entity.setAuthor(author);

        entity = commentJpaRepository.save(entity);
        return commentMapper.toDomain(entity);
    }

    @Override
    public List<Comment> findByArticleSlug(String slug) {
        ArticleEntity article = articleJpaRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        return commentJpaRepository.findByArticleOrderByCreatedAtDesc(article).stream()
                .map(commentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentJpaRepository.findById(id).map(commentMapper::toDomain);
    }

    @Override
    public void delete(Long id) {
        commentJpaRepository.deleteById(id);
    }
}
