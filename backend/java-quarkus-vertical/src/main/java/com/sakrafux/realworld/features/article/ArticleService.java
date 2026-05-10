package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.core.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
import com.sakrafux.realworld.features.article.dto.*;
import com.sakrafux.realworld.features.user.UserEntity;
import com.sakrafux.realworld.features.user.UserService;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final UserService userService;

    public MultipleArticlesResponse getArticles(String tag, String author, String favorited, int limit, int offset, Optional<String> currentEmail) {
        Map<String, Object> params = new HashMap<>();

        String hql = "FROM ArticleEntity a";
        List<String> conditions = new ArrayList<>();

        if (tag != null) {
            conditions.add(":tag MEMBER OF a.tags");
            params.put("tag", tag);
        }
        if (author != null) {
            Optional<UserEntity> authorEntity = UserEntity.findByUsername(author);
            if (authorEntity.isPresent()) {
                conditions.add("a.authorId = :authorId");
                params.put("authorId", authorEntity.get().id);
            } else {
                return articleMapper.toMultipleResponse(Collections.emptyList(), 0);
            }
        }
        if (favorited != null) {
            Optional<UserEntity> favoritedUser = UserEntity.findByUsername(favorited);
            if (favoritedUser.isPresent()) {
                conditions.add(":favoritedId MEMBER OF a.favoritedBy");
                params.put("favoritedId", favoritedUser.get().id);
            } else {
                return articleMapper.toMultipleResponse(Collections.emptyList(), 0);
            }
        }

        if (!conditions.isEmpty()) {
            hql += " WHERE " + String.join(" AND ", conditions);
        }

        PanacheQuery<ArticleEntity> panacheQuery = ArticleEntity.find(hql, Sort.descending("createdAt"), params);
        List<ArticleEntity> articles = panacheQuery.page(offset / limit, limit).list();
        long count = panacheQuery.count();

        Optional<UserEntity> currentUser = currentEmail.flatMap(UserEntity::findByEmail);

        List<ArticleResponse.ArticleData> articleDataList = articles.stream()
                .map(article -> mapToArticleData(article, currentUser))
                .collect(Collectors.toList());

        return articleMapper.toMultipleResponse(articleDataList, (int) count);
    }

    public MultipleArticlesResponse getFeed(int limit, int offset, String currentEmail) {
        UserEntity currentUser = UserEntity.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (currentUser.following.isEmpty()) {
            return articleMapper.toMultipleResponse(Collections.emptyList(), 0);
        }

        Set<Long> followingIds = currentUser.following.stream().map(u -> u.id).collect(Collectors.toSet());
        PanacheQuery<ArticleEntity> panacheQuery = ArticleEntity.find("authorId IN :followingIds", Sort.descending("createdAt"), Parameters.with("followingIds", followingIds));
        
        List<ArticleEntity> articles = panacheQuery.page(offset / limit, limit).list();
        long count = panacheQuery.count();

        List<ArticleResponse.ArticleData> articleDataList = articles.stream()
                .map(article -> mapToArticleData(article, Optional.of(currentUser)))
                .collect(Collectors.toList());

        return articleMapper.toMultipleResponse(articleDataList, (int) count);
    }

    @Transactional
    public ArticleResponse createArticle(NewArticleRequest request, String currentEmail) {
        UserEntity currentUser = UserEntity.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        var articleData = request.getArticle();

        if (ArticleEntity.findByTitle(articleData.getTitle()).isPresent()) {
            throw new ResourceAlreadyExistsException("Title already exists");
        }

        String slug = toSlug(articleData.getTitle());
        if (ArticleEntity.findBySlug(slug).isPresent()) {
            throw new ResourceAlreadyExistsException("Slug already exists");
        }

        ArticleEntity article = ArticleEntity.builder()
                .title(articleData.getTitle())
                .slug(slug)
                .description(articleData.getDescription())
                .body(articleData.getBody())
                .authorId(currentUser.id)
                .build();

        if (articleData.getTagList() != null) {
            persistTags(articleData.getTagList());
            article.setTags(new HashSet<>(articleData.getTagList()));
        }

        article.persist();
        return articleMapper.toResponse(article, getTagList(article), false, 0,
                userService.getProfile(currentUser.username, Optional.of(currentEmail)).getProfile());
    }

    public ArticleResponse getArticle(String slug, Optional<String> currentEmail) {
        ArticleEntity article = ArticleEntity.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Optional<UserEntity> currentUser = currentEmail.flatMap(UserEntity::findByEmail);
        UserEntity author = UserEntity.findById(article.getAuthorId());
        return articleMapper.toResponse(article, getTagList(article),
                currentUser.map(user -> article.getFavoritedBy().contains(user.id)).orElse(false),
                article.getFavoritedBy().size(),
                userService.getProfile(author.username, currentEmail).getProfile());
    }

    @Transactional
    public ArticleResponse updateArticle(String slug, UpdateArticleRequest request, String currentEmail) {
        ArticleEntity article = ArticleEntity.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        UserEntity currentUser = UserEntity.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!article.getAuthorId().equals(currentUser.id)) {
            throw new UnauthorizedException("You are not the author of this article");
        }

        var articleData = request.getArticle();

        if (articleData.getTitle() != null && !articleData.getTitle().equals(article.getTitle())) {
            if (ArticleEntity.findByTitle(articleData.getTitle()).isPresent()) {
                throw new ResourceAlreadyExistsException("Title already exists");
            }
            String newSlug = toSlug(articleData.getTitle());
            if (ArticleEntity.findBySlug(newSlug).isPresent()) {
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

        return articleMapper.toResponse(article, getTagList(article),
                article.getFavoritedBy().contains(currentUser.id),
                article.getFavoritedBy().size(),
                userService.getProfile(currentUser.username, Optional.of(currentEmail)).getProfile());
    }

    @Transactional
    public void deleteArticle(String slug, String currentEmail) {
        ArticleEntity article = ArticleEntity.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        UserEntity currentUser = UserEntity.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!article.getAuthorId().equals(currentUser.id)) {
            throw new UnauthorizedException("You are not the author of this article");
        }

        article.delete();
    }

    @Transactional
    public ArticleResponse favoriteArticle(String slug, String currentEmail) {
        ArticleEntity article = ArticleEntity.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        UserEntity currentUser = UserEntity.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        article.getFavoritedBy().add(currentUser.id);

        UserEntity author = UserEntity.findById(article.getAuthorId());
        return articleMapper.toResponse(article, getTagList(article), true, article.getFavoritedBy().size(),
                userService.getProfile(author.username, Optional.of(currentEmail)).getProfile());
    }

    @Transactional
    public ArticleResponse unfavoriteArticle(String slug, String currentEmail) {
        ArticleEntity article = ArticleEntity.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        UserEntity currentUser = UserEntity.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        article.getFavoritedBy().remove(currentUser.id);

        UserEntity author = UserEntity.findById(article.getAuthorId());
        return articleMapper.toResponse(article, getTagList(article), false, article.getFavoritedBy().size(),
                userService.getProfile(author.username, Optional.of(currentEmail)).getProfile());
    }

    private void persistTags(List<String> tags) {
        for (String tagName : tags) {
            if (TagEntity.findByTag(tagName).isEmpty()) {
                TagEntity.builder().tag(tagName).build().persist();
            }
        }
    }

    private ArticleResponse.ArticleData mapToArticleData(ArticleEntity article, Optional<UserEntity> currentUser) {
        List<String> tagList = getTagList(article);
        boolean favorited = currentUser
                .map(user -> article.getFavoritedBy().contains(user.id))
                .orElse(false);
        int favoritesCount = article.getFavoritedBy().size();

        UserEntity author = UserEntity.findById(article.getAuthorId());
        var authorProfile = userService.getProfile(author.username, currentUser.map(u -> u.email)).getProfile();

        return articleMapper.toArticleData(article, tagList, favorited, favoritesCount, authorProfile);
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
