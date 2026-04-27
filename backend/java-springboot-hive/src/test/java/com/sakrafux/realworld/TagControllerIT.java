package com.sakrafux.realworld;

import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.TagEntity;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.TagJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TagControllerIT extends AbstractControllerIT {

    @Autowired
    protected TagJpaRepository tagRepository;

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
