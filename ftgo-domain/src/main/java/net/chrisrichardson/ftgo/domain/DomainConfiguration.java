package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "net.chrisrichardson.ftgo.domain")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.domain")
@Import(CommonConfiguration.class)
public class DomainConfiguration {
}
