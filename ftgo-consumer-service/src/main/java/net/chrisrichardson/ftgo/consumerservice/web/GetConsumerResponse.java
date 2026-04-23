package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.common.PersonName;

public class GetConsumerResponse {
  private long consumerId;
  private PersonName name;

  public long getConsumerId() {
    return consumerId;
  }

  public PersonName getName() {
    return name;
  }

  public GetConsumerResponse() {
  }

  public GetConsumerResponse(long consumerId, PersonName name) {
    this.consumerId = consumerId;
    this.name = name;
  }

  public GetConsumerResponse(PersonName name) {
    this.name = name;
  }
}
