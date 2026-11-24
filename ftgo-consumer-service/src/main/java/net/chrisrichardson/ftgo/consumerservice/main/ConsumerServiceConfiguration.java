package net.chrisrichardson.ftgo.consumerservice.main;

import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.CommonSwaggerConfiguration;
import net.chrisrichardson.ftgo.consumerservice.web.ConsumerWebConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ConsumerWebConfiguration.class, CommonSwaggerConfiguration.class})
public class ConsumerServiceConfiguration {
}
