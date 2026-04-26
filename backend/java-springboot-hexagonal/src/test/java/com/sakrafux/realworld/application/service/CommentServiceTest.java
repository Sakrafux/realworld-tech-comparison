package com.sakrafux.realworld.application.service;

import com.sakrafux.realworld.application.port.in.profile.GetProfileQuery;
import com.sakrafux.realworld.application.port.out.ArticleRepository;
import com.sakrafux.realworld.application.port.out.CommentRepository;
import com.sakrafux.realworld.application.port.out.UserRepository;
import com.sakrafux.realworld.domain.exception.UnauthorizedException;
import com.sakrafux.realworld.domain.model.Article;
import com.sakrafux.realworld.domain.model.Comment;
import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GetProfileQuery getProfileQuery;

    @InjectMocks
    private CommentService commentService;

    @Test
    void addComment_validData_savesAndReturnsComment() {
        // Given
        String slug = "slug";
        String email = "author@example.com";
        String body = "comment body";
        User author = User.builder().username("author").email(email).build();
        Profile authorProfile = Profile.builder().username("author").build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(Article.builder().build()));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(author));
        given(getProfileQuery.getProfile(eq("author"), any())).willReturn(authorProfile);
        given(commentRepository.save(any(Comment.class), eq(slug))).willAnswer(inv -> inv.getArgument(0));

        // When
        Comment result = commentService.addComment(slug, body, email);

        // Then
        assertThat(result.getBody()).isEqualTo(body);
        verify(commentRepository).save(any(Comment.class), eq(slug));
    }

    @Test
    void getComments_articleExists_returnsComments() {
        // Given
        String slug = "slug";
        Comment comment = Comment.builder().author(Profile.builder().username("author").build()).build();
        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(Article.builder().build()));
        given(commentRepository.findByArticleSlug(slug)).willReturn(List.of(comment));
        given(getProfileQuery.getProfile(eq("author"), any())).willReturn(Profile.builder().username("author").build());

        // When
        List<Comment> result = commentService.getComments(slug, Optional.empty());

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void deleteComment_authorDeletes_callsRepository() {
        // Given
        String slug = "slug";
        Long id = 1L;
        String email = "author@example.com";
        User author = User.builder().username("author").email(email).build();
        Comment comment = Comment.builder().id(id).author(Profile.builder().username("author").build()).build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(Article.builder().build()));
        given(commentRepository.findById(id)).willReturn(Optional.of(comment));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(author));

        // When
        commentService.deleteComment(slug, id, email);

        // Then
        verify(commentRepository).delete(id);
    }

    @Test
    void deleteComment_notAuthor_throwsException() {
        // Given
        String slug = "slug";
        Long id = 1L;
        String email = "hacker@example.com";
        User author = User.builder().username("hacker").email(email).build();
        Comment comment = Comment.builder().id(id).author(Profile.builder().username("author").build()).build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(Article.builder().build()));
        given(commentRepository.findById(id)).willReturn(Optional.of(comment));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(author));

        // When / Then
        assertThatThrownBy(() -> commentService.deleteComment(slug, id, email))
                .isInstanceOf(UnauthorizedException.class);
    }
}
