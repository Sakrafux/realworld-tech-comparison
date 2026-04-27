package com.sakrafux.realworld.article.application.service;

import com.sakrafux.realworld.article.application.port.in.AddCommentUseCase;
import com.sakrafux.realworld.article.application.port.in.DeleteCommentUseCase;
import com.sakrafux.realworld.article.application.port.in.GetCommentsQuery;
import com.sakrafux.realworld.user.application.port.api.UserInternalApi;
import com.sakrafux.realworld.user.application.port.in.GetProfileQuery;
import com.sakrafux.realworld.article.application.port.out.ArticleRepository;
import com.sakrafux.realworld.article.application.port.out.CommentRepository;
import com.sakrafux.realworld.core.exception.ResourceNotFoundException;
import com.sakrafux.realworld.core.exception.UnauthorizedException;
import com.sakrafux.realworld.article.domain.Comment;
import com.sakrafux.realworld.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService implements AddCommentUseCase, GetCommentsQuery, DeleteCommentUseCase {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserInternalApi userInternalApi;
    private final GetProfileQuery getProfileQuery;

    @Override
    @Transactional
    public Comment addComment(String slug, String body, String authorEmail) {
        articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        User author = userInternalApi.getUserByEmail(authorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authorEmail));

        Comment comment = Comment.builder()
                .body(body)
                .author(getProfileQuery.getProfile(author.getUsername(), Optional.of(authorEmail)))
                .build();

        return commentRepository.save(comment, slug);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getComments(String slug, Optional<String> observerEmail) {
        articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        List<Comment> comments = commentRepository.findByArticleSlug(slug);
        comments.forEach(comment ->
                comment.setAuthor(getProfileQuery.getProfile(comment.getAuthor().getUsername(), observerEmail))
        );

        return comments;
    }

    @Override
    @Transactional
    public void deleteComment(String slug, Long id, String authorEmail) {
        articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "slug", slug));

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        User author = userInternalApi.getUserByEmail(authorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authorEmail));

        if (!comment.getAuthor().getUsername().equals(author.getUsername())) {
            throw new UnauthorizedException("You are not the author of this comment");
        }

        commentRepository.delete(id);
    }
}
