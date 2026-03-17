package net.chrisrichardson.ftgo.restaurantservice;

import net.chrisrichardson.ftgo.openapi.config.FtgoOpenApiConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Import({FtgoOpenApiConfiguration.class})
public class RestaurantServiceConfiguration {
}
