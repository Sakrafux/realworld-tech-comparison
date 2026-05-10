package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.TagsResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class TagsResource {

    private final TagService tagService;

    @GET
    public TagsResponse getTags() {
        return tagService.getAllTags();
    }
}
