package com.sakrafux.realworld.infrastructure.adapter.in.web.controller;

import com.sakrafux.realworld.infrastructure.adapter.in.web.dto.response.TagsResponse;
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

    /**
     * Retrieves a list of all tags.
     * Maps to: GET /api/tags
     *
     * @return a response containing a list of tag names
     */
    @GetMapping
    public TagsResponse getTags() {
        throw new UnsupportedOperationException("TODO");
    }
}
