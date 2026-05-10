package com.sakrafux.realworld.features.comment;

import com.sakrafux.realworld.features.comment.dto.CommentResponse;
import com.sakrafux.realworld.features.comment.dto.MultipleCommentsResponse;
import com.sakrafux.realworld.features.comment.dto.NewCommentRequest;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;

@Path("/articles/{slug}/comments")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class CommentsResource {

    private final CommentService commentService;
    private final JsonWebToken jwt;

    @GET
    public MultipleCommentsResponse getComments(@PathParam("slug") String slug) {
        return commentService.getComments(slug, Optional.ofNullable(jwt.getName()));
    }

    @POST
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    public CommentResponse addComment(@PathParam("slug") String slug, @Valid NewCommentRequest request) {
        return commentService.addComment(slug, request, jwt.getName());
    }

    @DELETE
    @Path("/{id}")
    @Authenticated
    public Response deleteComment(@PathParam("slug") String slug, @PathParam("id") Long id) {
        commentService.deleteComment(slug, id, jwt.getName());
        return Response.ok().build();
    }
}
