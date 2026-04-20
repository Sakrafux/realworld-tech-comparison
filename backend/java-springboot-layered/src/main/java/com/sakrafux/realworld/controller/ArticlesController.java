package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.NewArticleRequest;
import com.sakrafux.realworld.dto.response.ArticleResponse;
import com.sakrafux.realworld.dto.response.MultipleArticlesResponse;
import com.sakrafux.realworld.security.AuthUtil;
import com.sakrafux.realworld.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing articles.
 * Exposes endpoints for retrieving, creating, updating, and deleting articles.
 */
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticlesController {

    private final ArticleService articleService;

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
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return articleService.getArticles(tag, author, favorited, limit, offset, AuthUtil.getCurrentUserEmail());
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
    public ArticleResponse createArticle(@Valid @RequestBody NewArticleRequest request) {
        return articleService.createArticle(request, AuthUtil.getRequiredCurrentUserEmail());
    }
}
