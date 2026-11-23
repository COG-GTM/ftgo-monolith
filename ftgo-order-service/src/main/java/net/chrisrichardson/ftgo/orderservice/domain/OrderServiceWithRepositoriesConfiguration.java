package net.chrisrichardson.ftgo.orderservice.domain;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({OrderConfiguration.class})
public class OrderServiceWithRepositoriesConfiguration {


}
