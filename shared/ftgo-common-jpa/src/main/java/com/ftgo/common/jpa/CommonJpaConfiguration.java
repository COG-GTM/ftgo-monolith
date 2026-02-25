package com.ftgo.common.jpa;

import com.ftgo.common.CommonConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring configuration for common JPA utilities.
 *
 * <p>Provides ORM mappings for shared value objects ({@code Money}, {@code Address},
 * {@code PersonName}) via {@code META-INF/orm.xml} and imports the base
 * {@link CommonConfiguration} for Jackson and utility beans.</p>
 *
 * <p>Usage: {@code @Import(CommonJpaConfiguration.class)}</p>
 */
@Configuration
@EntityScan(basePackages = "com.ftgo.common")
@Import(CommonConfiguration.class)
public class CommonJpaConfiguration {
}
