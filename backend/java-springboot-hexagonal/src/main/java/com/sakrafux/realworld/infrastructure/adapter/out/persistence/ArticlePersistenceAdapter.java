package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.application.port.out.ArticleRepository;
import com.sakrafux.realworld.domain.model.Article;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.TagEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper.ArticlePersistenceMapper;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.ArticleJpaRepository;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.TagJpaRepository;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ArticlePersistenceAdapter implements ArticleRepository {

    private final ArticleJpaRepository articleJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final TagJpaRepository tagJpaRepository;
    private final ArticlePersistenceMapper articleMapper;

    @Override
    public Article save(Article article) {
        ArticleEntity entity;
        if (article.getId() != null) {
            entity = articleJpaRepository.findById(article.getId())
                    .orElseThrow(() -> new RuntimeException("Article not found for update"));
            articleMapper.updateEntityFromDomain(article, entity);
        } else {
            entity = articleMapper.toEntity(article);
        }

        // Handle tags
        if (article.getTagList() != null) {
            Set<TagEntity> tags = tagJpaRepository.findByTagIn(article.getTagList());
            entity.setTags(tags);
        }

        // Handle author
        if (article.getAuthor() != null) {
            userJpaRepository.findByUsername(article.getAuthor().getUsername())
                    .ifPresent(entity::setAuthor);
        }

        entity = articleJpaRepository.save(entity);
        return articleMapper.toDomain(entity);
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return articleJpaRepository.findBySlug(slug).map(articleMapper::toDomain);
    }

    @Override
    public Optional<Article> findByTitle(String title) {
        return articleJpaRepository.findByTitle(title).map(articleMapper::toDomain);
    }

    @Override
    public void delete(String slug) {
        articleJpaRepository.findBySlug(slug).ifPresent(articleJpaRepository::delete);
    }

    @Override
    public boolean isFavorited(Long userId, Long articleId) {
        return articleJpaRepository.isFavorited(userId, articleId);
    }
}
