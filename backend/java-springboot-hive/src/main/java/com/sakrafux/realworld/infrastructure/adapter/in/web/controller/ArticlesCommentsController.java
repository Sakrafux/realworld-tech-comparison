package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.application.port.in.comment.AddCommentUseCase;
import com.sakrafux.realworld.application.port.in.comment.DeleteCommentUseCase;
import com.sakrafux.realworld.application.port.in.comment.GetCommentsQuery;
import com.sakrafux.realworld.domain.model.Comment;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.NewCommentRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.CommentResponse;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.MultipleCommentsResponse;
import com.sakrafux.realworld.infrastructure.adapter.in.web.mapper.CommentWebMapper;
import com.sakrafux.realworld.infrastructure.security.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing comments on articles.
 * Maps to /articles/{slug}/comments
 */
@RestController
@RequestMapping("/articles/{slug}/comments")
@RequiredArgsConstructor
@Validated
public class ArticlesCommentsController {

    private final AddCommentUseCase addCommentUseCase;
    private final GetCommentsQuery getCommentsQuery;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final CommentWebMapper commentWebMapper;

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
        List<Comment> comments = getCommentsQuery.getComments(slug, AuthUtil.getCurrentUserEmail());
        return commentWebMapper.toMultipleResponse(comments);
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
        String authorEmail = AuthUtil.getRequiredCurrentUserEmail();
        Comment comment = addCommentUseCase.addComment(slug, request.getComment().getBody(), authorEmail);
        return commentWebMapper.toResponse(comment);
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
        String authorEmail = AuthUtil.getRequiredCurrentUserEmail();
        deleteCommentUseCase.deleteComment(slug, id, authorEmail);
    }
}
