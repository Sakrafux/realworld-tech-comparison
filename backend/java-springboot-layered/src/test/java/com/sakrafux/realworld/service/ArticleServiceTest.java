package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.request.NewArticleRequest;
import com.sakrafux.realworld.dto.request.UpdateArticleRequest;
import com.sakrafux.realworld.dto.response.ArticleResponse;
import com.sakrafux.realworld.dto.response.MultipleArticlesResponse;
import com.sakrafux.realworld.dto.response.ProfileResponse;
import com.sakrafux.realworld.entity.ArticleEntity;
import com.sakrafux.realworld.entity.TagEntity;
import com.sakrafux.realworld.entity.UserEntity;
import com.sakrafux.realworld.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.exception.UnauthorizedException;
import com.sakrafux.realworld.mapper.ArticleMapper;
import com.sakrafux.realworld.repository.ArticleRepository;
import com.sakrafux.realworld.repository.TagRepository;
import com.sakrafux.realworld.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ArticleService}.
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileService profileService;

    @Spy
    private ArticleMapper articleMapper = Mappers.getMapper(ArticleMapper.class);

    @InjectMocks
    private ArticleService articleService;

    @Test
    void createArticle_ValidRequest_SavesAndReturnsArticle() {
        // Given
        String email = "author@example.com";
        UserEntity author = UserEntity.builder().username("author").email(email).build();
        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder()
                        .title("My Article")
                        .description("Description")
                        .body("Body")
                        .tagList(List.of("tag1"))
                        .build())
                .build();

        ArticleEntity article = ArticleEntity.builder()
                .title("My Article")
                .slug("my-article")
                .author(author)
                .tags(new HashSet<>(List.of(TagEntity.builder().tag("tag1").build())))
                .favoritedBy(new HashSet<>())
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(author));
        given(articleRepository.findByTitle("My Article")).willReturn(Optional.empty());
        given(articleRepository.findBySlug("my-article")).willReturn(Optional.empty());
        given(tagRepository.findByTag("tag1")).willReturn(Optional.of(TagEntity.builder().tag("tag1").build()));
        given(articleRepository.save(any(ArticleEntity.class))).willAnswer(inv -> inv.getArgument(0));
        given(profileService.getProfile(eq("author"), any())).willReturn(
                ProfileResponse.builder().profile(ProfileResponse.ProfileData.builder().username("author").build()).build()
        );

        // When
        ArticleResponse result = articleService.createArticle(request, email);

        // Then
        assertThat(result.getArticle().getTitle()).isEqualTo("My Article");
        assertThat(result.getArticle().getSlug()).isEqualTo("my-article");
        assertThat(result.getArticle().getTagList()).containsExactly("tag1");
        verify(articleRepository).save(any(ArticleEntity.class));
    }

    @Test
    void createArticle_DuplicateTitle_ThrowsResourceAlreadyExistsException() {
        // Given
        String email = "author@example.com";
        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder().title("Duplicate").build())
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(new UserEntity()));
        given(articleRepository.findByTitle("Duplicate")).willReturn(Optional.of(new ArticleEntity()));

        // When / Then
        assertThatThrownBy(() -> articleService.createArticle(request, email))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Title already exists");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getArticles_ValidCriteria_ReturnsOkWithArticles() {
        // Given
        ArticleEntity article = ArticleEntity.builder()
                .title("Title")
                .slug("slug")
                .author(UserEntity.builder().username("author").build())
                .tags(new HashSet<>())
                .favoritedBy(new HashSet<>())
                .build();
        Page<ArticleEntity> page = new PageImpl<>(List.of(article));

        given(articleRepository.findAll(any(Specification.class), any(PageRequest.class))).willReturn(page);
        given(profileService.getProfile(eq("author"), any())).willReturn(
                ProfileResponse.builder().profile(ProfileResponse.ProfileData.builder().username("author").build()).build()
        );

        // When
        MultipleArticlesResponse result = articleService.getArticles(null, null, null, 20, 0, Optional.empty());

        // Then
        assertThat(result.getArticles()).hasSize(1);
        assertThat(result.getArticlesCount()).isEqualTo(1);
        assertThat(result.getArticles().getFirst().getTitle()).isEqualTo("Title");
    }

    @Test
    void getFeed_FollowedAuthorsExist_ReturnsOkWithFeedArticles() {
        // Given
        String email = "user@example.com";
        UserEntity author = UserEntity.builder().username("author").build();
        UserEntity user = UserEntity.builder()
                .username("user")
                .email(email)
                .following(new HashSet<>(List.of(author)))
                .build();
        ArticleEntity article = ArticleEntity.builder()
                .title("Followed Title")
                .slug("followed-slug")
                .author(author)
                .tags(new HashSet<>())
                .favoritedBy(new HashSet<>())
                .build();
        Page<ArticleEntity> page = new PageImpl<>(List.of(article));

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(articleRepository.findByAuthorIn(eq(user.getFollowing()), any(PageRequest.class))).willReturn(page);
        given(profileService.getProfile(eq("author"), any())).willReturn(
                ProfileResponse.builder().profile(ProfileResponse.ProfileData.builder().username("author").build()).build()
        );

        // When
        MultipleArticlesResponse result = articleService.getFeed(20, 0, email);

        // Then
        assertThat(result.getArticles()).hasSize(1);
        assertThat(result.getArticles().getFirst().getTitle()).isEqualTo("Followed Title");
        verify(articleRepository).findByAuthorIn(eq(user.getFollowing()), any(PageRequest.class));
    }

    @Test
    void getArticle_ValidSlug_ReturnsArticle() {
        // Given
        String slug = "slug";
        ArticleEntity article = ArticleEntity.builder()
                .slug(slug)
                .author(UserEntity.builder().username("author").build())
                .tags(new HashSet<>())
                .favoritedBy(new HashSet<>())
                .build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(profileService.getProfile(eq("author"), any())).willReturn(
                ProfileResponse.builder().profile(ProfileResponse.ProfileData.builder().username("author").build()).build()
        );

        // When
        ArticleResponse result = articleService.getArticle(slug, Optional.empty());

        // Then
        assertThat(result.getArticle().getSlug()).isEqualTo(slug);
    }

    @Test
    void updateArticle_AuthorUpdatesTitle_SavesAndReturnsUpdatedArticle() {
        // Given
        String slug = "old-slug";
        String email = "author@example.com";
        UserEntity author = UserEntity.builder().username("author").email(email).build();
        ArticleEntity article = ArticleEntity.builder()
                .title("Old Title")
                .slug(slug)
                .author(author)
                .tags(new HashSet<>())
                .favoritedBy(new HashSet<>())
                .build();
        UpdateArticleRequest request = UpdateArticleRequest.builder()
                .article(UpdateArticleRequest.ArticleData.builder().title("New Title").build())
                .build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(articleRepository.findByTitle("New Title")).willReturn(Optional.empty());
        given(articleRepository.save(any(ArticleEntity.class))).willAnswer(inv -> inv.getArgument(0));
        given(profileService.getProfile(eq("author"), any())).willReturn(
                ProfileResponse.builder().profile(ProfileResponse.ProfileData.builder().username("author").build()).build()
        );

        // When
        ArticleResponse result = articleService.updateArticle(slug, request, email);

        // Then
        assertThat(result.getArticle().getTitle()).isEqualTo("New Title");
        assertThat(result.getArticle().getSlug()).isEqualTo("new-title");
        verify(articleRepository).save(article);
    }

    @Test
    void updateArticle_NonAuthor_ThrowsUnauthorizedException() {
        // Given
        String slug = "slug";
        String email = "not-author@example.com";
        ArticleEntity article = ArticleEntity.builder()
                .author(UserEntity.builder().email("author@example.com").build())
                .build();
        UpdateArticleRequest request = UpdateArticleRequest.builder().article(new UpdateArticleRequest.ArticleData()).build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));

        // When / Then
        assertThatThrownBy(() -> articleService.updateArticle(slug, request, email))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void deleteArticle_AuthorDeletes_CallsDelete() {
        // Given
        String slug = "slug";
        String email = "author@example.com";
        ArticleEntity article = ArticleEntity.builder()
                .author(UserEntity.builder().email(email).build())
                .build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));

        // When
        articleService.deleteArticle(slug, email);

        // Then
        verify(articleRepository).delete(article);
    }

    @Test
    void favoriteArticle_ValidArticleAndUser_SavesAndReturnsFavoritedArticle() {
        // Given
        String slug = "test-article";
        String email = "user@example.com";
        UserEntity author = UserEntity.builder().username("author").build();
        UserEntity user = UserEntity.builder().username("user").email(email).build();
        ArticleEntity article = ArticleEntity.builder()
                .slug(slug)
                .author(author)
                .favoritedBy(new HashSet<>())
                .tags(new HashSet<>())
                .build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(articleRepository.save(any(ArticleEntity.class))).willAnswer(inv -> inv.getArgument(0));
        given(profileService.getProfile(eq("author"), any())).willReturn(
                ProfileResponse.builder().profile(ProfileResponse.ProfileData.builder().username("author").build()).build()
        );

        // When
        ArticleResponse result = articleService.favoriteArticle(slug, email);

        // Then
        assertThat(result.getArticle().isFavorited()).isTrue();
        assertThat(result.getArticle().getFavoritesCount()).isEqualTo(1);
        assertThat(article.getFavoritedBy()).contains(user);
        verify(articleRepository).save(article);
    }

    @Test
    void unfavoriteArticle_ValidArticleAndUser_SavesAndReturnsUnfavoritedArticle() {
        // Given
        String slug = "test-article";
        String email = "user@example.com";
        UserEntity author = UserEntity.builder().username("author").build();
        UserEntity user = UserEntity.builder().username("user").email(email).build();
        ArticleEntity article = ArticleEntity.builder()
                .slug(slug)
                .author(author)
                .favoritedBy(new HashSet<>(List.of(user)))
                .tags(new HashSet<>())
                .build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(articleRepository.save(any(ArticleEntity.class))).willAnswer(inv -> inv.getArgument(0));
        given(profileService.getProfile(eq("author"), any())).willReturn(
                ProfileResponse.builder().profile(ProfileResponse.ProfileData.builder().username("author").build()).build()
        );

        // When
        ArticleResponse result = articleService.unfavoriteArticle(slug, email);

        // Then
        assertThat(result.getArticle().isFavorited()).isFalse();
        assertThat(result.getArticle().getFavoritesCount()).isEqualTo(0);
        assertThat(article.getFavoritedBy()).isEmpty();
        verify(articleRepository).save(article);
    }
}
