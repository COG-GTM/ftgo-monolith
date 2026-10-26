package net.chrisrichardson.ftgo.consumerservice.api.web;

public class CreateConsumerResponse {
  private long consumerId;
  private String apiKey;

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public CreateConsumerResponse() {

  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public CreateConsumerResponse(long consumerId) {
    this.consumerId = consumerId;
  }

  public CreateConsumerResponse(long consumerId, String apiKey) {
    this.consumerId = consumerId;
    this.apiKey = apiKey;
  }
}
