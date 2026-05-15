package com.sakrafux.realworld.features.article;

import com.sakrafux.realworld.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TagEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_id_gen")
    @SequenceGenerator(name = "tag_id_gen", sequenceName = "seq_tag_id", allocationSize = 1)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(unique = true, nullable = false, length = 20)
    private String tag;
}
