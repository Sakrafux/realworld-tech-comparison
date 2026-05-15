package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.ArticleResponse;
import com.sakrafux.realworld.features.article.dto.MultipleArticlesResponse;
import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.article.dto.UpdateArticleRequest;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Optional;

@Controller("/articles")
@RequiredArgsConstructor
public class ArticlesController {

    private final ArticleService articleService;

    @Get
    @Secured(SecurityRule.IS_ANONYMOUS)
    public MultipleArticlesResponse getArticles(
            @QueryValue(defaultValue = "") String tag,
            @QueryValue(defaultValue = "") String author,
            @QueryValue(defaultValue = "") String favorited,
            @QueryValue(defaultValue = "20") @Min(1) int limit,
            @QueryValue(defaultValue = "0") @Min(0) int offset,
            @Nullable Principal principal) {
        return articleService.getArticles(
                tag.isEmpty() ? null : tag,
                author.isEmpty() ? null : author,
                favorited.isEmpty() ? null : favorited,
                limit, offset, Optional.ofNullable(principal).map(Principal::getName));
    }

    @Get("/feed")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public MultipleArticlesResponse getFeed(
            @QueryValue(defaultValue = "20") @Min(1) int limit,
            @QueryValue(defaultValue = "0") @Min(0) int offset,
            Principal principal) {
        return articleService.getFeed(limit, offset, principal.getName());
    }

    @Post
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Status(HttpStatus.CREATED)
    public ArticleResponse createArticle(@Valid @Body NewArticleRequest request, Principal principal) {
        return articleService.createArticle(request, principal.getName());
    }

    @Get("/{slug}")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public ArticleResponse getArticle(String slug, @Nullable Principal principal) {
        return articleService.getArticle(slug, Optional.ofNullable(principal).map(Principal::getName));
    }

    @Put("/{slug}")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public ArticleResponse updateArticle(String slug, @Valid @Body UpdateArticleRequest request, Principal principal) {
        return articleService.updateArticle(slug, request, principal.getName());
    }

    @Delete("/{slug}")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public HttpResponse<?> deleteArticle(String slug, Principal principal) {
        articleService.deleteArticle(slug, principal.getName());
        return HttpResponse.ok();
    }

    @Post("/{slug}/favorite")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public ArticleResponse favoriteArticle(String slug, Principal principal) {
        return articleService.favoriteArticle(slug, principal.getName());
    }

    @Delete("/{slug}/favorite")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public ArticleResponse unfavoriteArticle(String slug, Principal principal) {
        return articleService.unfavoriteArticle(slug, principal.getName());
    }
}
