package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.security.ApiSecurityConfiguration;
import net.chrisrichardson.ftgo.orderservice.domain.OrderServiceWithRepositoriesConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@Import({OrderServiceWithRepositoriesConfiguration.class, ApiSecurityConfiguration.class})
public class OrderWebConfiguration {
}
