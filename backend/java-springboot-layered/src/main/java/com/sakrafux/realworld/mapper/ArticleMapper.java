package com.sakrafux.realworld.mapper;

import com.sakrafux.realworld.dto.response.ArticleResponse;
import com.sakrafux.realworld.dto.response.MultipleArticlesResponse;
import com.sakrafux.realworld.dto.response.ProfileResponse;
import com.sakrafux.realworld.entity.ArticleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface ArticleMapper {

    /**
     * Maps an ArticleEntity and its calculated properties to an ArticleResponse.
     */
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

    /**
     * Maps a list of ArticleData to a MultipleArticlesResponse.
     */
    MultipleArticlesResponse toMultipleResponse(List<ArticleResponse.ArticleData> articles, int articlesCount);

    /**
     * Utility method for MapStruct to convert Instant to ZonedDateTime.
     */
    default ZonedDateTime map(Instant instant) {
        return instant == null ? null : ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }
}
