package com.sakrafux.realworld.features.comment;

import com.sakrafux.realworld.features.comment.dto.CommentResponse;
import com.sakrafux.realworld.features.comment.dto.MultipleCommentsResponse;
import com.sakrafux.realworld.features.comment.dto.NewCommentRequest;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Optional;

@Controller("/articles/{slug}/comments")
@RequiredArgsConstructor
public class CommentsController {

    private final CommentService commentService;

    @Post
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public CommentResponse addComment(String slug, @Valid @Body NewCommentRequest request, Principal principal) {
        return commentService.addComment(slug, request, principal.getName());
    }

    @Get
    @Secured(SecurityRule.IS_ANONYMOUS)
    public MultipleCommentsResponse getComments(String slug, @Nullable Principal principal) {
        return commentService.getComments(slug, Optional.ofNullable(principal).map(Principal::getName));
    }

    @Delete("/{id}")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public HttpResponse<?> deleteComment(String slug, Long id, Principal principal) {
        commentService.deleteComment(slug, id, principal.getName());
        return HttpResponse.ok();
    }
}
