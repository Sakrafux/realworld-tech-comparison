package com.sakrafux.realworld.user.infrastructure.persistence.repository;

import com.sakrafux.realworld.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);

    boolean existsByIdAndFollowing_Id(Long followerId, Long followeeId);
}
