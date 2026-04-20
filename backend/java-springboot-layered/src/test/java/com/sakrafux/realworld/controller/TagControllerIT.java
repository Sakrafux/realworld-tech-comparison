package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.entity.TagEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link TagController}.
 */
class TagControllerIT extends AbstractControllerIT {

    @BeforeEach
    void setUp() {
        // cleanup is already handled by AbstractControllerIT
        tagRepository.save(TagEntity.builder().tag("reactjs").build());
        tagRepository.save(TagEntity.builder().tag("angularjs").build());
    }

    @Test
    void getTags_TagsExist_ReturnsOkWithTags() throws Exception {
        mockMvc.perform(get("/tags")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags", hasItems("reactjs", "angularjs")));
    }
}
