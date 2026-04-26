package com.sakrafux.realworld.infrastructure.adapter.out.persistence.mapper;

import com.sakrafux.realworld.domain.model.User;
import com.sakrafux.realworld.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserPersistenceMapperTest {

    private final UserPersistenceMapper mapper = Mappers.getMapper(UserPersistenceMapper.class);

    @Test
    void toDomain_validEntity_returnsDomain() {
        // Given
        UserEntity entity = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        // When
        User domain = mapper.toDomain(entity);

        // Then
        assertThat(domain.getUsername()).isEqualTo("testuser");
        assertThat(domain.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void toEntity_validDomain_returnsEntity() {
        // Given
        User domain = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        // When
        UserEntity entity = mapper.toEntity(domain);

        // Then
        assertThat(entity.getUsername()).isEqualTo("testuser");
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
    }
}
