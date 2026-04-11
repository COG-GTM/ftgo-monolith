package net.chrisrichardson.ftgo.consumerservice.main;

import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import net.chrisrichardson.ftgo.consumerservice.web.ConsumerWebConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EntityScan(basePackages = "net.chrisrichardson.ftgo.consumerservice.domain")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.consumerservice.domain")
@Import({ConsumerWebConfiguration.class, CommonSwaggerConfiguration.class})
public class ConsumerServiceConfiguration {
}
