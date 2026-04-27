package com.sakrafux.realworld.user.infrastructure.persistence.mapper;

import com.sakrafux.realworld.user.domain.User;
import com.sakrafux.realworld.user.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface UserPersistenceMapper {

    @Mapping(target = "following", ignore = true)
    @Mapping(target = "followers", ignore = true)
    User toDomain(UserEntity entity);

    @Mapping(target = "following", ignore = true)
    @Mapping(target = "followers", ignore = true)
    UserEntity toEntity(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "following", ignore = true)
    @Mapping(target = "followers", ignore = true)
    void updateEntityFromDomain(User user, @MappingTarget UserEntity entity);
}
