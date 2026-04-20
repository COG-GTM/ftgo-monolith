package net.chrisrichardson.ftgo.consumerservice.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "net.chrisrichardson.ftgo")
@EntityScan("net.chrisrichardson.ftgo.consumerservice.domain")
@EnableJpaRepositories("net.chrisrichardson.ftgo.consumerservice.domain")
public class ConsumerServiceStandaloneMain {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerServiceStandaloneMain.class, args);
  }
}
