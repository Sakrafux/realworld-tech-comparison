package com.sakrafux.realworld.article.infrastructure.web.mapper;

import com.sakrafux.realworld.article.application.port.in.ArticleListResult;
import com.sakrafux.realworld.article.application.port.in.CreateArticleUseCase.CreateArticleCommand;
import com.sakrafux.realworld.article.application.port.in.GetArticlesQuery.GetArticlesFilter;
import com.sakrafux.realworld.article.application.port.in.UpdateArticleUseCase.UpdateArticleCommand;
import com.sakrafux.realworld.article.domain.Article;
import com.sakrafux.realworld.article.infrastructure.web.dto.request.NewArticleRequest;
import com.sakrafux.realworld.article.infrastructure.web.dto.request.UpdateArticleRequest;
import com.sakrafux.realworld.article.infrastructure.web.dto.response.ArticleResponse;
import com.sakrafux.realworld.article.infrastructure.web.dto.response.MultipleArticlesResponse;
import com.sakrafux.realworld.user.infrastructure.web.mapper.ProfileWebMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;

@Mapper(uses = {ProfileWebMapper.class})
public interface ArticleWebMapper {

    @Mapping(target = "authorEmail", source = "authorEmail")
    @Mapping(target = "title", source = "request.article.title")
    @Mapping(target = "description", source = "request.article.description")
    @Mapping(target = "body", source = "request.article.body")
    @Mapping(target = "tagList", source = "request.article.tagList")
    CreateArticleCommand toCreateCommand(NewArticleRequest request, String authorEmail);

    @Mapping(target = "authorEmail", source = "authorEmail")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "title", source = "request.article.title")
    @Mapping(target = "description", source = "request.article.description")
    @Mapping(target = "body", source = "request.article.body")
    UpdateArticleCommand toUpdateCommand(UpdateArticleRequest request, String slug, String authorEmail);

    default GetArticlesFilter toFilter(String tag, String author, String favorited, int limit, int offset, Optional<String> observerEmail) {
        return GetArticlesFilter.builder()
                .tag(tag)
                .author(author)
                .favorited(favorited)
                .limit(limit)
                .offset(offset)
                .observerEmail(observerEmail)
                .build();
    }

    @Mapping(target = "article", source = "domain")
    ArticleResponse toResponse(Article domain);

    @Mapping(target = "articles", source = "result.articles")
    @Mapping(target = "articlesCount", source = "result.totalCount")
    MultipleArticlesResponse toMultipleResponse(ArticleListResult result);
}
