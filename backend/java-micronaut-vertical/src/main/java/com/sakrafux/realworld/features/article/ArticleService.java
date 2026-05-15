package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.core.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
import com.sakrafux.realworld.features.article.dto.ArticleResponse;
import com.sakrafux.realworld.features.article.dto.MultipleArticlesResponse;
import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.article.dto.UpdateArticleRequest;
import com.sakrafux.realworld.features.user.UserService;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.retry.annotation.Retryable;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final ArticleMapper articleMapper;
    private final UserService userService;

    @Transactional
    public MultipleArticlesResponse getArticles(String tag, String author, String favorited, int limit, int offset, Optional<String> currentEmail) {
        PredicateSpecification<ArticleEntity> spec = (root, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tag != null) {
                predicates.add(criteriaBuilder.isMember(tag, root.get("tags")));
            }
            if (author != null) {
                Optional<Long> authorId = userService.findUserIdByUsername(author);
                if (authorId.isPresent()) {
                    predicates.add(criteriaBuilder.equal(root.get("authorId"), authorId.get()));
                } else {
                    predicates.add(criteriaBuilder.disjunction());
                }
            }
            if (favorited != null) {
                Optional<Long> favoritedUserId = userService.findUserIdByUsername(favorited);
                if (favoritedUserId.isPresent()) {
                    predicates.add(criteriaBuilder.isMember(favoritedUserId.get(), root.get("favoritedBy")));
                } else {
                    predicates.add(criteriaBuilder.disjunction());
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = Pageable.from(offset / limit, limit);
        Page<ArticleEntity> articlePage = articleRepository.findAll(spec, pageable);

        List<ArticleResponse.ArticleData> articles = articlePage.getContent().stream()
                .map(article -> mapToArticleData(article, currentEmail))
                .collect(Collectors.toList());

        return articleMapper.toMultipleResponse(articles, (int) articlePage.getTotalSize());
    }

    @Transactional
    public MultipleArticlesResponse getFeed(int limit, int offset, String currentEmail) {
        Collection<Long> followingIds = userService.findFollowingIdsByEmail(currentEmail);
        if (followingIds.isEmpty()) {
            return articleMapper.toMultipleResponse(Collections.emptyList(), 0);
        }

        Pageable pageable = Pageable.from(offset / limit, limit);
        Page<ArticleEntity> articlePage = articleRepository.findByAuthorIdIn(followingIds, pageable);

        List<ArticleResponse.ArticleData> articles = articlePage.getContent().stream()
                .map(article -> mapToArticleData(article, Optional.of(currentEmail)))
                .collect(Collectors.toList());

        return articleMapper.toMultipleResponse(articles, (int) articlePage.getTotalSize());
    }

    private ArticleResponse.ArticleData mapToArticleData(ArticleEntity article, Optional<String> currentEmail) {
        List<String> tagList = getTagList(article);
        Optional<Long> currentUserId = currentEmail.flatMap(userService::findUserIdByEmail);
        boolean favorited = currentUserId
                .map(userId -> article.getFavoritedBy().contains(userId))
                .orElse(false);
        int favoritesCount = article.getFavoritedBy().size();

        var authorProfile = userService.getProfileById(article.getAuthorId(), currentEmail).getProfile();

        return articleMapper.toArticleData(article, tagList, favorited, favoritesCount, authorProfile);
    }

    @Retryable(attempts = "3", delay = "50ms", multiplier = "2.0")
    @Transactional
    public ArticleResponse createArticle(NewArticleRequest request, String currentEmail) {
        var authorProfile = userService.getProfileByEmail(currentEmail, Optional.of(currentEmail)).getProfile();
        Long authorId = userService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        var articleData = request.getArticle();

        if (articleRepository.findByTitle(articleData.getTitle()).isPresent()) {
            throw new ResourceAlreadyExistsException("Title already exists");
        }

        String slug = toSlug(articleData.getTitle());
        if (articleRepository.findBySlug(slug).isPresent()) {
            throw new ResourceAlreadyExistsException("Slug already exists");
        }

        ArticleEntity article = ArticleEntity.builder()
                .title(articleData.getTitle())
                .slug(slug)
                .description(articleData.getDescription())
                .body(articleData.getBody())
                .authorId(authorId)
                .build();

        if (articleData.getTagList() != null && !articleData.getTagList().isEmpty()) {
            persistTags(articleData.getTagList());
            article.setTags(new HashSet<>(articleData.getTagList()));
        }

        article = articleRepository.save(article);
        
        return articleMapper.toResponse(article, getTagList(article), false, 0, authorProfile);
    }

    @Transactional
    public ArticleResponse getArticle(String slug, Optional<String> currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Long currentUserId = currentEmail.flatMap(userService::findUserIdByEmail).orElse(null);
        boolean favorited = currentUserId != null && article.getFavoritedBy().contains(currentUserId);
        
        var authorProfile = userService.getProfileById(article.getAuthorId(), currentEmail).getProfile();

        return articleMapper.toResponse(article, getTagList(article), favorited, article.getFavoritedBy().size(), authorProfile);
    }

    @Transactional
    public ArticleResponse updateArticle(String slug, UpdateArticleRequest request, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Long currentUserId = userService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!article.getAuthorId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not the author of this article");
        }

        var articleData = request.getArticle();

        if (articleData.getTitle() != null && !articleData.getTitle().equals(article.getTitle())) {
            if (articleRepository.findByTitle(articleData.getTitle()).isPresent()) {
                throw new ResourceAlreadyExistsException("Title already exists");
            }
            String newSlug = toSlug(articleData.getTitle());
            if (articleRepository.findBySlug(newSlug).isPresent()) {
                throw new ResourceAlreadyExistsException("Slug already exists");
            }
            article.setTitle(articleData.getTitle());
            article.setSlug(newSlug);
        }

        if (articleData.getDescription() != null) {
            article.setDescription(articleData.getDescription());
        }

        if (articleData.getBody() != null) {
            article.setBody(articleData.getBody());
        }
        
        article = articleRepository.update(article);

        var authorProfile = userService.getProfileById(article.getAuthorId(), Optional.of(currentEmail)).getProfile();
        return articleMapper.toResponse(article, getTagList(article), article.getFavoritedBy().contains(currentUserId), article.getFavoritedBy().size(), authorProfile);
    }

    @Transactional
    public void deleteArticle(String slug, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Long currentUserId = userService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!article.getAuthorId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not the author of this article");
        }

        articleRepository.delete(article);
    }

    @Transactional
    public ArticleResponse favoriteArticle(String slug, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Long currentUserId = userService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (article.getFavoritedBy().add(currentUserId)) {
            article = articleRepository.update(article);
        }

        var authorProfile = userService.getProfileById(article.getAuthorId(), Optional.of(currentEmail)).getProfile();

        return articleMapper.toResponse(article, getTagList(article), true, article.getFavoritedBy().size(), authorProfile);
    }

    @Transactional
    public ArticleResponse unfavoriteArticle(String slug, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Long currentUserId = userService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (article.getFavoritedBy().remove(currentUserId)) {
            article = articleRepository.update(article);
        }

        var authorProfile = userService.getProfileById(article.getAuthorId(), Optional.of(currentEmail)).getProfile();

        return articleMapper.toResponse(article, getTagList(article), false, article.getFavoritedBy().size(), authorProfile);
    }

    private void persistTags(List<String> tags) {
        Set<String> uniqueTags = new HashSet<>(tags);
        
        Set<String> existingTags = tagRepository.findByTagIn(uniqueTags).stream()
                .map(TagEntity::getTag)
                .collect(Collectors.toSet());

        List<TagEntity> missingTags = uniqueTags.stream()
                .filter(tagName -> !existingTags.contains(tagName))
                .map(tagName -> TagEntity.builder().tag(tagName).build())
                .collect(Collectors.toList());

        if (!missingTags.isEmpty()) {
            tagRepository.saveAll(missingTags);
        }
    }

    private List<String> getTagList(ArticleEntity article) {
        return article.getTags().stream().sorted().collect(Collectors.toList());
    }

    private String toSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
