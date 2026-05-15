package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.TagsResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.RequiredArgsConstructor;

@Controller("/tags")
@RequiredArgsConstructor
@Secured(SecurityRule.IS_ANONYMOUS)
public class TagsController {

    private final TagService tagService;

    @Get
    public TagsResponse getTags() {
        return tagService.getAllTags();
    }
}
