package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.core.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
import com.sakrafux.realworld.features.article.dto.ArticleResponse;
import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.article.dto.UpdateArticleRequest;
import com.sakrafux.realworld.features.user.UserEntity;
import com.sakrafux.realworld.features.user.UserRepository;
import com.sakrafux.realworld.features.user.UserService;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final ArticleMapper articleMapper;
    private final UserService userService;
    private final UserRepository userRepository;

    @Transactional
    public ArticleResponse createArticle(NewArticleRequest request, String currentEmail) {
        UserEntity currentUser = userRepository.findByEmail(currentEmail)
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
                .authorId(currentUser.getId())
                .build();

        if (articleData.getTagList() != null) {
            persistTags(articleData.getTagList());
            article.setTags(new HashSet<>(articleData.getTagList()));
        }

        article = articleRepository.save(article);
        
        var authorProfile = userService.getProfile(currentUser.getUsername(), Optional.of(currentEmail)).getProfile();
        return articleMapper.toResponse(article, getTagList(article), false, 0, authorProfile);
    }

    @Transactional
    public ArticleResponse getArticle(String slug, Optional<String> currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Long currentUserId = currentEmail.flatMap(email -> userRepository.findByEmail(email).map(UserEntity::getId)).orElse(null);
        boolean favorited = currentUserId != null && article.getFavoritedBy().contains(currentUserId);
        
        UserEntity author = userRepository.findById(article.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", article.getAuthorId()));
                
        var authorProfile = userService.getProfile(author.getUsername(), currentEmail).getProfile();

        return articleMapper.toResponse(article, getTagList(article), favorited, article.getFavoritedBy().size(), authorProfile);
    }

    @Transactional
    public ArticleResponse updateArticle(String slug, UpdateArticleRequest request, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!article.getAuthorId().equals(currentUser.getId())) {
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

        var authorProfile = userService.getProfile(currentUser.getUsername(), Optional.of(currentEmail)).getProfile();
        return articleMapper.toResponse(article, getTagList(article), article.getFavoritedBy().contains(currentUser.getId()), article.getFavoritedBy().size(), authorProfile);
    }

    @Transactional
    public void deleteArticle(String slug, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!article.getAuthorId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not the author of this article");
        }

        articleRepository.delete(article);
    }

    @Transactional
    public ArticleResponse favoriteArticle(String slug, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (article.getFavoritedBy().add(currentUser.getId())) {
            article = articleRepository.update(article);
        }

        final Long authorId = article.getAuthorId();
        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));
                
        var authorProfile = userService.getProfile(author.getUsername(), Optional.of(currentEmail)).getProfile();

        return articleMapper.toResponse(article, getTagList(article), true, article.getFavoritedBy().size(), authorProfile);
    }

    @Transactional
    public ArticleResponse unfavoriteArticle(String slug, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (article.getFavoritedBy().remove(currentUser.getId())) {
            article = articleRepository.update(article);
        }

        final Long authorId = article.getAuthorId();
        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));
                
        var authorProfile = userService.getProfile(author.getUsername(), Optional.of(currentEmail)).getProfile();

        return articleMapper.toResponse(article, getTagList(article), false, article.getFavoritedBy().size(), authorProfile);
    }

    private void persistTags(List<String> tags) {
        for (String tagName : tags) {
            if (tagRepository.findByTag(tagName).isEmpty()) {
                tagRepository.save(TagEntity.builder().tag(tagName).build());
            }
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
