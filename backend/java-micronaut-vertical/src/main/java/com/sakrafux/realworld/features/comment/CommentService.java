package com.sakrafux.realworld.features.comment;

import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
import com.sakrafux.realworld.features.article.ArticleEntity;
import com.sakrafux.realworld.features.article.ArticleRepository;
import com.sakrafux.realworld.features.comment.dto.CommentResponse;
import com.sakrafux.realworld.features.comment.dto.MultipleCommentsResponse;
import com.sakrafux.realworld.features.comment.dto.NewCommentRequest;
import com.sakrafux.realworld.features.user.UserService;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final ArticleRepository articleRepository;

    @Transactional
    public CommentResponse addComment(String slug, NewCommentRequest request, String currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        
        Long authorId = userService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        CommentEntity comment = CommentEntity.builder()
                .body(request.getComment().getBody())
                .articleId(article.getId())
                .authorId(authorId)
                .build();

        comment = commentRepository.save(comment);

        var authorProfile = userService.getProfileById(authorId, Optional.of(currentEmail)).getProfile();

        return commentMapper.toResponse(comment, authorProfile);
    }

    @Transactional
    public MultipleCommentsResponse getComments(String slug, Optional<String> currentEmail) {
        ArticleEntity article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        List<CommentEntity> comments = commentRepository.findByArticleIdOrderByCreatedAtDesc(article.getId());

        List<CommentResponse.CommentData> commentDataList = comments.stream()
                .map(comment -> commentMapper.toCommentData(comment,
                        userService.getProfileById(comment.getAuthorId(), currentEmail).getProfile()))
                .toList();

        return commentMapper.toMultipleResponse(commentDataList);
    }

    @Transactional
    public void deleteComment(String slug, Long commentId, String currentEmail) {
        articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        Long currentUserId = userService.findUserIdByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!comment.getAuthorId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not the author of this comment");
        }

        commentRepository.delete(comment);
    }
}
