package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.domain.Consumer;

public class ConsumerRegistration {

  private final Consumer consumer;
  private final String password;

  public ConsumerRegistration(Consumer consumer, String password) {
    this.consumer = consumer;
    this.password = password;
  }

  public Consumer getConsumer() {
    return consumer;
  }

  public String getPassword() {
    return password;
  }
}
