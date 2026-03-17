package net.chrisrichardson.ftgo.consumerservice.api.web;

/**
 * Response DTO for consumer creation.
 *
 * <p>Service ownership: Consumer Service</p>
 */
public class CreateConsumerResponse {

  private long consumerId;

  private CreateConsumerResponse() {
  }

  public CreateConsumerResponse(long consumerId) {
    this.consumerId = consumerId;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }
}
