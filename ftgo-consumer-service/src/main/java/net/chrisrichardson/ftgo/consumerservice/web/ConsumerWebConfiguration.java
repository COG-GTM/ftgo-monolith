package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerConfiguration;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@Import({ConsumerConfiguration.class, DomainConfiguration.class})
public class ConsumerWebConfiguration {
}
