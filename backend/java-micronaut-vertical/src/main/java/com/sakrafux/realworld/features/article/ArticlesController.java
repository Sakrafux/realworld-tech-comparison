package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.ArticleResponse;
import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.article.dto.UpdateArticleRequest;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Optional;

@Controller("/articles")
@RequiredArgsConstructor
public class ArticlesController {

    private final ArticleService articleService;

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
