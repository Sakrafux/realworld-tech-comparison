package com.sakrafux.realworld.application.service;

import com.sakrafux.realworld.application.port.in.article.ArticleListResult;
import com.sakrafux.realworld.application.port.in.article.CreateArticleUseCase.CreateArticleCommand;
import com.sakrafux.realworld.application.port.in.article.GetArticlesQuery;
import com.sakrafux.realworld.application.port.in.article.UpdateArticleUseCase.UpdateArticleCommand;
import com.sakrafux.realworld.application.port.in.profile.GetProfileQuery;
import com.sakrafux.realworld.application.port.out.ArticleRepository;
import com.sakrafux.realworld.application.port.out.TagRepository;
import com.sakrafux.realworld.application.port.out.UserRepository;
import com.sakrafux.realworld.domain.exception.ResourceAlreadyExistsException;
import com.sakrafux.realworld.domain.model.Article;
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
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private GetProfileQuery getProfileQuery;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void createArticle_validCommand_savesAndReturnsArticle() {
        // Given
        String email = "author@example.com";
        User author = User.builder().username("author").email(email).build();
        CreateArticleCommand command = CreateArticleCommand.builder()
                .title("My Article")
                .description("Desc")
                .body("Body")
                .tagList(List.of("tag1"))
                .authorEmail(email)
                .build();

        Profile authorProfile = Profile.builder().username("author").build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(author));
        given(articleRepository.findByTitle("My Article")).willReturn(Optional.empty());
        given(articleRepository.findBySlug("my-article")).willReturn(Optional.empty());
        given(getProfileQuery.getProfile(eq("author"), any())).willReturn(authorProfile);
        given(articleRepository.save(any(Article.class))).willAnswer(inv -> inv.getArgument(0));

        // When
        Article result = articleService.createArticle(command);

        // Then
        assertThat(result.getTitle()).isEqualTo("My Article");
        assertThat(result.getSlug()).isEqualTo("my-article");
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void createArticle_duplicateTitle_throwsException() {
        // Given
        String email = "author@example.com";
        CreateArticleCommand command = CreateArticleCommand.builder().title("Duplicate").authorEmail(email).build();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(User.builder().build()));
        given(articleRepository.findByTitle("Duplicate")).willReturn(Optional.of(Article.builder().build()));

        // When / Then
        assertThatThrownBy(() -> articleService.createArticle(command))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void getArticle_existingArticle_returnsArticle() {
        // Given
        String slug = "slug";
        Article article = Article.builder().slug(slug).author(Profile.builder().username("author").build()).build();
        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(getProfileQuery.getProfile(eq("author"), any())).willReturn(Profile.builder().username("author").build());

        // When
        Article result = articleService.getArticle(slug, Optional.empty());

        // Then
        assertThat(result.getSlug()).isEqualTo(slug);
    }

    @Test
    void updateArticle_authorUpdates_savesArticle() {
        // Given
        String slug = "slug";
        String email = "author@example.com";
        User author = User.builder().username("author").email(email).build();
        Article article = Article.builder().slug(slug).title("Old").author(Profile.builder().username("author").build()).build();
        UpdateArticleCommand command = UpdateArticleCommand.builder().slug(slug).title("New").authorEmail(email).build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(author));
        given(articleRepository.save(any(Article.class))).willAnswer(inv -> inv.getArgument(0));

        // When
        Article result = articleService.updateArticle(command);

        // Then
        assertThat(result.getTitle()).isEqualTo("New");
        verify(articleRepository).save(article);
    }

    @Test
    void deleteArticle_authorDeletes_callsRepository() {
        // Given
        String slug = "slug";
        String email = "author@example.com";
        User author = User.builder().username("author").email(email).build();
        Article article = Article.builder().slug(slug).author(Profile.builder().username("author").build()).build();

        given(articleRepository.findBySlug(slug)).willReturn(Optional.of(article));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(author));

        // When
        articleService.deleteArticle(slug, email);

        // Then
        verify(articleRepository).delete(slug);
    }

    @Test
    void getArticles_validFilter_returnsArticleList() {
        // Given
        GetArticlesQuery.GetArticlesFilter filter = new GetArticlesQuery.GetArticlesFilter(
                null, null, null, 20, 0, Optional.empty());
        Article article = Article.builder().author(Profile.builder().username("author").build()).build();
        given(articleRepository.findFiltered(filter)).willReturn(List.of(article));
        given(articleRepository.countFiltered(filter)).willReturn(1L);
        given(getProfileQuery.getProfile(eq("author"), any())).willReturn(Profile.builder().username("author").build());

        // When
        ArticleListResult result = articleService.getArticles(filter);

        // Then
        assertThat(result.articles()).hasSize(1);
        assertThat(result.totalCount()).isEqualTo(1L);
    }

    @Test
    void getFeed_validObserver_returnsArticleList() {
        // Given
        String email = "observer@example.com";
        Article article = Article.builder().author(Profile.builder().username("author").build()).build();
        given(articleRepository.findFeed(email, 20, 0)).willReturn(List.of(article));
        given(articleRepository.countFeed(email)).willReturn(1L);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(User.builder().id(1L).build()));
        given(getProfileQuery.getProfile(eq("author"), any())).willReturn(Profile.builder().username("author").build());

        // When
        ArticleListResult result = articleService.getFeed(20, 0, email);

        // Then
        assertThat(result.articles()).hasSize(1);
        assertThat(result.totalCount()).isEqualTo(1L);
    }
}
