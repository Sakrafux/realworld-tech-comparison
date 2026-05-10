package com.sakrafux.realworld.features.user;

import com.sakrafux.realworld.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @EqualsAndHashCode.Include
    @Column(unique = true, nullable = false, length = 50)
    public String username;

    @EqualsAndHashCode.Include
    @Column(unique = true, nullable = false, length = 100)
    public String email;

    @Column(nullable = false, length = 60)
    public String password;

    @Column(nullable = false)
    public String bio;

    public String image;

    @ManyToMany
    @JoinTable(
        name = "follow_is_user_to_user",
        joinColumns = @JoinColumn(name = "following_user_id"),
        inverseJoinColumns = @JoinColumn(name = "followed_user_id")
    )
    @Builder.Default
    public Set<UserEntity> following = new HashSet<>();

    @ManyToMany(mappedBy = "following")
    @Builder.Default
    public Set<UserEntity> followers = new HashSet<>();

    public static Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public static Optional<UserEntity> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }
}
