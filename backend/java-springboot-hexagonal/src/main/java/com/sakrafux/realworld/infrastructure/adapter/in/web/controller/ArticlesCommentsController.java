package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.NewCommentRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.CommentResponse;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.MultipleCommentsResponse;
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
        throw new UnsupportedOperationException("TODO");
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
            @Valid @RequestBody NewCommentRequest request
    ) {
        throw new UnsupportedOperationException("TODO");
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
            @PathVariable Long id
    ) {
        throw new UnsupportedOperationException("TODO");
    }
}
