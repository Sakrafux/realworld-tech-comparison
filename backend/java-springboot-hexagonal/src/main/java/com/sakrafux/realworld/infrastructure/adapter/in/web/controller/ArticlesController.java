package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.NewArticleRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.UpdateArticleRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.ArticleResponse;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.MultipleArticlesResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.sakrafux.realworld.application.port.in.article.CreateArticleUseCase;
import com.sakrafux.realworld.application.port.in.article.DeleteArticleUseCase;
import com.sakrafux.realworld.application.port.in.article.GetArticleQuery;
import com.sakrafux.realworld.application.port.in.article.UpdateArticleUseCase;
import com.sakrafux.realworld.domain.model.Article;
import com.sakrafux.realworld.infrastructure.adapter.in.web.mapper.ArticleWebMapper;
import com.sakrafux.realworld.infrastructure.security.AuthUtil;

/**
 * REST Controller for managing articles.
 * Exposes endpoints for retrieving, creating, updating, and deleting articles.
 */
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
@Validated
public class ArticlesController {

    private final CreateArticleUseCase createArticleUseCase;
    private final UpdateArticleUseCase updateArticleUseCase;
    private final DeleteArticleUseCase deleteArticleUseCase;
    private final GetArticleQuery getArticleQuery;
    private final com.sakrafux.realworld.application.port.in.article.GetArticlesQuery getArticlesQuery;
    private final com.sakrafux.realworld.application.port.in.article.GetFeedQuery getFeedQuery;
    private final ArticleWebMapper articleWebMapper;

    /**
     * Retrieves a list of articles globally.
     * Maps to: GET /api/articles
     *
     * @param tag       filter by tag
     * @param author    filter by author username
     * @param favorited filter by username who favorited the article
     * @param limit     limit the number of results (default 20)
     * @param offset    offset for pagination (default 0)
     * @return a response containing a list of articles and total count
     */
    @GetMapping
    public MultipleArticlesResponse getArticles(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String favorited,
            @RequestParam(defaultValue = "20") @Min(1) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        var filter = articleWebMapper.toFilter(tag, author, favorited, limit, offset, AuthUtil.getCurrentUserEmail());
        var result = getArticlesQuery.getArticles(filter);
        return articleWebMapper.toMultipleResponse(result);
    }

    /**
     * Retrieves the article feed for the current user.
     * Maps to: GET /api/articles/feed
     * Auth required.
     *
     * @param limit  limit the number of results (default 20)
     * @param offset offset for pagination (default 0)
     * @return a response containing a list of articles and total count
     */
    @GetMapping("/feed")
    public MultipleArticlesResponse getArticlesFeed(
            @RequestParam(defaultValue = "20") @Min(1) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        String observerEmail = AuthUtil.getRequiredCurrentUserEmail();
        var result = getFeedQuery.getFeed(limit, offset, observerEmail);
        
        return MultipleArticlesResponse.builder()
                .articles(result.articles().stream().map(articleWebMapper::toResponse).map(ArticleResponse::getArticle).toList())
                .articlesCount((int) result.totalCount())
                .build();
    }

    /**
     * Retrieves a single article.
     * Maps to: GET /api/articles/{slug}
     * Auth optional.
     *
     * @param slug the article slug
     * @return the requested article
     */
    @GetMapping("/{slug}")
    public ArticleResponse getArticle(@PathVariable String slug) {
        Article article = getArticleQuery.getArticle(slug, AuthUtil.getCurrentUserEmail());
        return articleWebMapper.toResponse(article);
    }

    /**
     * Updates an article.
     * Maps to: PUT /api/articles/{slug}
     * Auth required.
     *
     * @param slug    the article slug
     * @param request the article update details
     * @return the updated article
     */
    @PutMapping("/{slug}")
    public ArticleResponse updateArticle(
            @PathVariable String slug,
            @Valid @RequestBody UpdateArticleRequest request
    ) {
        String authorEmail = AuthUtil.getRequiredCurrentUserEmail();
        Article article = updateArticleUseCase.updateArticle(articleWebMapper.toUpdateCommand(request, slug, authorEmail));
        return articleWebMapper.toResponse(article);
    }

    /**
     * Deletes an article.
     * Maps to: DELETE /api/articles/{slug}
     * Auth required.
     *
     * @param slug the article slug
     */
    @DeleteMapping("/{slug}")
    public void deleteArticle(@PathVariable String slug) {
        String authorEmail = AuthUtil.getRequiredCurrentUserEmail();
        deleteArticleUseCase.deleteArticle(slug, authorEmail);
    }

    /**
     * Creates a new article.
     * Maps to: POST /api/articles
     * Auth required.
     *
     * @param request the new article details
     * @return the created article
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse createArticle(@Valid @RequestBody NewArticleRequest request) {
        String authorEmail = AuthUtil.getRequiredCurrentUserEmail();
        Article article = createArticleUseCase.createArticle(articleWebMapper.toCreateCommand(request, authorEmail));
        return articleWebMapper.toResponse(article);
    }

    /**
     * Favorites an article.
     * Maps to: POST /api/articles/{slug}/favorite
     * Auth required.
     *
     * @param slug the article slug
     * @return the updated article
     */
    @PostMapping("/{slug}/favorite")
    public ArticleResponse favoriteArticle(@PathVariable String slug) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Unfavorites an article.
     * Maps to: DELETE /api/articles/{slug}/favorite
     * Auth required.
     *
     * @param slug the article slug
     * @return the updated article
     */
    @DeleteMapping("/{slug}/favorite")
    public ArticleResponse unfavoriteArticle(@PathVariable String slug) {
        throw new UnsupportedOperationException("TODO");
    }
}
