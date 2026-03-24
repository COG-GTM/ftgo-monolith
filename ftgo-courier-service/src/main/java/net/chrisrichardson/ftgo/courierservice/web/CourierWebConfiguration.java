package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import net.chrisrichardson.ftgo.courierservice.domain.CourierServiceConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Import({CourierServiceConfiguration.class, CommonSwaggerConfiguration.class})
@EnableAutoConfiguration
@EntityScan(basePackages = "net.chrisrichardson.ftgo.courierservice.domain")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.courierservice.domain")
@ComponentScan
public class CourierWebConfiguration {
}
