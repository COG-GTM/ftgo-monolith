package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.orderservice.domain.OrderServiceWithRepositoriesConfiguration;
import net.chrisrichardson.ftgo.orderservice.security.OrderSecurityConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@Import({OrderServiceWithRepositoriesConfiguration.class, OrderSecurityConfiguration.class})
public class OrderWebConfiguration {
}
