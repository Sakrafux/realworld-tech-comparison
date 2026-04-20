package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.request.NewArticleRequest;
import com.sakrafux.realworld.dto.response.ArticleResponse;
import com.sakrafux.realworld.dto.response.MultipleArticlesResponse;
import com.sakrafux.realworld.dto.response.ProfileResponse;
import com.sakrafux.realworld.entity.ArticleEntity;
import com.sakrafux.realworld.entity.TagEntity;
import com.sakrafux.realworld.entity.UserEntity;
import com.sakrafux.realworld.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.exception.ResourceNotFoundException;
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
            // No distinct needed here because "author" is a ManyToOne relationship.
            // Joining a single entity does not multiply the result rows.
            spec = spec.and((root, query, cb) -> cb.equal(root.join("author").get("username"), author));
        }
        if (favorited != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("favoritedBy").get("username"), favorited));
        }

        PageRequest pageRequest = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ArticleEntity> articlePage = articleRepository.findAll(spec, pageRequest);

        // Instead of fetching the current user inside the loop for every single article,
        // we fetch the user exactly once here and pass the UserEntity down to the mapper.
        Optional<UserEntity> currentUser = currentEmail.flatMap(userRepository::findByEmail);

        List<ArticleResponse.ArticleData> articles = articlePage.getContent().stream()
                .map(article -> mapToArticleData(article, currentUser))
                .collect(Collectors.toList());

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

        if (articleData.getTagList() != null) {
            Set<TagEntity> tags = new HashSet<>();

            for (String tagName : articleData.getTagList()) {
                Optional<TagEntity> optionalTag = tagRepository.findByTag(tagName);
                TagEntity tag = optionalTag.orElseGet(() -> {
                    TagEntity newTag = TagEntity.builder().tag(tagName).build();
                    return tagRepository.save(newTag);
                });
                tags.add(tag);
            }

            article.setTags(tags);
        }

        article = articleRepository.save(article);
        return articleMapper.toResponse(article, getTagList(article), false, 0, 
                profileService.getProfile(author.getUsername(), Optional.of(currentEmail)).getProfile());
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
                .collect(Collectors.toList());
    }

    private String toSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
