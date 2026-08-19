package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.domain.Consumer;

/**
 * A newly created consumer along with its API key, which is only available at
 * registration time since only its hash is stored.
 */
public class ConsumerRegistration {

  private final Consumer consumer;
  private final String apiKey;

  public ConsumerRegistration(Consumer consumer, String apiKey) {
    this.consumer = consumer;
    this.apiKey = apiKey;
  }

  public Consumer getConsumer() {
    return consumer;
  }

  public String getApiKey() {
    return apiKey;
  }
}
