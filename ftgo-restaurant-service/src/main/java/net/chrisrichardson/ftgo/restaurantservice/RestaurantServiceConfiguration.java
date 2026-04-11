package net.chrisrichardson.ftgo.restaurantservice;

import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Import({CommonSwaggerConfiguration.class, CommonConfiguration.class})
public class RestaurantServiceConfiguration {
}
