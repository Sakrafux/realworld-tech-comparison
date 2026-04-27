package com.sakrafux.realworld.article.infrastructure.persistence.adapter;

import com.sakrafux.realworld.article.application.port.out.TagRepository;
import com.sakrafux.realworld.article.domain.Tag;
import com.sakrafux.realworld.article.infrastructure.persistence.entity.TagEntity;
import com.sakrafux.realworld.article.infrastructure.persistence.repository.TagJpaRepository;
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

    @Override
    public List<Tag> findByNames(List<String> names) {
        return tagJpaRepository.findByTagIn(names).stream()
                .map(entity -> new Tag(entity.getTag()))
                .toList();
    }

    @Override
    public void saveAll(List<Tag> tags) {
        List<TagEntity> entities = tags.stream()
                .map(tag -> TagEntity.builder().tag(tag.getName()).build())
                .toList();
        tagJpaRepository.saveAll(entities);
    }
}
