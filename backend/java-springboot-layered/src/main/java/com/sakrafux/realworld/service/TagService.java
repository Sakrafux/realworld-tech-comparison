package com.sakrafux.realworld.service;

import com.sakrafux.realworld.dto.response.TagsResponse;
import com.sakrafux.realworld.mapper.TagMapper;
import com.sakrafux.realworld.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Transactional(readOnly = true)
    public TagsResponse getAllTags() {
        return tagMapper.toResponse(tagRepository.findAllTagNames());
    }
}
