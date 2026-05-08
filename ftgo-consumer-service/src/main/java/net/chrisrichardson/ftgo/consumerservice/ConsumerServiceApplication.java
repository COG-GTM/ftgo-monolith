package net.chrisrichardson.ftgo.consumerservice;

import net.chrisrichardson.ftgo.common.MoneyModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConsumerServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ConsumerServiceApplication.class, args);
  }

  @Bean
  public MoneyModule moneyModule() {
    return new MoneyModule();
  }
}
