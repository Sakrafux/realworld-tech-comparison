package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.request.NewCommentRequest;
import com.sakrafux.realworld.dto.response.CommentResponse;
import com.sakrafux.realworld.dto.response.MultipleCommentsResponse;
import com.sakrafux.realworld.security.AuthUtil;
import com.sakrafux.realworld.service.ArticleCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing comments on articles.
 * Maps to /articles/{slug}/comments
 */
@RestController
@RequestMapping("/articles/{slug}/comments")
@RequiredArgsConstructor
@Validated
public class ArticlesCommentsController {

    private final ArticleCommentService articleCommentService;

    /**
     * Retrieves all comments for an article.
     * Maps to: GET /api/articles/{slug}/comments
     * Auth optional.
     *
     * @param slug the article slug
     * @return response containing a list of comments
     */
    @GetMapping
    public MultipleCommentsResponse getComments(@PathVariable String slug) {
        return articleCommentService.getComments(slug, AuthUtil.getCurrentUserEmail());
    }

    /**
     * Adds a comment to an article.
     * Maps to: POST /api/articles/{slug}/comments
     * Auth required.
     *
     * @param slug    the article slug
     * @param request the comment details
     * @return the created comment
     */
    @PostMapping
    public CommentResponse addComment(
            @PathVariable String slug,
            @Valid @RequestBody NewCommentRequest request) {
        return articleCommentService.addComment(slug, request, AuthUtil.getRequiredCurrentUserEmail());
    }

    /**
     * Deletes a comment from an article.
     * Maps to: DELETE /api/articles/{slug}/comments/{id}
     * Auth required.
     *
     * @param slug the article slug
     * @param id   the comment ID
     */
    @DeleteMapping("/{id}")
    public void deleteComment(
            @PathVariable String slug,
            @PathVariable Long id) {
        articleCommentService.deleteComment(slug, id, AuthUtil.getRequiredCurrentUserEmail());
    }
}
