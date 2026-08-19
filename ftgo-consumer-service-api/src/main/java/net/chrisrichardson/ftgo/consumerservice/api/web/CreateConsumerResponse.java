package net.chrisrichardson.ftgo.consumerservice.api.web;

public class CreateConsumerResponse {
  private long consumerId;
  private String accessToken;

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public CreateConsumerResponse() {

  }

  public CreateConsumerResponse(long consumerId, String accessToken) {
    this.consumerId = consumerId;
    this.accessToken = accessToken;
  }
}
