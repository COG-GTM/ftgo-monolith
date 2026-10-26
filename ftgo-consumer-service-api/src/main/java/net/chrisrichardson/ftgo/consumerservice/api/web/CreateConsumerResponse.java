package net.chrisrichardson.ftgo.consumerservice.api.web;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CreateConsumerResponse {
  private long consumerId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String password;

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public CreateConsumerResponse() {

  }

  public CreateConsumerResponse(long consumerId) {
    this.consumerId = consumerId;
  }

  public CreateConsumerResponse(long consumerId, String password) {
    this.consumerId = consumerId;
    this.password = password;
  }
}
