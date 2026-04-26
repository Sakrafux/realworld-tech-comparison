package com.sakrafux.realworld.infrastructure.adapter.in.web.mapper;

import com.sakrafux.realworld.application.port.in.article.CreateArticleUseCase.CreateArticleCommand;
import com.sakrafux.realworld.domain.model.Article;
import com.sakrafux.realworld.domain.model.Profile;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.NewArticleRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.ArticleResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleWebMapperTest {

    @Spy
    private ProfileWebMapper profileWebMapper = Mappers.getMapper(ProfileWebMapper.class);

    @InjectMocks
    private ArticleWebMapperImpl mapper;

    @Test
    void toCreateCommand_validRequest_returnsCommand() {
        // Given
        NewArticleRequest request = NewArticleRequest.builder()
                .article(NewArticleRequest.ArticleData.builder()
                        .title("Title")
                        .description("Desc")
                        .body("Body")
                        .tagList(List.of("tag1"))
                        .build())
                .build();
        String email = "author@example.com";

        // When
        CreateArticleCommand command = mapper.toCreateCommand(request, email);

        // Then
        assertThat(command.title()).isEqualTo("Title");
        assertThat(command.authorEmail()).isEqualTo(email);
        assertThat(command.tagList()).containsExactly("tag1");
    }

    @Test
    void toResponse_validDomain_returnsResponse() {
        // Given
        Article article = Article.builder()
                .title("Title")
                .slug("slug")
                .author(Profile.builder().username("author").build())
                .build();

        // When
        ArticleResponse response = mapper.toResponse(article);

        // Then
        assertThat(response.getArticle().getTitle()).isEqualTo("Title");
        assertThat(response.getArticle().getAuthor().getUsername()).isEqualTo("author");
    }
}
