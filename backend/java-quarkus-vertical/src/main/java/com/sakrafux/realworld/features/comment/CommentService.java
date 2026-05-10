package com.sakrafux.realworld.features.comment;

import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
import com.sakrafux.realworld.features.article.ArticleEntity;
import com.sakrafux.realworld.features.comment.dto.CommentResponse;
import com.sakrafux.realworld.features.comment.dto.MultipleCommentsResponse;
import com.sakrafux.realworld.features.comment.dto.NewCommentRequest;
import com.sakrafux.realworld.features.user.UserEntity;
import com.sakrafux.realworld.features.user.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserService userService;

    @Transactional
    public CommentResponse addComment(String slug, NewCommentRequest request, String currentEmail) {
        ArticleEntity article = ArticleEntity.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));
        UserEntity currentUser = UserEntity.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        CommentEntity comment = CommentEntity.builder()
                .body(request.getComment().getBody())
                .articleId(article.getId())
                .authorId(currentUser.id)
                .build();

        comment.persist();

        var authorProfile = userService.getProfile(currentUser.username, Optional.of(currentEmail)).getProfile();
        return commentMapper.toResponse(comment, authorProfile);
    }

    public MultipleCommentsResponse getComments(String slug, Optional<String> currentEmail) {
        ArticleEntity article = ArticleEntity.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        List<CommentEntity> comments = CommentEntity.findByArticleId(article.getId());

        List<CommentResponse.CommentData> commentDataList = comments.stream()
                .map(comment -> {
                    UserEntity author = UserEntity.findById(comment.getAuthorId());
                    var authorProfile = userService.getProfile(author.username, currentEmail).getProfile();
                    return commentMapper.toCommentData(comment, authorProfile);
                })
                .collect(Collectors.toList());

        return commentMapper.toMultipleResponse(commentDataList);
    }

    @Transactional
    public void deleteComment(String slug, Long commentId, String currentEmail) {
        ArticleEntity.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        CommentEntity comment = CommentEntity.findById(commentId);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }

        UserEntity currentUser = UserEntity.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!comment.getAuthorId().equals(currentUser.id)) {
            throw new UnauthorizedException("You are not the author of this comment");
        }

        comment.delete();
    }
}
