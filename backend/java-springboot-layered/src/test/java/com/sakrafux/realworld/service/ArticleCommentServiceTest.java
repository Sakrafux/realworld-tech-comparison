package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.request.NewCommentRequest;
import com.sakrafux.realworld.dto.response.CommentResponse;
import com.sakrafux.realworld.dto.response.MultipleCommentsResponse;
import com.sakrafux.realworld.dto.response.ProfileResponse;
import com.sakrafux.realworld.entity.ArticleEntity;
import com.sakrafux.realworld.entity.CommentEntity;
import com.sakrafux.realworld.entity.UserEntity;
import com.sakrafux.realworld.exception.UnauthorizedException;
import com.sakrafux.realworld.mapper.CommentMapper;
import com.sakrafux.realworld.repository.ArticleRepository;
import com.sakrafux.realworld.repository.CommentRepository;
import com.sakrafux.realworld.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ArticleCommentService}.
 */
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileService profileService;

    @Spy
    private CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @InjectMocks
    private ArticleCommentService articleCommentService;

    @Test
    void addComment_ValidRequest_SavesAndReturnsComment() {
        // Given
        String slug = "article-slug";
        String email = "author@example.com";
        ArticleEntity article = ArticleEntity.builder().slug(slug).build();
        UserEntity author = UserEntity.builder().username("author").email(email).build();
        NewCommentRequest request = NewCommentRequest.builder()
                .comment(NewCommentRequest.CommentData.builder().body("Test Comment").build())
                .build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(author));
        given(commentRepository.save(any(CommentEntity.class))).willAnswer(inv -> inv.getArgument(0));
        given(profileService.getProfile(eq("author"), any(Optional.class))).willReturn(
                ProfileResponse.builder().profile(ProfileResponse.ProfileData.builder().username("author").build()).build()
        );

        // When
        CommentResponse result = articleCommentService.addComment(slug, request, email);

        // Then
        assertThat(result.getComment().getBody()).isEqualTo("Test Comment");
        verify(commentRepository).save(any(CommentEntity.class));
    }

    @Test
    void getComments_ArticlesExists_ReturnsComments() {
        // Given
        String slug = "article-slug";
        ArticleEntity article = ArticleEntity.builder().slug(slug).build();
        UserEntity author = UserEntity.builder().username("author").build();
        CommentEntity comment = CommentEntity.builder()
                .body("Comment Body")
                .author(author)
                .build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(commentRepository.findByArticleOrderByCreatedAtDesc(article)).willReturn(List.of(comment));
        given(profileService.getProfile(eq(author), any(Optional.class))).willReturn(
                ProfileResponse.builder().profile(ProfileResponse.ProfileData.builder().username("author").build()).build()
        );

        // When
        MultipleCommentsResponse result = articleCommentService.getComments(slug, Optional.empty());

        // Then
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getBody()).isEqualTo("Comment Body");
    }

    @Test
    void deleteComment_AuthorDeletes_CallsDelete() {
        // Given
        String slug = "article-slug";
        Long commentId = 1L;
        String email = "author@example.com";
        ArticleEntity article = ArticleEntity.builder().slug(slug).build();
        UserEntity author = UserEntity.builder().email(email).build();
        CommentEntity comment = CommentEntity.builder().id(commentId).author(author).build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // When
        articleCommentService.deleteComment(slug, commentId, email);

        // Then
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_NonAuthor_ThrowsUnauthorizedException() {
        // Given
        String slug = "article-slug";
        Long commentId = 1L;
        String email = "hacker@example.com";
        ArticleEntity article = ArticleEntity.builder().slug(slug).build();
        UserEntity author = UserEntity.builder().email("author@example.com").build();
        CommentEntity comment = CommentEntity.builder().id(commentId).author(author).build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // When / Then
        assertThatThrownBy(() -> articleCommentService.deleteComment(slug, commentId, email))
                .isInstanceOf(UnauthorizedException.class);
    }
}
