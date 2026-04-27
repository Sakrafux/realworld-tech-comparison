package com.sakrafux.realworld.article.infrastructure.persistence.adapter;

import com.sakrafux.realworld.article.application.port.out.CommentRepository;
import com.sakrafux.realworld.article.domain.Comment;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.CommentEntity;
import com.sakrafux.realworld.user.application.port.api.UserInternalApi;
import com.sakrafux.realworld.article.infrastructure.persistence.mapper.CommentPersistenceMapper;
import com.sakrafux.realworld.article.infrastructure.persistence.repository.ArticleJpaRepository;
import com.sakrafux.realworld.article.infrastructure.persistence.repository.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;
    private final ArticleJpaRepository articleJpaRepository;
    private final UserInternalApi userInternalApi;
    private final CommentPersistenceMapper commentMapper;

    @Override
    public Comment save(Comment comment, String articleSlug) {
        ArticleEntity article = articleJpaRepository.findBySlug(articleSlug)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        Long authorId = userInternalApi.getUserByUsername(comment.getAuthor().getUsername())
                .map(com.sakrafux.realworld.user.domain.User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CommentEntity entity = commentMapper.toEntity(comment);
        entity.setArticle(article);
        entity.setAuthorId(authorId);

        entity = commentJpaRepository.save(entity);
        return hydrate(entity);
    }

    @Override
    public List<Comment> findByArticleSlug(String slug) {
        ArticleEntity article = articleJpaRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        return commentJpaRepository.findByArticleOrderByCreatedAtDesc(article).stream()
                .map(this::hydrate)
                .toList();
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentJpaRepository.findById(id).map(this::hydrate);
    }

    @Override
    public void delete(Long id) {
        commentJpaRepository.deleteById(id);
    }

    private Comment hydrate(CommentEntity entity) {
        Comment comment = commentMapper.toDomain(entity);
        userInternalApi.getUserById(entity.getAuthorId())
                .ifPresent(user -> comment.getAuthor().setUsername(user.getUsername()));
        return comment;
    }
}
