package net.chrisrichardson.ftgo.orderservice.api.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@ApiModel(description = "Request to revise an existing order")
public class ReviseOrderRequest {
  @ApiModelProperty(value = "Map of menu item ID to revised quantity", required = true)
  private Map<String, Integer> revisedLineItemQuantities;

  private ReviseOrderRequest() {
  }

  public ReviseOrderRequest(Map<String, Integer> revisedLineItemQuantities) {
    this.revisedLineItemQuantities = revisedLineItemQuantities;
  }

  public Map<String, Integer> getRevisedLineItemQuantities() {
    return revisedLineItemQuantities;
  }

  public void setRevisedLineItemQuantities(Map<String, Integer> revisedLineItemQuantities) {
    this.revisedLineItemQuantities = revisedLineItemQuantities;
  }
}
