package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.ArticleResponse;
import com.sakrafux.realworld.features.article.dto.MultipleArticlesResponse;
import com.sakrafux.realworld.features.user.dto.ProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface ArticleMapper {

    default ArticleResponse toResponse(ArticleEntity article, List<String> tagList, boolean favorited, int favoritesCount, ProfileResponse.ProfileData author) {
        return ArticleResponse.builder()
                .article(toArticleData(article, tagList, favorited, favoritesCount, author))
                .build();
    }

    @Mapping(target = "tagList", source = "tagList")
    @Mapping(target = "favorited", source = "favorited")
    @Mapping(target = "favoritesCount", source = "favoritesCount")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "createdAt", source = "article.createdAt")
    @Mapping(target = "updatedAt", source = "article.updatedAt")
    ArticleResponse.ArticleData toArticleData(ArticleEntity article, List<String> tagList, boolean favorited, int favoritesCount, ProfileResponse.ProfileData author);

    MultipleArticlesResponse toMultipleResponse(List<ArticleResponse.ArticleData> articles, int articlesCount);
}
