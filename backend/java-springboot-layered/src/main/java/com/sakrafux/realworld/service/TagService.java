package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.response.TagsResponse;
import com.sakrafux.realworld.mapper.TagMapper;
import com.sakrafux.realworld.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for handling business logic related to tags.
 * Acts as an intermediary between the TagController and TagRepository.
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    /**
     * Retrieves all distinct tag names currently existing in the system.
     * The operation is strictly read-only.
     *
     * @return a TagsResponse containing the list of tags
     */
    @Transactional(readOnly = true)
    public TagsResponse getAllTags() {
        return tagMapper.toResponse(tagRepository.findAllTagNames());
    }
}
