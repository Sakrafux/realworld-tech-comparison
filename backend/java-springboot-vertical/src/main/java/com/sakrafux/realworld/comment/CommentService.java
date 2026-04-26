package com.sakrafux.realworld.comment;

import com.sakrafux.realworld.article.ArticleIntegrationService;
import com.sakrafux.realworld.comment.request.NewCommentRequest;
import com.sakrafux.realworld.comment.response.CommentResponse;
import com.sakrafux.realworld.comment.response.MultipleCommentsResponse;
import com.sakrafux.realworld.profile.ProfileService;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
import com.sakrafux.realworld.user.UserIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing comments on articles.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ProfileService profileService;
    private final UserIntegrationService userIntegrationService;
    private final ArticleIntegrationService articleIntegrationService;

    /**
     * Adds a comment to an article.
     *
     * @param slug         the article slug
     * @param request      the comment details
     * @param currentEmail email of the authenticated user
     * @return CommentResponse containing the created comment details
     */
    @Transactional
    public CommentResponse addComment(String slug, NewCommentRequest request, String currentEmail) {
        Long articleId = articleIntegrationService.findArticleIdBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        Long authorId = userIntegrationService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        CommentEntity comment = CommentEntity.builder()
                .body(request.getComment().getBody())
                .articleId(articleId)
                .authorId(authorId)
                .build();

        comment = commentRepository.save(comment);

        return commentMapper.toResponse(comment, 
                profileService.getProfile(authorId, Optional.of(authorId)).getProfile());
    }

    /**
     * Retrieves all comments for an article.
     *
     * @param slug         the article slug
     * @param currentEmail optional email of the authenticated user
     * @return MultipleCommentsResponse containing the list of comments
     */
    @Transactional(readOnly = true)
    public MultipleCommentsResponse getComments(String slug, Optional<String> currentEmail) {
        Long articleId = articleIntegrationService.findArticleIdBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        List<CommentEntity> comments = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);

        Optional<Long> currentUserId = currentEmail.flatMap(userIntegrationService::findUserIdByEmail);

        List<CommentResponse.CommentData> commentDataList = comments.stream()
                .map(comment -> commentMapper.toCommentData(comment,
                        profileService.getProfile(comment.getAuthorId(), currentUserId).getProfile()))
                .toList();

        return commentMapper.toMultipleResponse(commentDataList);
    }

    /**
     * Deletes a comment from an article.
     *
     * @param slug         the article slug
     * @param commentId    the comment ID
     * @param currentEmail email of the authenticated user
     */
    @Transactional
    public void deleteComment(String slug, Long commentId, String currentEmail) {
        // RealWorld spec says "slug" is part of the path, but the ID is unique.
        // We check if the article exists first.
        articleIntegrationService.findArticleIdBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        Long currentUserId = userIntegrationService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!comment.getAuthorId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not the author of this comment");
        }

        commentRepository.delete(comment);
    }
}