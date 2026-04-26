package com.sakrafux.realworld.article;

import com.sakrafux.realworld.article.request.NewArticleRequest;
import com.sakrafux.realworld.article.request.UpdateArticleRequest;
import com.sakrafux.realworld.article.response.ArticleResponse;
import com.sakrafux.realworld.article.response.MultipleArticlesResponse;
import com.sakrafux.realworld.profile.ProfileService;
import com.sakrafux.realworld.profile.response.ProfileResponse;
import com.sakrafux.realworld.tag.TagEntity;
import com.sakrafux.realworld.core.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
import com.sakrafux.realworld.tag.TagRepository;
import com.sakrafux.realworld.user.UserIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing articles.
 * Coordinates between ArticleRepository, TagRepository, and ProfileService.
 */
@Service
@RequiredArgsConstructor
public class ArticleService implements ArticleIntegrationService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final ArticleMapper articleMapper;
    private final ProfileService profileService;
    private final UserIntegrationService userIntegrationService;

    /**
     * Retrieves a list of articles based on filtering criteria.
     *
     * @param tag          filter by tag
     * @param author       filter by author username
     * @param favorited    filter by username who favorited the article
     * @param limit        limit the number of results
     * @param offset       offset for pagination
     * @param currentEmail current authenticated user email
     * @return MultipleArticlesResponse containing the list of articles and total count
     */
    @Transactional(readOnly = true)
    public MultipleArticlesResponse getArticles(String tag, String author, String favorited, int limit, int offset, Optional<String> currentEmail) {
        // Using cb.conjunction() creates an empty "AND" clause (always true).
        // This is safer than passing `null` to where(), which can trigger NullPointer exceptions
        // or strict static analysis warnings in newer Spring Boot versions.
        Specification<ArticleEntity> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (tag != null) {
            spec = spec.and((root, query, cb) -> cb.isMember(tag, root.get("tags")));
        }
        if (author != null) {
            Optional<Long> authorId = userIntegrationService.findUserIdByUsername(author);
            if (authorId.isPresent()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("authorId"), authorId.get()));
            } else {
                spec = spec.and((root, query, cb) -> cb.disjunction());
            }
        }
        if (favorited != null) {
            Optional<Long> favoritedUserId = userIntegrationService.findUserIdByUsername(favorited);
            if (favoritedUserId.isPresent()) {
                spec = spec.and((root, query, cb) -> cb.isMember(favoritedUserId.get(), root.get("favoritedBy")));
            } else {
                spec = spec.and((root, query, cb) -> cb.disjunction());
            }
        }

        PageRequest pageRequest = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ArticleEntity> articlePage = articleRepository.findAll(spec, pageRequest);

        // Instead of fetching the current user inside the loop for every single article,
        // we fetch the user exactly once here and pass the UserEntity down to the mapper.
        Optional<Long> currentUserId = currentEmail.flatMap(userIntegrationService::findUserIdByEmail);

        List<ArticleResponse.ArticleData> articles = articlePage.getContent().stream()
                .map(article -> mapToArticleData(article, currentUserId))
                .toList();

        return articleMapper.toMultipleResponse(articles, (int) articlePage.getTotalElements());
    }

    /**
     * Retrieves the article feed for the current user.
     * The feed contains articles from authors that the current user follows.
     *
     * @param limit        limit the number of results
     * @param offset       offset for pagination
     * @param currentEmail email of the authenticated user
     * @return MultipleArticlesResponse containing the feed articles and total count
     */
    @Transactional(readOnly = true)
    public MultipleArticlesResponse getFeed(int limit, int offset, String currentEmail) {
        Collection<Long> followingIds = userIntegrationService.findFollowingIdsByEmail(currentEmail);
        if (followingIds.isEmpty()) {
            return articleMapper.toMultipleResponse(Collections.emptyList(), 0);
        }

        PageRequest pageRequest = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ArticleEntity> articlePage = articleRepository.findByAuthorIdIn(followingIds, pageRequest);

        Long userId = userIntegrationService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        List<ArticleResponse.ArticleData> articles = articlePage.getContent().stream()
                .map(article -> mapToArticleData(article, Optional.of(userId)))
                .toList();

        return articleMapper.toMultipleResponse(articles, (int) articlePage.getTotalElements());
    }

    /**
     * Creates a new article.
     *
     * @param request      the details of the new article
     * @param currentEmail email of the authenticated author
     * @return ArticleResponse containing the created article details
     */
    // Retryable is necessary to deal with concurrent creation of the same tags that
    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            backoff = @Backoff(delay = 50, multiplier = 2.0)
    )
    @Transactional
    public ArticleResponse createArticle(NewArticleRequest request, String currentEmail) {
        Long authorId = userIntegrationService.findUserIdByEmail(currentEmail)
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

        persistTags(articleData, article);

        article = articleRepository.save(article);
        return articleMapper.toResponse(article, getTagList(article), false, 0, 
                profileService.getProfile(authorId, Optional.of(authorId)).getProfile());
    }

    private void persistTags(NewArticleRequest.ArticleData articleData, ArticleEntity article) {
        if (articleData.getTagList() == null || articleData.getTagList().isEmpty()) {
            return;
        }

        Set<String> tagNames = new HashSet<>(articleData.getTagList());

        // Bulk fetch all existing tags at once to avoid N+1 problem during article creation
        Set<TagEntity> existingTags = tagRepository.findByTagIn(tagNames);

        Set<String> existingTagNames = existingTags.stream()
                .map(TagEntity::getTag)
                .collect(Collectors.toSet());

        // Determine which tags are missing from the database
        List<TagEntity> missingTags = tagNames.stream()
                .filter(tagName -> !existingTagNames.contains(tagName))
                .map(tagName -> TagEntity.builder().tag(tagName).build())
                .toList();

        // Bulk insert any new tags that don't exist yet
        if (!missingTags.isEmpty()) {
            // NOTE: Under high concurrency, this may lead to DataIntegrityViolationException due to the insertion
            // of the same tags. This will cause a retry of the transaction.
            List<TagEntity> newlySavedTags = tagRepository.saveAll(missingTags);
            existingTags.addAll(newlySavedTags);
        }

        article.setTags(existingTags.stream().map(TagEntity::getTag).collect(Collectors.toSet()));
    }

    /**
     * Retrieves a single article by its slug.
     *
     * @param slug         the article slug
     * @param currentEmail optional email of the authenticated user
     * @return ArticleResponse containing the article details
     */
    @Transactional(readOnly = true)
    public ArticleResponse getArticle(String slug, Optional<String> currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Optional<Long> currentUserId = currentEmail.flatMap(userIntegrationService::findUserIdByEmail);
        return articleMapper.toResponse(article, getTagList(article),
                currentUserId.map(userId -> article.getFavoritedBy().contains(userId)).orElse(false),
                article.getFavoritedBy().size(),
                profileService.getProfile(article.getAuthorId(), currentUserId).getProfile());
    }

    /**
     * Updates an existing article.
     *
     * @param slug         the slug of the article to update
     * @param request      the update details
     * @param currentEmail email of the authenticated author
     * @return ArticleResponse containing the updated article details
     */
    @Transactional
    public ArticleResponse updateArticle(String slug, UpdateArticleRequest request, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Long currentUserId = userIntegrationService.findUserIdByEmail(currentEmail)
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

        article = articleRepository.save(article);
        return articleMapper.toResponse(article, getTagList(article),
                article.getFavoritedBy().contains(currentUserId),
                article.getFavoritedBy().size(),
                profileService.getProfile(article.getAuthorId(), Optional.of(currentUserId)).getProfile());
    }

    /**
     * Deletes an article by its slug.
     *
     * @param slug         the article slug
     * @param currentEmail email of the authenticated author
     */
    @Transactional
    public void deleteArticle(String slug, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Long currentUserId = userIntegrationService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!article.getAuthorId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not the author of this article");
        }

        articleRepository.delete(article);
    }

    /**
     * Favorites an article for the current user.
     *
     * @param slug         the slug of the article to favorite
     * @param currentEmail the email of the authenticated user
     * @return ArticleResponse containing the updated article details
     */
    @Transactional
    public ArticleResponse favoriteArticle(String slug, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        Long userId = userIntegrationService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        article.getFavoritedBy().add(userId);
        article = articleRepository.save(article);

        return articleMapper.toResponse(article, getTagList(article), true, article.getFavoritedBy().size(),
                profileService.getProfile(article.getAuthorId(), Optional.of(userId)).getProfile());
    }

    /**
     * Unfavorites an article for the current user.
     *
     * @param slug         the slug of the article to unfavorite
     * @param currentEmail the email of the authenticated user
     * @return ArticleResponse containing the updated article details
     */
    @Transactional
    public ArticleResponse unfavoriteArticle(String slug, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        Long userId = userIntegrationService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        article.getFavoritedBy().remove(userId);
        article = articleRepository.save(article);

        return articleMapper.toResponse(article, getTagList(article), false, article.getFavoritedBy().size(),
                profileService.getProfile(article.getAuthorId(), Optional.of(userId)).getProfile());
    }

    @Override
    public Optional<Long> findArticleIdBySlug(String slug) {
        return articleRepository.findBySlug(slug).map(ArticleEntity::getId);
    }

    private ArticleResponse.ArticleData mapToArticleData(ArticleEntity article, Optional<Long> currentUserId) {
        List<String> tagList = getTagList(article);
        boolean favorited = currentUserId
                .map(userId -> article.getFavoritedBy().contains(userId))
                .orElse(false);
        int favoritesCount = article.getFavoritedBy().size();

        ProfileResponse.ProfileData authorProfile = profileService.getProfile(
                article.getAuthorId(),
                currentUserId
        ).getProfile();

        return articleMapper.toArticleData(article, tagList, favorited, favoritesCount, authorProfile);
    }

    private List<String> getTagList(ArticleEntity article) {
        return article.getTags().stream()
                .sorted()
                .toList();
    }

    private String toSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}