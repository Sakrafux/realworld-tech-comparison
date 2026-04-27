package com.sakrafux.realworld.article.infrastructure.persistence.adapter;

import com.sakrafux.realworld.article.application.port.in.GetArticlesQuery.GetArticlesFilter;
import com.sakrafux.realworld.article.application.port.out.ArticleRepository;
import com.sakrafux.realworld.user.application.port.api.UserInternalApi;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.article.domain.Article;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.TagEntity;
import com.sakrafux.realworld.article.infrastructure.persistence.mapper.ArticlePersistenceMapper;
import com.sakrafux.realworld.article.infrastructure.persistence.repository.ArticleJpaRepository;
import com.sakrafux.realworld.article.infrastructure.persistence.repository.TagJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ArticlePersistenceAdapter implements ArticleRepository {

    private final ArticleJpaRepository articleJpaRepository;
    private final UserInternalApi userInternalApi;
    private final TagJpaRepository tagJpaRepository;
    private final ArticlePersistenceMapper articleMapper;

    @Override
    public Article save(Article article) {
        ArticleEntity entity;
        if (article.getId() != null) {
            entity = articleJpaRepository.findById(article.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Article", "id", article.getId()));
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
        ArticleEntity finalEntity = entity;
        if (article.getAuthor() != null) {
            userInternalApi.getUserByUsername(article.getAuthor().getUsername())
                    .ifPresent(user -> finalEntity.setAuthorId(user.getId()));
        }

        entity = articleJpaRepository.save(finalEntity);
        return hydrate(entity);
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return articleJpaRepository.findBySlug(slug).map(this::hydrate);
    }

    @Override
    public Optional<Article> findByTitle(String title) {
        return articleJpaRepository.findByTitle(title).map(this::hydrate);
    }

    @Override
    public void delete(String slug) {
        articleJpaRepository.findBySlug(slug).ifPresent(articleJpaRepository::delete);
    }

    @Override
    public void favorite(Long userId, Long articleId) {
        articleJpaRepository.findById(articleId).ifPresent(article -> {
            article.getFavoritedByUserIds().add(userId);
            articleJpaRepository.save(article);
        });
    }

    @Override
    public void unfavorite(Long userId, Long articleId) {
        articleJpaRepository.findById(articleId).ifPresent(article -> {
            article.getFavoritedByUserIds().remove(userId);
            articleJpaRepository.save(article);
        });
    }

    @Override
    public boolean isFavorited(Long userId, Long articleId) {
        return articleJpaRepository.isFavorited(userId, articleId);
    }

    @Override
    public List<Article> findFiltered(GetArticlesFilter filter) {
        PageRequest pageRequest = PageRequest.of(filter.offset() / filter.limit(), filter.limit(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return articleJpaRepository.findAll(createSpecification(filter), pageRequest).getContent().stream()
                .map(this::hydrate)
                .toList();
    }

    @Override
    public long countFiltered(GetArticlesFilter filter) {
        return articleJpaRepository.count(createSpecification(filter));
    }

    @Override
    public List<Article> findFeed(String observerEmail, int limit, int offset) {
        Long observerId = userInternalApi.getUserByEmail(observerEmail)
                .map(com.sakrafux.realworld.user.domain.User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", observerEmail));
        
        // This is tricky because we need the IDs of people the observer follows.
        // We'll need another InternalApi method for that or use IDs.
        // Let's assume we can get followed IDs.
        Set<Long> followingIds = userInternalApi.getUserByEmail(observerEmail)
                .map(user -> userInternalApi.getFollowingIds(user.getId())) // Need to add this
                .orElse(Set.of());

        if (followingIds.isEmpty()) return List.of();

        PageRequest pageRequest = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return articleJpaRepository.findByAuthorIdIn(followingIds, pageRequest).getContent().stream()
                .map(this::hydrate)
                .toList();
    }

    @Override
    public long countFeed(String observerEmail) {
        Set<Long> followingIds = userInternalApi.getUserByEmail(observerEmail)
                .map(user -> userInternalApi.getFollowingIds(user.getId()))
                .orElse(Set.of());

        if (followingIds.isEmpty()) return 0;

        return articleJpaRepository.count((root, query, cb) -> root.get("authorId").in(followingIds));
    }

    private Article hydrate(ArticleEntity entity) {
        Article article = articleMapper.toDomain(entity);
        // Hydrate author username
        userInternalApi.getUserById(entity.getAuthorId())
                .ifPresent(user -> article.getAuthor().setUsername(user.getUsername()));
        return article;
    }

    private Specification<ArticleEntity> createSpecification(GetArticlesFilter filter) {
        return (root, query, cb) -> {
            Specification<ArticleEntity> spec = Specification.where((r, q, c) -> c.conjunction());
            if (filter.tag() != null) {
                spec = spec.and((r, q, c) -> c.equal(r.join("tags").get("tag"), filter.tag()));
            }
            if (filter.author() != null) {
                Long authorId = userInternalApi.getUserByUsername(filter.author())
                        .map(com.sakrafux.realworld.user.domain.User::getId)
                        .orElse(-1L);
                spec = spec.and((r, q, c) -> c.equal(r.get("authorId"), authorId));
            }
            if (filter.favorited() != null) {
                Long favoritedById = userInternalApi.getUserByUsername(filter.favorited())
                        .map(com.sakrafux.realworld.user.domain.User::getId)
                        .orElse(-1L);
                spec = spec.and((r, q, c) -> c.isMember(favoritedById, r.get("favoritedByUserIds")));
            }
            return spec.toPredicate(root, query, cb);
        };
    }
}
