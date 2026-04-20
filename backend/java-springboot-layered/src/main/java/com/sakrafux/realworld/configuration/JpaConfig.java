package com.sakrafux.realworld.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration class for Spring Data JPA.
 * The @EnableJpaAuditing annotation enables JPA Auditing, which automatically
 * populates the createdAt and updatedAt fields on our entities (like BaseEntity)
 * whenever they are persisted or updated in the database.
 */
@Configuration
@EnableJpaAuditing
@EnableRetry
public class JpaConfig {
}
