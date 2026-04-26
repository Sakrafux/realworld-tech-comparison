package com.sakrafux.realworld.application.service;

import com.sakrafux.realworld.application.port.in.GetTagsQuery;
import com.sakrafux.realworld.application.port.out.TagRepository;
import com.sakrafux.realworld.domain.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService implements GetTagsQuery {

    private final TagRepository tagRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Tag> getTags() {
        return tagRepository.findAll();
    }
}
