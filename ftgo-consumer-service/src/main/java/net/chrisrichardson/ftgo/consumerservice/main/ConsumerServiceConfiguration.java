package net.chrisrichardson.ftgo.consumerservice.main;

import net.chrisrichardson.ftgo.openapi.config.FtgoOpenApiConfiguration;
import net.chrisrichardson.ftgo.consumerservice.web.ConsumerWebConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EntityScan
@Import({ConsumerWebConfiguration.class, FtgoOpenApiConfiguration.class})
public class ConsumerServiceConfiguration {
}
