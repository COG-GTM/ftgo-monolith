package com.ftgo.domain;

import com.ftgo.common.CommonConfiguration;
import com.ftgo.common.jpa.CommonJpaConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EntityScan
@EnableJpaRepositories
@Import({CommonConfiguration.class, CommonJpaConfiguration.class})
public class DomainConfiguration {
}
