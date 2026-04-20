package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.request.NewCommentRequest;
import com.sakrafux.realworld.dto.response.CommentResponse;
import com.sakrafux.realworld.dto.response.MultipleCommentsResponse;
import com.sakrafux.realworld.entity.ArticleEntity;
import com.sakrafux.realworld.entity.CommentEntity;
import com.sakrafux.realworld.entity.UserEntity;
import com.sakrafux.realworld.exception.ResourceNotFoundException;
import com.sakrafux.realworld.exception.UnauthorizedException;
import com.sakrafux.realworld.mapper.CommentMapper;
import com.sakrafux.realworld.repository.ArticleRepository;
import com.sakrafux.realworld.repository.CommentRepository;
import com.sakrafux.realworld.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing comments on articles.
 */
@Service
@RequiredArgsConstructor
public class ArticleCommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final ProfileService profileService;

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
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        UserEntity author = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        CommentEntity comment = CommentEntity.builder()
                .body(request.getComment().getBody())
                .article(article)
                .author(author)
                .build();

        comment = commentRepository.save(comment);

        return commentMapper.toResponse(comment, 
                profileService.getProfile(author.getUsername(), Optional.of(currentEmail)).getProfile());
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
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        List<CommentEntity> comments = commentRepository.findByArticleOrderByCreatedAtDesc(article);

        Optional<UserEntity> currentUser = currentEmail.flatMap(userRepository::findByEmail);

        // NOTE: Ideally, we would load all comment author profiles in a single batch to avoid N+1 issues.
        // For the current scale and requirements, this iterative approach is acceptable.
        List<CommentResponse.CommentData> commentDataList = comments.stream()
                .map(comment -> commentMapper.toCommentData(comment,
                        profileService.getProfile(comment.getAuthor(), currentUser).getProfile()))
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
        articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthor().getEmail().equals(currentEmail)) {
            throw new UnauthorizedException("You are not the author of this comment");
        }

        commentRepository.delete(comment);
    }
}
