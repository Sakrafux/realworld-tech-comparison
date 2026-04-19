package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.dto.response.TagsResponse;
import com.sakrafux.realworld.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing tags.
 * Exposes endpoints for retrieving all tags used within the application.
 */
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * Retrieves a list of all tags.
     * Maps to: GET /api/tags
     *
     * @return a response containing a list of tag names
     */
    @GetMapping
    public TagsResponse getTags() {
        return tagService.getAllTags();
    }
}
