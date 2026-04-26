package com.sakrafux.realworld.infrastructure.adapter.in.web.mapper;

import com.sakrafux.realworld.application.port.in.article.CreateArticleUseCase.CreateArticleCommand;
import com.sakrafux.realworld.application.port.in.article.UpdateArticleUseCase.UpdateArticleCommand;
import com.sakrafux.realworld.domain.model.Article;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.NewArticleRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.request.UpdateArticleRequest;
import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.ArticleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    @Mapping(target = "article", source = "domain")
    ArticleResponse toResponse(Article domain);
}
