package net.chrisrichardson.ftgo.consumerservice.application;

import net.chrisrichardson.ftgo.consumerservice.web.ConsumerWebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "net.chrisrichardson.ftgo.domain")
@EnableJpaRepositories(basePackages = "net.chrisrichardson.ftgo.domain")
@Import(ConsumerWebConfiguration.class)
public class ConsumerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerServiceApplication.class, args);
    }
}
