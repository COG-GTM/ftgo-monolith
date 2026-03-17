package net.chrisrichardson.ftgo.restaurantservice;

import net.chrisrichardson.ftgo.openapi.config.FtgoOpenApiAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Import({FtgoOpenApiAutoConfiguration.class})
public class RestaurantServiceConfiguration {
}
