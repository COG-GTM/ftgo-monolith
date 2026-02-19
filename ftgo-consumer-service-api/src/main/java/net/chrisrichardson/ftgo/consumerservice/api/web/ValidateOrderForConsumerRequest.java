package net.chrisrichardson.ftgo.consumerservice.api.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request to validate an order for a consumer")
public class ValidateOrderForConsumerRequest {
  @ApiModelProperty(value = "Consumer ID", required = true)
  private long consumerId;
  @ApiModelProperty(value = "Order total amount", required = true)
  private String orderTotal;

  private ValidateOrderForConsumerRequest() {
  }

  public ValidateOrderForConsumerRequest(long consumerId, String orderTotal) {
    this.consumerId = consumerId;
    this.orderTotal = orderTotal;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public String getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(String orderTotal) {
    this.orderTotal = orderTotal;
  }
}
