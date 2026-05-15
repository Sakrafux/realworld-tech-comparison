package com.sakrafux.realworld.features.article;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends CrudRepository<TagEntity, Long> {
    Optional<TagEntity> findByTag(String tag);
    List<TagEntity> findByTagIn(Collection<String> tags);
}
