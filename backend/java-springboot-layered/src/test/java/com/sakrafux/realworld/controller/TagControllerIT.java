package com.sakrafux.realworld.controller;

import com.sakrafux.realworld.entity.TagEntity;
import com.sakrafux.realworld.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TagControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
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
