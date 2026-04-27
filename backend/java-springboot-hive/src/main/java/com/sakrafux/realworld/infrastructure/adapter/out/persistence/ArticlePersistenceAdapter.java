package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.application.port.in.article.GetArticlesQuery.GetArticlesFilter;
import com.sakrafux.realworld.application.port.out.ArticleRepository;
import com.sakrafux.realworld.domain.exception.ResourceNotFoundException;
import com.sakrafux.realworld.domain.model.Article;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.ArticleEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.TagEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper.ArticlePersistenceMapper;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.ArticleJpaRepository;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.TagJpaRepository;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
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
    private final UserJpaRepository userJpaRepository;
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
    public void favorite(Long userId, Long articleId) {
        articleJpaRepository.findById(articleId).ifPresent(article -> {
            userJpaRepository.findById(userId).ifPresent(user -> {
                article.getFavoritedBy().add(user);
                articleJpaRepository.save(article);
            });
        });
    }

    @Override
    public void unfavorite(Long userId, Long articleId) {
        articleJpaRepository.findById(articleId).ifPresent(article -> {
            article.getFavoritedBy().removeIf(user -> user.getId().equals(userId));
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
                .map(articleMapper::toDomain)
                .toList();
    }

    @Override
    public long countFiltered(GetArticlesFilter filter) {
        return articleJpaRepository.count(createSpecification(filter));
    }

    @Override
    public List<Article> findFeed(String observerEmail, int limit, int offset) {
        UserEntity user = userJpaRepository.findByEmail(observerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", observerEmail));
        
        Set<UserEntity> following = user.getFollowing();
        if (following.isEmpty()) return List.of();

        PageRequest pageRequest = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return articleJpaRepository.findByAuthorIn(following, pageRequest).getContent().stream()
                .map(articleMapper::toDomain)
                .toList();
    }

    @Override
    public long countFeed(String observerEmail) {
        UserEntity user = userJpaRepository.findByEmail(observerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", observerEmail));
        
        Set<UserEntity> following = user.getFollowing();
        if (following.isEmpty()) return 0;

        return articleJpaRepository.count((root, query, cb) -> root.get("author").in(following));
    }

    private Specification<ArticleEntity> createSpecification(GetArticlesFilter filter) {
        return (root, query, cb) -> {
            Specification<ArticleEntity> spec = Specification.where((r, q, c) -> c.conjunction());
            if (filter.tag() != null) {
                spec = spec.and((r, q, c) -> c.equal(r.join("tags").get("tag"), filter.tag()));
            }
            if (filter.author() != null) {
                spec = spec.and((r, q, c) -> c.equal(r.join("author").get("username"), filter.author()));
            }
            if (filter.favorited() != null) {
                spec = spec.and((r, q, c) -> c.equal(r.join("favoritedBy").get("username"), filter.favorited()));
            }
            return spec.toPredicate(root, query, cb);
        };
    }
}
