package com.sakrafux.realworld.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_gen")
    @SequenceGenerator(name = "user_id_gen", sequenceName = "seq_user_id", allocationSize = 1)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @EqualsAndHashCode.Include
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false)
    private String bio;

    private String image;

    @ManyToMany
    @JoinTable(
        name = "follow_is_user_to_user",
        joinColumns = @JoinColumn(name = "following_user_id"),
        inverseJoinColumns = @JoinColumn(name = "followed_user_id")
    )
    @Builder.Default
    private Set<UserEntity> following = new HashSet<>();

    @ManyToMany(mappedBy = "following")
    @Builder.Default
    private Set<UserEntity> followers = new HashSet<>();
}
