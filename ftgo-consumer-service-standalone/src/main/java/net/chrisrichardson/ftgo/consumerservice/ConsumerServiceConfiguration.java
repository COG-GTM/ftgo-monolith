package net.chrisrichardson.ftgo.consumerservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.consumerservice.common.MoneyModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class ConsumerServiceConfiguration {

  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.modulesToInstall(new MoneyModule());
    return builder;
  }
}
