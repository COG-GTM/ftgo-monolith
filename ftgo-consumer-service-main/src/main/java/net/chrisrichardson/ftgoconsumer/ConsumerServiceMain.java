package net.chrisrichardson.ftgoconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.CommonJsonMapperInitializer;
import net.chrisrichardson.ftgo.common.tracking.CorrelationIdInterceptor;
import net.chrisrichardson.ftgo.common.web.WebCompatibilityConfiguration;
import net.chrisrichardson.ftgo.consumerservice.main.ConsumerServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.handler.MappedInterceptor;

@SpringBootApplication
@Import({ConsumerServiceConfiguration.class, WebCompatibilityConfiguration.class})
public class ConsumerServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerServiceMain.class, args);
  }

  @Bean
  ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  CommonJsonMapperInitializer commonJsonMapperInitializer() {
    return new CommonJsonMapperInitializer();
  }

  @Bean
  MappedInterceptor consumerCorrelationIdInterceptor() {
    return new MappedInterceptor(new String[]{"/consumers/**"}, new CorrelationIdInterceptor());
  }
}
