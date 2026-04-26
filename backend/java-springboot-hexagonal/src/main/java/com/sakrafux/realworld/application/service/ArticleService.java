package com.sakrafux.realworld.application.service;

import com.sakrafux.realworld.application.port.in.article.*;
import com.sakrafux.realworld.application.port.in.profile.GetProfileQuery;
import com.sakrafux.realworld.application.port.out.ArticleRepository;
import com.sakrafux.realworld.application.port.out.TagRepository;
import com.sakrafux.realworld.application.port.out.UserRepository;
import com.sakrafux.realworld.domain.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.domain.exception.ResourceNotFoundException;
import com.sakrafux.realworld.domain.exception.UnauthorizedException;
import com.sakrafux.realworld.domain.model.Article;
import com.sakrafux.realworld.domain.model.Tag;
import com.sakrafux.realworld.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService implements CreateArticleUseCase, UpdateArticleUseCase, DeleteArticleUseCase, 
        GetArticleQuery, GetArticlesQuery, GetFeedQuery, FavoriteArticleUseCase, UnfavoriteArticleUseCase {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final GetProfileQuery getProfileQuery;

    @Override
    @Retryable(
            retryFor = DataIntegrityViolationException.class,
            backoff = @Backoff(delay = 50, multiplier = 2.0)
    )
    @Transactional
    public Article createArticle(CreateArticleCommand command) {
        User author = userRepository.findByEmail(command.authorEmail())
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
        
        observerEmail.flatMap(userRepository::findByEmail).ifPresent(observer -> {
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
            filter.observerEmail().flatMap(userRepository::findByEmail).ifPresent(observer -> {
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
            userRepository.findByEmail(observerEmail).ifPresent(observer -> {
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
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        articleRepository.favorite(user.getId(), article.getId());
        
        return getArticle(slug, Optional.of(userEmail));
    }

    @Override
    @Transactional
    public Article unfavoriteArticle(String slug, String userEmail) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        articleRepository.unfavorite(user.getId(), article.getId());
        
        return getArticle(slug, Optional.of(userEmail));
    }

    private String getUserNameByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private String toSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
