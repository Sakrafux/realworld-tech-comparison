package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.features.article.dto.TagsResponse;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;

    public TagsResponse getAllTags() {
        List<String> tags = TagEntity.listAll().stream()
                .map(entity -> ((TagEntity) entity).getTag())
                .collect(Collectors.toList());
        return tagMapper.toResponse(tags);
    }
}
