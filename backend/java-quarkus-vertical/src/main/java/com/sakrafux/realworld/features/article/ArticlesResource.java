package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.ArticleResponse;
import com.sakrafux.realworld.features.article.dto.MultipleArticlesResponse;
import com.sakrafux.realworld.features.article.dto.NewArticleRequest;
import com.sakrafux.realworld.features.article.dto.UpdateArticleRequest;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;

@Path("/articles")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ArticlesResource {

    private final ArticleService articleService;
    private final JsonWebToken jwt;

    @GET
    public MultipleArticlesResponse getArticles(
            @QueryParam("tag") String tag,
            @QueryParam("author") String author,
            @QueryParam("favorited") String favorited,
            @QueryParam("limit") @DefaultValue("20") @Min(1) int limit,
            @QueryParam("offset") @DefaultValue("0") @Min(0) int offset) {
        return articleService.getArticles(tag, author, favorited, limit, offset, Optional.ofNullable(jwt.getName()));
    }

    @GET
    @Path("/feed")
    @Authenticated
    public MultipleArticlesResponse getFeed(
            @QueryParam("limit") @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {
        return articleService.getFeed(limit, offset, jwt.getName());
    }

    @GET
    @Path("/{slug}")
    public ArticleResponse getArticle(@PathParam("slug") String slug) {
        return articleService.getArticle(slug, Optional.ofNullable(jwt.getName()));
    }

    @POST
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createArticle(@Valid NewArticleRequest request) {
        ArticleResponse response = articleService.createArticle(request, jwt.getName());
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{slug}")
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    public ArticleResponse updateArticle(@PathParam("slug") String slug, @Valid UpdateArticleRequest request) {
        return articleService.updateArticle(slug, request, jwt.getName());
    }

    @DELETE
    @Path("/{slug}")
    @Authenticated
    public Response deleteArticle(@PathParam("slug") String slug) {
        articleService.deleteArticle(slug, jwt.getName());
        return Response.ok().build();
    }

    @POST
    @Path("/{slug}/favorite")
    @Authenticated
    public ArticleResponse favoriteArticle(@PathParam("slug") String slug) {
        return articleService.favoriteArticle(slug, jwt.getName());
    }

    @DELETE
    @Path("/{slug}/favorite")
    @Authenticated
    public ArticleResponse unfavoriteArticle(@PathParam("slug") String slug) {
        return articleService.unfavoriteArticle(slug, jwt.getName());
    }
}
