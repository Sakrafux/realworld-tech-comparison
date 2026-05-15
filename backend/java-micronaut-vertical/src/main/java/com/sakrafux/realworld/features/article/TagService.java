package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.TagsResponse;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagsResponse getAllTags() {
        List<String> tags = tagRepository.findAll().stream()
                .map(TagEntity::getTag)
                .collect(Collectors.toList());
        return tagMapper.toResponse(tags);
    }
}
