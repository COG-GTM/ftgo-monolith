package net.chrisrichardson.ftgo.consumerservice.api.web;

import net.chrisrichardson.ftgo.common.PersonName;

/**
 * Request DTO for creating a new consumer.
 *
 * <p>Service ownership: Consumer Service</p>
 */
public class CreateConsumerRequest {

  private PersonName name;

  private CreateConsumerRequest() {
  }

  public CreateConsumerRequest(PersonName name) {
    this.name = name;
  }

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }
}
