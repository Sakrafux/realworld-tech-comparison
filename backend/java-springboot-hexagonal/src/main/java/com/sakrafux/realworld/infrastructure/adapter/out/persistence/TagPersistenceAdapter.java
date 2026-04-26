package com.sakrafux.realworld.infrastructure.adapter.out.persistence;

import com.sakrafux.realworld.application.port.out.TagRepository;
import com.sakrafux.realworld.domain.model.Tag;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.repository.TagJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TagPersistenceAdapter implements TagRepository {

    private final TagJpaRepository tagJpaRepository;

    @Override
    public List<Tag> findAll() {
        return tagJpaRepository.findAllTagNames().stream()
                .map(Tag::new)
                .toList();
    }
}
