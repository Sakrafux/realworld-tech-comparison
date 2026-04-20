package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.request.NewArticleRequest;
import com.sakrafux.realworld.dto.request.UpdateArticleRequest;
import com.sakrafux.realworld.dto.response.ArticleResponse;
import com.sakrafux.realworld.dto.response.MultipleArticlesResponse;
import com.sakrafux.realworld.dto.response.ProfileResponse;
import com.sakrafux.realworld.entity.ArticleEntity;
import com.sakrafux.realworld.entity.TagEntity;
import com.sakrafux.realworld.entity.UserEntity;
import com.sakrafux.realworld.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.exception.ResourceNotFoundException;
import com.sakrafux.realworld.exception.UnauthorizedException;
import com.sakrafux.realworld.mapper.ArticleMapper;
import com.sakrafux.realworld.repository.ArticleRepository;
import com.sakrafux.realworld.repository.TagRepository;
import com.sakrafux.realworld.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;
    private final ProfileService profileService;

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
            spec = spec.and((root, query, cb) -> cb.equal(root.join("tags").get("tag"), tag));
        }
        if (author != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("author").get("username"), author));
        }
        if (favorited != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("favoritedBy").get("username"), favorited));
        }

        PageRequest pageRequest = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        // Repository uses EntityGraph to fetch relationships in a single query (fixes N+1)
        Page<ArticleEntity> articlePage = articleRepository.findAll(spec, pageRequest);

        // Instead of fetching the current user inside the loop for every single article,
        // we fetch the user exactly once here and pass the UserEntity down to the mapper.
        Optional<UserEntity> currentUser = currentEmail.flatMap(userRepository::findByEmail);

        List<ArticleResponse.ArticleData> articles = articlePage.getContent().stream()
                .map(article -> mapToArticleData(article, currentUser))
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
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        Set<UserEntity> following = user.getFollowing();
        if (following.isEmpty()) {
            return articleMapper.toMultipleResponse(Collections.emptyList(), 0);
        }

        PageRequest pageRequest = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        // Repository uses EntityGraph to fetch relationships in a single query (fixes N+1)
        Page<ArticleEntity> articlePage = articleRepository.findByAuthorIn(following, pageRequest);

        List<ArticleResponse.ArticleData> articles = articlePage.getContent().stream()
                .map(article -> mapToArticleData(article, Optional.of(user)))
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
    @Transactional
    public ArticleResponse createArticle(NewArticleRequest request, String currentEmail) {
        UserEntity author = userRepository.findByEmail(currentEmail)
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
                .author(author)
                .build();

        if (articleData.getTagList() != null && !articleData.getTagList().isEmpty()) {
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
                List<TagEntity> newlySavedTags = tagRepository.saveAll(missingTags);
                existingTags.addAll(newlySavedTags);
            }

            article.setTags(existingTags);
        }

        article = articleRepository.save(article);
        return articleMapper.toResponse(article, getTagList(article), false, 0, 
                profileService.getProfile(author.getUsername(), Optional.of(currentEmail)).getProfile());
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

        Optional<UserEntity> currentUser = currentEmail.flatMap(userRepository::findByEmail);
        return articleMapper.toResponse(article, getTagList(article), 
                currentUser.map(user -> article.getFavoritedBy().contains(user)).orElse(false),
                article.getFavoritedBy().size(),
                profileService.getProfile(article.getAuthor().getUsername(), currentEmail).getProfile());
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

        if (!article.getAuthor().getEmail().equals(currentEmail)) {
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
                article.getFavoritedBy().stream().anyMatch(u -> u.getEmail().equals(currentEmail)),
                article.getFavoritedBy().size(),
                profileService.getProfile(article.getAuthor().getUsername(), Optional.of(currentEmail)).getProfile());
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

        if (!article.getAuthor().getEmail().equals(currentEmail)) {
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
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        article.getFavoritedBy().add(user);
        article = articleRepository.save(article);

        return articleMapper.toResponse(article, getTagList(article), true, article.getFavoritedBy().size(),
                profileService.getProfile(article.getAuthor().getUsername(), Optional.of(currentEmail)).getProfile());
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
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        article.getFavoritedBy().remove(user);
        article = articleRepository.save(article);

        return articleMapper.toResponse(article, getTagList(article), false, article.getFavoritedBy().size(),
                profileService.getProfile(article.getAuthor().getUsername(), Optional.of(currentEmail)).getProfile());
    }

    private ArticleResponse.ArticleData mapToArticleData(ArticleEntity article, Optional<UserEntity> currentUser) {
        List<String> tagList = getTagList(article);
        boolean favorited = currentUser
                .map(user -> article.getFavoritedBy().contains(user))
                .orElse(false);
        int favoritesCount = article.getFavoritedBy().size();

        ProfileResponse.ProfileData authorProfile = profileService.getProfile(
                article.getAuthor().getUsername(),
                currentUser.map(UserEntity::getEmail)
        ).getProfile();

        return articleMapper.toArticleData(article, tagList, favorited, favoritesCount, authorProfile);
    }

    private List<String> getTagList(ArticleEntity article) {
        return article.getTags().stream()
                .map(TagEntity::getTag)
                .sorted()
                .toList();
    }

    private String toSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
