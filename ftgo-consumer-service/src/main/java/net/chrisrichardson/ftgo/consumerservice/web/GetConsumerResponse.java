package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;

public class GetConsumerResponse {
  private PersonName name;
  private Long consumerId;

  public PersonName getName() {
    return name;
  }

  public Long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(Long consumerId) {
    this.consumerId = consumerId;
  }

  public GetConsumerResponse(PersonName name) {
    this.name = name;
  }
}
