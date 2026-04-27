package com.sakrafux.realworld.article.application.service;

import com.sakrafux.realworld.article.application.port.in.*;
import com.sakrafux.realworld.user.application.port.in.GetProfileQuery;
import com.sakrafux.realworld.article.application.port.out.ArticleRepository;
import com.sakrafux.realworld.article.application.port.out.TagRepository;
import com.sakrafux.realworld.user.application.port.api.UserInternalApi;
import com.sakrafux.realworld.core.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
import com.sakrafux.realworld.article.domain.Article;
import com.sakrafux.realworld.article.domain.Tag;
import com.sakrafux.realworld.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService implements CreateArticleUseCase, UpdateArticleUseCase, DeleteArticleUseCase,
        GetArticleQuery, GetArticlesQuery, GetFeedQuery, FavoriteArticleUseCase, UnfavoriteArticleUseCase {

    private final ArticleRepository articleRepository;
    private final UserInternalApi userInternalApi;
    private final TagRepository tagRepository;
    private final GetProfileQuery getProfileQuery;

    @Override
    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            backoff = @Backoff(delay = 50, multiplier = 2.0)
    )
    @Transactional
    public Article createArticle(CreateArticleCommand command) {
        User author = userInternalApi.getUserByEmail(command.authorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", command.authorEmail()));

        if (articleRepository.findByTitle(command.title()).isPresent()) {
            throw new ResourceAlreadyExistsException("Title already exists");
        }

        String slug = toSlug(command.title());
        if (articleRepository.findBySlug(slug).isPresent()) {
            throw new ResourceAlreadyExistsException("Slug already exists");
        }

        ensureTagsExist(command.tagList());

        Article article = Article.builder()
                .title(command.title())
                .slug(slug)
                .description(command.description())
                .body(command.body())
                .tagList(command.tagList() != null ? command.tagList().stream().sorted().toList() : Collections.emptyList())
                .author(getProfileQuery.getProfile(author.getUsername(), Optional.of(command.authorEmail())))
                .favorited(false)
                .favoritesCount(0)
                .build();

        return articleRepository.save(article);
    }

    private void ensureTagsExist(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;
        
        List<Tag> existingTags = tagRepository.findByNames(tagNames);
        Set<String> existingNames = existingTags.stream().map(Tag::getName).collect(Collectors.toSet());
        
        List<Tag> missingTags = tagNames.stream()
                .filter(name -> !existingNames.contains(name))
                .map(Tag::new)
                .toList();
        
        if (!missingTags.isEmpty()) {
            tagRepository.saveAll(missingTags);
        }
    }

    @Override
    @Transactional
    public Article updateArticle(UpdateArticleCommand command) {
        Article article = articleRepository.findBySlug(command.slug())
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", command.slug()));

        if (!article.getAuthor().getUsername().equals(getUserNameByEmail(command.authorEmail()))) {
            throw new UnauthorizedException("You are not the author of this article");
        }

        String newSlug = article.getSlug();
        if (command.title() != null && !command.title().equals(article.getTitle())) {
            if (articleRepository.findByTitle(command.title()).isPresent()) {
                throw new ResourceAlreadyExistsException("Title already exists");
            }
            newSlug = toSlug(command.title());
            if (articleRepository.findBySlug(newSlug).isPresent()) {
                throw new ResourceAlreadyExistsException("Slug already exists");
            }
        }

        article.update(command.title(), newSlug, command.description(), command.body());
        return articleRepository.save(article);
    }

    @Override
    @Transactional
    public void deleteArticle(String slug, String authorEmail) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        if (!article.getAuthor().getUsername().equals(getUserNameByEmail(authorEmail))) {
            throw new UnauthorizedException("You are not the author of this article");
        }

        articleRepository.delete(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public Article getArticle(String slug, Optional<String> observerEmail) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        
        // Refresh author profile with observer context
        article.setAuthor(getProfileQuery.getProfile(article.getAuthor().getUsername(), observerEmail));
        
        observerEmail.flatMap(userInternalApi::getUserByEmail).ifPresent(observer -> {
            article.setFavorited(articleRepository.isFavorited(observer.getId(), article.getId()));
        });

        return article;
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleListResult getArticles(GetArticlesFilter filter) {
        List<Article> articles = articleRepository.findFiltered(filter);
        long count = articleRepository.countFiltered(filter);
        
        articles.forEach(article -> {
            article.setAuthor(getProfileQuery.getProfile(article.getAuthor().getUsername(), filter.observerEmail()));
            filter.observerEmail().flatMap(userInternalApi::getUserByEmail).ifPresent(observer -> {
                article.setFavorited(articleRepository.isFavorited(observer.getId(), article.getId()));
            });
        });

        return new ArticleListResult(articles, count);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleListResult getFeed(int limit, int offset, String observerEmail) {
        List<Article> articles = articleRepository.findFeed(observerEmail, limit, offset);
        long count = articleRepository.countFeed(observerEmail);

        articles.forEach(article -> {
            article.setAuthor(getProfileQuery.getProfile(article.getAuthor().getUsername(), Optional.of(observerEmail)));
            userInternalApi.getUserByEmail(observerEmail).ifPresent(observer -> {
                article.setFavorited(articleRepository.isFavorited(observer.getId(), article.getId()));
            });
        });

        return new ArticleListResult(articles, count);
    }

    @Override
    @Transactional
    public Article favoriteArticle(String slug, String userEmail) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        User user = userInternalApi.getUserByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        articleRepository.favorite(user.getId(), article.getId());
        
        return getArticle(slug, Optional.of(userEmail));
    }

    @Override
    @Transactional
    public Article unfavoriteArticle(String slug, String userEmail) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        User user = userInternalApi.getUserByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        articleRepository.unfavorite(user.getId(), article.getId());
        
        return getArticle(slug, Optional.of(userEmail));
    }

    private String getUserNameByEmail(String email) {
        return userInternalApi.getUserByEmail(email)
                .map(User::getUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private String toSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
