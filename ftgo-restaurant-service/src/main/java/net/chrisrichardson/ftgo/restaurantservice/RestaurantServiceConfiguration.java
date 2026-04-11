package net.chrisrichardson.ftgo.restaurantservice;

import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EntityScan(basePackages = "net.chrisrichardson.ftgo.restaurantservice.domain")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.restaurantservice.domain")
@Import({CommonSwaggerConfiguration.class})
public class RestaurantServiceConfiguration {
}
